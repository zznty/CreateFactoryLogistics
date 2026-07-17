package ru.zznty.create_factory_logistics.compat.mekanism.logistics.barrel;

import com.simibubi.create.content.logistics.box.PackageStyles;
import com.simibubi.create.foundation.item.render.SimpleCustomRenderer;
import com.tterrag.registrate.util.entry.EntityEntry;
import mekanism.api.chemical.ChemicalStack;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;
import ru.zznty.create_factory_logistics.compat.mekanism.FactoryMekanismEntities;
import ru.zznty.create_factory_logistics.compat.mekanism.logistics.panel.FactoryChemicalPanelBehaviour;
import ru.zznty.create_factory_abstractions.logistics.box.AbstractPackageEntity;
import ru.zznty.create_factory_abstractions.logistics.box.AbstractPackageItem;
import ru.zznty.create_factory_logistics.logistics.panel.FactoryFluidPanelBehaviour;

import java.util.List;
import java.util.function.Consumer;

public class BarrelPackageItem extends AbstractPackageItem {
    public BarrelPackageItem(Properties properties, PackageStyles.PackageStyle style) {
        super(properties, style);
        BarrelStyles.ALL_BARRELS.add(this);
    }

    @Override
    protected String getIdSuffix() {
        return "barrel";
    }

    @Override
    public Entity createEntity(Level world, Entity location, ItemStack itemstack) {
        return BarrelPackageEntity.fromDroppedItem(world, location, itemstack);
    }

    @Override
    protected EntityEntry<? extends AbstractPackageEntity> getEntityEntry() {
        return FactoryMekanismEntities.BARREL;
    }

    @Override
    protected InteractionResultHolder<ItemStack> openServerSide(Level worldIn, Player playerIn, InteractionHand handIn,
                                                                ItemStack box, BlockHitResult hitResult) {
        return InteractionResultHolder.success(box);
    }

    @SuppressWarnings("removal")
    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(@NotNull Consumer<IClientItemExtensions> consumer) {
        consumer.accept(SimpleCustomRenderer.create(this, new BarrelItemRenderer()));
        super.initializeClient(consumer);
    }

    public static ItemStack getDefaultBarrel() {
        return BarrelStyles.ALL_BARRELS.get(0).getDefaultInstance();
    }

    @Override
    public void appendHoverText(ItemStack pStack, TooltipContext tooltipContext, List<Component> pTooltipComponents,
                                TooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, tooltipContext, pTooltipComponents, pIsAdvanced);

        ChemicalStack contained = FactoryChemicalPanelBehaviour.getChemicalStack(pStack);

        if (!contained.isEmpty()) {
            pTooltipComponents.add(contained.getTextComponent()
                                           .copy()
                                           .append(" ")
                                           .append(FactoryFluidPanelBehaviour.formatLevel(
                                                   (int) contained.getAmount()).component())
                                           .withStyle(ChatFormatting.GRAY));
        }

        fireGenericTooltip(pStack, pTooltipComponents, tooltipContext);
    }
}
