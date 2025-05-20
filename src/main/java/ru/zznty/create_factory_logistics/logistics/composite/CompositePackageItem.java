package ru.zznty.create_factory_logistics.logistics.composite;

import com.simibubi.create.AllEntityTypes;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.box.PackageStyles;
import com.simibubi.create.foundation.item.render.SimpleCustomRenderer;
import net.createmod.catnip.nbt.NBTHelper;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class CompositePackageItem extends PackageItem {

    public static final String CHILDREN_TAG = "Children";

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
        for (ItemStack child : getChildren(tooltipContext.registries(), stack)) {
            child.getItem().appendHoverText(child, tooltipContext, tooltipComponents, tooltipFlag);
        }
    }

    public static List<ItemStack> getChildren(HolderLookup.Provider lookupProvider, ItemStack stack) {
        if (!stack.has(DataComponents.CUSTOM_DATA) || !stack.get(DataComponents.CUSTOM_DATA).contains(CHILDREN_TAG))
            return List.of();
        ListTag listTag = stack.get(DataComponents.CUSTOM_DATA).copyTag().getList(CHILDREN_TAG, Tag.TAG_COMPOUND);
        return NBTHelper.readCompoundList(listTag, t -> ItemStack.parseOptional(lookupProvider, t));
    }

    public static ItemStack of(HolderLookup.Provider lookupProvider, ItemStack box, List<ItemStack> originalChildren) {
        ItemStack compositeBox = new ItemStack(FactoryItems.COMPOSITE_PACKAGE.get());
        if (box.has(DataComponents.CUSTOM_DATA))
            compositeBox.set(DataComponents.CUSTOM_DATA, box.get(DataComponents.CUSTOM_DATA));

        List<ItemStack> children = new ArrayList<>(originalChildren);

        ItemStackHandler contents = PackageItem.getContents(compositeBox);

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
                    ItemStack reminder = ItemHandlerHelper.insertItemStacked(contents, childContents.getStackInSlot(slot), false);
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

        CustomData.update(DataComponents.CUSTOM_DATA, compositeBox, t ->
                t.put(CHILDREN_TAG, NBTHelper.writeCompoundList(children, s -> (CompoundTag) s.saveOptional(lookupProvider))));

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
}
