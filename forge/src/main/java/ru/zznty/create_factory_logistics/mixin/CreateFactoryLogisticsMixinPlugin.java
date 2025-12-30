package ru.zznty.create_factory_logistics.mixin;

import net.minecraftforge.fml.loading.LoadingModList;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import ru.zznty.create_factory_abstractions.compat.computercraft.AbstractionsComputerCraftCompat;

import java.util.List;
import java.util.Set;

public class CreateFactoryLogisticsMixinPlugin implements IMixinConfigPlugin {
    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        if (LoadingModList.get().getModFileById(AbstractionsComputerCraftCompat.MOD_ID) != null)
            return List.of(
                    "compat.computercraft.StockTickerPeripheralMixin",
                    "compat.computercraft.GenericRedstoneRequesterPeripheralMixin"
            );

        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
