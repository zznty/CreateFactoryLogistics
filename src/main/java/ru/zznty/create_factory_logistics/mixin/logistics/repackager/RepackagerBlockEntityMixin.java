package ru.zznty.create_factory_logistics.mixin.logistics.repackager;

import com.simibubi.create.content.logistics.packager.repackager.PackageRepackageHelper;
import com.simibubi.create.content.logistics.packager.repackager.RepackagerBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import ru.zznty.create_factory_logistics.logistics.repackager.CompositeRepackagerHelper;

@Mixin(RepackagerBlockEntity.class)
public class RepackagerBlockEntityMixin {
    @Redirect(
            method = "<init>",
            at = @At(
                    value = "NEW",
                    target = "()Lcom/simibubi/create/content/logistics/packager/repackager/PackageRepackageHelper;"
            )
    )
    private PackageRepackageHelper createHelper() {
        return new CompositeRepackagerHelper();
    }
}
