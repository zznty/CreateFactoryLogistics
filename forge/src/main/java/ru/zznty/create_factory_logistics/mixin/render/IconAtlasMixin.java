package ru.zznty.create_factory_logistics.mixin.render;

import com.simibubi.create.foundation.gui.AllIcons;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import ru.zznty.create_factory_logistics.CreateFactoryLogistics;
import ru.zznty.create_factory_logistics.render.IconAtlasIndexHolder;

@Mixin(AllIcons.class)
public class IconAtlasMixin implements IconAtlasIndexHolder {
    @Unique
    private int createFactoryLogistics$index;

    @Unique
    private static final ResourceLocation[] createFactoryLogistics$atlas = new ResourceLocation[]{
            AllIcons.ICON_ATLAS,
            CreateFactoryLogistics.resource("textures/gui/icons.png")
    };

    @Redirect(
            method = "render(Lnet/minecraft/client/gui/GuiGraphics;II)V",
            at = @At(value = "FIELD", target = "Lcom/simibubi/create/foundation/gui/AllIcons;ICON_ATLAS:Lnet/minecraft/resources/ResourceLocation;"),
            remap = false
    )
    private ResourceLocation redirectAtlas1() {
        return createFactoryLogistics$atlas[createFactoryLogistics$index];
    }

    @Redirect(
            method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(value = "FIELD", target = "Lcom/simibubi/create/foundation/gui/AllIcons;ICON_ATLAS:Lnet/minecraft/resources/ResourceLocation;"),
            remap = false
    )
    private ResourceLocation redirectAtlas2() {
        return createFactoryLogistics$atlas[createFactoryLogistics$index];
    }

    @Override
    public int getIconAtlasIndex() {
        return createFactoryLogistics$index;
    }

    @Override
    public void setIconAtlasIndex(int index) {
        if (index < 0 || index >= createFactoryLogistics$atlas.length) {
            throw new IllegalArgumentException("Invalid index");
        }
        createFactoryLogistics$index = index;
    }
}
