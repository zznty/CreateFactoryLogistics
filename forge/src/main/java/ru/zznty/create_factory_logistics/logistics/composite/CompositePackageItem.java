package ru.zznty.create_factory_logistics.logistics.composite;

import com.simibubi.create.AllEntityTypes;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.box.PackageStyles;
import com.simibubi.create.foundation.item.render.SimpleCustomRenderer;
import net.createmod.catnip.nbt.NBTHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import ru.zznty.create_factory_logistics.FactoryItems;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class CompositePackageItem extends PackageItem {

    public static final String CHILDREN_TAG = "Children";
    public static final String ITEMS_TAG = "CompositeItems";

    public CompositePackageItem(Properties properties) {
        super(properties, PackageStyles.STYLES.get(1));
        PackageStyles.ALL_BOXES.remove(this);
        PackageStyles.STANDARD_BOXES.remove(this);
    }

    @Override
    public Entity createEntity(Level world, Entity location, ItemStack itemstack) {
        return CompositePackageEntity.fromDroppedItem(world, location, itemstack);
    }

    @Override
    public void appendHoverText(ItemStack pStack, Level pLevel, List<Component> pTooltipComponents,
                                TooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);

        int visibleNames = 0;
        int skippedNames = 0;
        ItemStackHandler contents = getContents(pStack);
        for (int i = 0; i < contents.getSlots(); i++) {
            ItemStack itemstack = contents.getStackInSlot(i);
            if (itemstack.isEmpty())
                continue;
            if (itemstack.getItem() instanceof SpawnEggItem)
                continue;
            if (visibleNames > 2) {
                skippedNames++;
                continue;
            }

            visibleNames++;
            pTooltipComponents.add(itemstack.getHoverName()
                                           .copy()
                                           .append(" x")
                                           .append(String.valueOf(itemstack.getCount()))
                                           .withStyle(ChatFormatting.GRAY));
        }

        if (skippedNames > 0)
            pTooltipComponents.add(Component.translatable("container.shulkerBox.more", skippedNames)
                                           .withStyle(ChatFormatting.ITALIC));

        for (ItemStack child : getChildren(pStack)) {
            child.getItem().appendHoverText(child, pLevel, pTooltipComponents, pIsAdvanced);
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> open(Level worldIn, Player playerIn, InteractionHand handIn) {
        ItemStack box = playerIn.getItemInHand(handIn);
        playerIn.setItemInHand(handIn, PackageItem.containing(getContents(box)));
        InteractionResultHolder<ItemStack> resultHolder = super.open(worldIn, playerIn, handIn);
        if (resultHolder.getResult() == InteractionResult.SUCCESS && !worldIn.isClientSide()) {
            for (ItemStack child : getChildren(box)) {
                playerIn.getInventory().placeItemBackInInventory(child);
            }
        }
        return new InteractionResultHolder<>(resultHolder.getResult(), box);
    }

    public static List<ItemStack> getChildren(ItemStack stack) {
        if (!stack.hasTag()) return List.of();
        return NBTHelper.readCompoundList(stack.getTag().getList(CHILDREN_TAG, Tag.TAG_COMPOUND), ItemStack::of);
    }

    public static ItemStackHandler getContents(ItemStack box) {
        ItemStackHandler newInv = new ItemStackHandler(PackageItem.SLOTS);
        CompoundTag invNBT = box.getOrCreateTagElement(ITEMS_TAG);
        if (!invNBT.isEmpty())
            newInv.deserializeNBT(invNBT);
        return newInv;
    }

    public static ItemStack of(ItemStack box, List<ItemStack> children) {
        if (children.isEmpty()) return box.copy();

        ItemStack compositeBox = new ItemStack(FactoryItems.COMPOSITE_PACKAGE);

        if (box.hasTag()) {
            CompoundTag tag = box.getTag().copy();
            tag.remove("Items");
            compositeBox.setTag(tag);
        }

        children = new ArrayList<>(children);

        ItemStackHandler contents = PackageItem.getContents(box);

        ListTag listTag = compositeBox.getOrCreateTag().getList(CHILDREN_TAG, Tag.TAG_COMPOUND);
        if (!listTag.isEmpty()) {
            children.addAll(getChildren(compositeBox));
            listTag.clear();
        }

        for (int i = 0; i < children.size(); i++) {
            ItemStack child = children.get(i);
            if (child.getItem() instanceof CompositePackageItem) {
                children.addAll(getChildren(child));
                child.getOrCreateTag().remove(CHILDREN_TAG);
                // merge items with parent box
                ItemStackHandler childContents = PackageItem.getContents(child);
                boolean emptied = true;
                for (int slot = 0; slot < childContents.getSlots(); slot++) {
                    ItemStack reminder = ItemHandlerHelper.insertItemStacked(contents,
                                                                             childContents.getStackInSlot(slot), false);
                    childContents.setStackInSlot(slot, reminder);
                    if (!reminder.isEmpty())
                        emptied = false;
                }
                if (!emptied)
                    children.add(PackageItem.containing(childContents));
                children.remove(i);
                i--;
            }
        }

        compositeBox.addTagElement(CHILDREN_TAG, NBTHelper.writeCompoundList(children, ItemStack::serializeNBT));
        compositeBox.addTagElement(ITEMS_TAG, contents.serializeNBT());

        return compositeBox;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(@NotNull Consumer<IClientItemExtensions> consumer) {
        consumer.accept(SimpleCustomRenderer.create(this, new CompositePackageRenderer()));
        super.initializeClient(consumer);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getPlayer()
                .isShiftKeyDown()) {
            return open(context.getLevel(), context.getPlayer(), context.getHand()).getResult();
        }

        Vec3 point = context.getClickLocation();
        float h = style.height() / 16f;
        float r = style.width() / 2f / 16f;

        if (context.getClickedFace() == Direction.DOWN)
            point = point.subtract(0, h + .25f, 0);
        else if (context.getClickedFace()
                .getAxis()
                .isHorizontal())
            point = point.add(Vec3.atLowerCornerOf(context.getClickedFace()
                                                           .getNormal())
                                      .scale(r));

        AABB scanBB = new AABB(point, point).inflate(r, 0, r)
                .expandTowards(0, h, 0);
        Level world = context.getLevel();
        if (!world.getEntities(AllEntityTypes.PACKAGE.get(), scanBB, e -> true)
                .isEmpty())
            return super.useOn(context);

        CompositePackageEntity packageEntity = new CompositePackageEntity(world, point.x, point.y, point.z);
        ItemStack itemInHand = context.getItemInHand();
        packageEntity.setBox(itemInHand.copy());
        world.addFreshEntity(packageEntity);
        itemInHand.shrink(1);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void releaseUsing(ItemStack stack, Level world, LivingEntity entity, int ticks) {
        if (!(entity instanceof Player player))
            return;
        int i = this.getUseDuration(stack) - ticks;
        if (i < 0)
            return;

        float f = getPackageVelocity(i);
        if (f < 0.1D)
            return;
        if (world.isClientSide)
            return;

        world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SNOWBALL_THROW,
                        SoundSource.NEUTRAL, 0.5F, 0.5F);

        ItemStack copy = stack.copy();
        if (!player.getAbilities().instabuild)
            stack.shrink(1);

        Vec3 vec = new Vec3(entity.getX(), entity.getY() + entity.getBoundingBox()
                .getYsize() / 2f, entity.getZ());
        Vec3 motion = entity.getLookAngle()
                .scale(f * 2);
        vec = vec.add(motion);

        CompositePackageEntity packageEntity = new CompositePackageEntity(world, vec.x, vec.y, vec.z);
        packageEntity.setBox(copy);
        packageEntity.setDeltaMovement(motion);
        packageEntity.tossedBy = new WeakReference<>(player);
        world.addFreshEntity(packageEntity);
    }
}
