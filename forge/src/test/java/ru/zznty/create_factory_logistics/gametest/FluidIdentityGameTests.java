package ru.zznty.create_factory_logistics.gametest;

import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import ru.zznty.create_factory_abstractions.api.generic.key.GenericKey;
import ru.zznty.create_factory_abstractions.api.generic.key.GenericKeyCanonicalizer;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;
import ru.zznty.create_factory_logistics.CreateFactoryLogistics;
import ru.zznty.create_factory_logistics.logistics.generic.FluidGenericStack;
import ru.zznty.create_factory_logistics.logistics.generic.FluidKey;

@GameTestHolder(CreateFactoryLogistics.MODID)
@PrefixGameTestTemplate(false)
public final class FluidIdentityGameTests {
    static {
        GenericKeyCanonicalizer.register(FluidKey.class,
                                          BuiltInRegistries.FLUID.getResourceKey(Fluids.WATER).orElseThrow(),
                                          FluidIdentityGameTests::canonicalize);
    }

    private static FluidKey canonicalize(FluidKey key) {
        CustomData custom = key.nbt().get(DataComponents.CUSTOM_DATA);
        if (custom == null || custom.isEmpty() || !custom.contains("mixed_potion"))
            return key;

        custom.update(FluidIdentityGameTests::stripTransientNbt);

        PatchedDataComponentMap newNbt = key.nbt().copy();
        newNbt.set(DataComponents.CUSTOM_DATA, custom);

        return new FluidKey(key.holder(), newNbt);
    }

    @SuppressWarnings("deprecation")
    private static void stripTransientNbt(CompoundTag root) {
        CompoundTag mixed = root.getCompound("mixed_potion");
        root.remove("mixed_potion");

        CompoundTag identity = new CompoundTag();
        identity.putString("bottle_type", mixed.getString("bottle_type"));
        identity.putInt("color", mixed.getInt("color"));
        identity.putString("name", mixed.getString("name"));

        var composition = mixed.getList("composition", Tag.TAG_COMPOUND);
        for (int i = 0; i < composition.size(); i++) {
            CompoundTag e = composition.getCompound(i);
            CompoundTag c = new CompoundTag();
            c.putString("effect", e.getString("effect"));
            c.putInt("amplifier", e.getInt("amplifier"));
            c.putLong("base_duration_ticks", e.getLong("base_duration_ticks"));
            composition.set(i, c);
        }
        identity.put("composition", composition);

        root.put("mixed_potion", identity);
    }

    @GameTest(template = "empty", batch = "fluid_identity", timeoutTicks = 40)
    public static void sameCompositionDifferentConcentrationAreIdentityEqual(GameTestHelper helper) {
        GenericStack stack1 = FluidGenericStack.wrap(mixedPotionStack(
                entry("minecraft:invisibility", 0, 600, 500, 600),
                entry("minecraft:instant_health", 0, 600, 500, 600)
        ));
        GenericStack stack2 = FluidGenericStack.wrap(mixedPotionStack(
                entry("minecraft:invisibility", 0, 1200, 250, 600),
                entry("minecraft:instant_health", 0, 1200, 250, 600)
        ));

        helper.assertTrue(stack1.canStack(stack2),
                "same composition + different concentration should be identity-equal");
        helper.assertTrue(stack2.canStack(stack1),
                "identity equality must be symmetric");

        GenericKey canon1 = ((FluidKey) stack1.key()).canonical();
        GenericKey canon2 = ((FluidKey) stack2.key()).canonical();
        helper.assertTrue(canon1.equals(canon2), "canonical keys should be equal");
        helper.assertValueEqual(canon1.hashCode(), canon2.hashCode(),
                "canonical keys should have same hash");

        helper.succeed();
    }

    @GameTest(template = "empty", batch = "fluid_identity", timeoutTicks = 40)
    public static void differentEffectCombosAreNotIdentityEqual(GameTestHelper helper) {
        GenericStack stack1 = FluidGenericStack.wrap(mixedPotionStack(
                entry("minecraft:invisibility", 0, 600, 500, 600),
                entry("minecraft:instant_health", 0, 600, 500, 600)
        ));
        GenericStack stack2 = FluidGenericStack.wrap(mixedPotionStack(
                entry("minecraft:invisibility", 0, 600, 500, 600),
                entry("minecraft:speed", 0, 600, 500, 600)
        ));

        helper.assertFalse(stack1.canStack(stack2),
                "different effect combos must NOT be identity-equal");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "fluid_identity", timeoutTicks = 40)
    public static void vanillaFluidsPreserveExactMatch(GameTestHelper helper) {
        FluidStack water = new FluidStack(Fluids.WATER, 1000);
        FluidStack lava = new FluidStack(Fluids.LAVA, 1000);

        helper.assertTrue(FluidGenericStack.wrap(water).canStack(FluidGenericStack.wrap(water)),
                "identical vanilla fluids must stack");
        helper.assertFalse(FluidGenericStack.wrap(water).canStack(FluidGenericStack.wrap(lava)),
                "different vanilla fluids must not stack");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "fluid_identity", timeoutTicks = 40)
    public static void canonicalKeyIsMemoized(GameTestHelper helper) {
        GenericStack stack = FluidGenericStack.wrap(mixedPotionStack(
                entry("minecraft:invisibility", 0, 600, 500, 600)
        ));
        FluidKey key = (FluidKey) stack.key();

        helper.assertTrue(key.canonical() == key.canonical(),
                "canonical() should return same cached instance");
        helper.succeed();
    }

    private static FluidStack mixedPotionStack(Entry... composition) {
        CompoundTag mixed = new CompoundTag();
        mixed.putString("bottle_type", "regular");
        mixed.putInt("color", 0xFF00FF);
        mixed.putString("name", "IH");
        mixed.putInt("total_mb", 1000000);

        ListTag list = new ListTag();
        for (int i = 0; i < composition.length; i++) {
            CompoundTag e = new CompoundTag();
            e.putString("effect", composition[i].effect);
            e.putInt("amplifier", composition[i].amplifier);
            e.putLong("duration_ticks", composition[i].durationTicks);
            e.putInt("amount_mb", composition[i].amountMb);
            e.putLong("base_duration_ticks", composition[i].baseDurationTicks);
            e.putBoolean("capped", false);
            list.add(e);
        }
        mixed.put("composition", list);

        CompoundTag root = new CompoundTag();
        root.put("mixed_potion", mixed);

        PatchedDataComponentMap map = new PatchedDataComponentMap(DataComponentMap.EMPTY);
        map.set(DataComponents.CUSTOM_DATA, CustomData.of(root));
        return new FluidStack(Fluids.WATER.builtInRegistryHolder(), 1000, map.asPatch());
    }

    private static Entry entry(String effect, int amplifier, long durationTicks, int amountMb, long baseDurationTicks) {
        return new Entry(effect, amplifier, durationTicks, amountMb, baseDurationTicks);
    }

    private record Entry(String effect, int amplifier, long durationTicks, int amountMb, long baseDurationTicks) {}
}
