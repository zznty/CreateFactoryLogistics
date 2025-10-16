package ru.zznty.create_factory_logistics.logistics.composite;

import com.simibubi.create.AllDataComponents;
import com.simibubi.create.AllEntityTypes;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.box.PackageStyles;
import com.simibubi.create.foundation.item.render.SimpleCustomRenderer;
import net.createmod.catnip.nbt.NBTHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.PatchedDataComponentMap;
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
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;
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
    public void appendHoverText(ItemStack stack, TooltipContext tooltipContext, List<Component> tooltipComponents,
                                TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, tooltipContext, tooltipComponents, tooltipFlag);

        int visibleNames = 0;
        int skippedNames = 0;
        ItemStackHandler contents = getContents(tooltipContext.registries(), stack);
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
            tooltipComponents.add(itemstack.getHoverName()
                                          .copy()
                                          .append(" x")
                                          .append(String.valueOf(itemstack.getCount()))
                                          .withStyle(ChatFormatting.GRAY));
        }

        if (skippedNames > 0)
            tooltipComponents.add(Component.translatable("container.shulkerBox.more", skippedNames)
                                          .withStyle(ChatFormatting.ITALIC));

        for (ItemStack child : getChildren(tooltipContext.registries(), stack)) {
            child.getItem().appendHoverText(child, tooltipContext, tooltipComponents, tooltipFlag);
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> open(Level worldIn, Player playerIn, InteractionHand handIn) {
        ItemStack box = playerIn.getItemInHand(handIn);
        playerIn.setItemInHand(handIn, PackageItem.containing(getContents(box)));
        InteractionResultHolder<ItemStack> resultHolder = super.open(worldIn, playerIn, handIn);
        if (resultHolder.getResult() == InteractionResult.SUCCESS && !worldIn.isClientSide()) {
            for (ItemStack child : getChildren(worldIn.registryAccess(), box)) {
                playerIn.getInventory().placeItemBackInInventory(child);
            }
        }
        return new InteractionResultHolder<>(resultHolder.getResult(), box);
    }

    public static List<ItemStack> getChildren(HolderLookup.Provider lookupProvider, ItemStack stack) {
        if (!stack.has(DataComponents.CUSTOM_DATA) || !stack.get(DataComponents.CUSTOM_DATA).contains(CHILDREN_TAG))
            return List.of();
        ListTag listTag = stack.get(DataComponents.CUSTOM_DATA).copyTag().getList(CHILDREN_TAG, Tag.TAG_COMPOUND);
        return NBTHelper.readCompoundList(listTag, t -> ItemStack.parseOptional(lookupProvider, t));
    }

    public static ItemStackHandler getContents(HolderLookup.Provider lookupProvider, ItemStack box) {
        ItemStackHandler newInv = new ItemStackHandler(PackageItem.SLOTS);

        CustomData customData = box.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        if (customData.contains(ITEMS_TAG)) {
            //noinspection deprecation
            newInv.deserializeNBT(lookupProvider, customData.getUnsafe().getCompound(ITEMS_TAG));
        }
        return newInv;
    }

    public static ItemStack of(HolderLookup.Provider lookupProvider, ItemStack box, List<ItemStack> originalChildren) {
        ItemStack compositeBox = new ItemStack(FactoryItems.COMPOSITE_PACKAGE.get());

        PatchedDataComponentMap components = new PatchedDataComponentMap(box.getComponents());
        components.remove(AllDataComponents.PACKAGE_CONTENTS);
        compositeBox.applyComponents(components);

        List<ItemStack> children = new ArrayList<>(originalChildren);

        ItemStackHandler contents = PackageItem.getContents(box);

        CompoundTag tag = compositeBox.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();

        ListTag listTag = tag.getList(CHILDREN_TAG, Tag.TAG_COMPOUND);
        if (!listTag.isEmpty()) {
            children.addAll(getChildren(lookupProvider, compositeBox));
            listTag.clear();
        }

        for (int i = 0; i < children.size(); i++) {
            ItemStack child = children.get(i);
            if (child.getItem() instanceof CompositePackageItem) {
                children.addAll(getChildren(lookupProvider, child));
                CustomData.update(DataComponents.CUSTOM_DATA, child, childTag -> childTag.remove(CHILDREN_TAG));
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

        CustomData.update(DataComponents.CUSTOM_DATA, compositeBox, t -> {
            t.put(CHILDREN_TAG,
                  NBTHelper.writeCompoundList(children, s -> (CompoundTag) s.saveOptional(lookupProvider)));
            t.put(ITEMS_TAG, contents.serializeNBT(lookupProvider));
        });

        return compositeBox;
    }

    @SuppressWarnings("removal")
    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
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
        int i = this.getUseDuration(stack, entity) - ticks;
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
