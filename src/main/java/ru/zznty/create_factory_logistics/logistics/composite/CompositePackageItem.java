package ru.zznty.create_factory_logistics.logistics.composite;

import com.simibubi.create.AllEntityTypes;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.box.PackageStyles;
import com.simibubi.create.foundation.item.render.SimpleCustomRenderer;
import net.createmod.catnip.nbt.NBTHelper;
import net.minecraft.core.Direction;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;
import ru.zznty.create_factory_logistics.FactoryItems;

import java.util.List;
import java.util.function.Consumer;

public class CompositePackageItem extends PackageItem {
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
    public void appendHoverText(ItemStack pStack, Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
        for (ItemStack child : getChildren(pStack)) {
            child.getItem().appendHoverText(child, pLevel, pTooltipComponents, pIsAdvanced);
        }
    }

    public static List<ItemStack> getChildren(ItemStack stack) {
        if (!stack.hasTag()) return List.of();
        return NBTHelper.readCompoundList(stack.getTag().getList("Children", Tag.TAG_COMPOUND), ItemStack::of);
    }

    public static ItemStack of(ItemStack box, Iterable<ItemStack> children) {
        ItemStack compositeBox = new ItemStack(FactoryItems.COMPOSITE_PACKAGE);
        compositeBox.setTag(box.getTag());

        compositeBox.addTagElement("Children", NBTHelper.writeCompoundList(children, ItemStack::serializeNBT));

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
}
