package ru.zznty.create_factory_logistics.gametest;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import ru.zznty.create_factory_abstractions.CreateFactoryAbstractions;
import ru.zznty.create_factory_abstractions.generic.impl.GenericContentExtender;
import ru.zznty.create_factory_logistics.CreateFactoryLogistics;
import ru.zznty.create_factory_logistics.FactoryBlocks;
import ru.zznty.create_factory_logistics.FactoryItems;
import ru.zznty.create_factory_logistics.compat.mekanism.FactoryMekanismBlocks;
import ru.zznty.create_factory_logistics.compat.mekanism.FactoryMekanismItems;
import ru.zznty.create_factory_logistics.logistics.networkLink.NetworkLinkQualificationRecipe;

@GameTestHolder(CreateFactoryLogistics.MODID)
@PrefixGameTestTemplate(false)
public final class BootstrapGameTests {
    @GameTest(template = "empty", batch = "bootstrap")
    public static void baseRegistriesAreAvailable(GameTestHelper helper) {
        helper.assertTrue(FactoryBlocks.JAR_PACKAGER.get() != null, "jar packager is not registered");
        helper.assertTrue(FactoryBlocks.FACTORY_FLUID_GAUGE.get() != null, "fluid gauge is not registered");
        helper.assertTrue(FactoryBlocks.NETWORK_LINK.get() != null, "network link is not registered");
        helper.assertTrue(FactoryItems.REGULAR_JAR.get() != null, "regular jar is not registered");
        helper.assertTrue(FactoryItems.COMPOSITE_PACKAGE.get() != null, "composite package is not registered");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "bootstrap")
    public static void genericTypesAreAvailable(GameTestHelper helper) {
        assertGenericType(helper, "create_factory_abstractions", "empty");
        assertGenericType(helper, "create_factory_abstractions", "item");
        assertGenericType(helper, CreateFactoryLogistics.MODID, "fluid");
        assertGenericType(helper, CreateFactoryLogistics.MODID, "chemical");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "bootstrap")
    public static void mekanismRegistriesAreAvailable(GameTestHelper helper) {
        helper.assertTrue(FactoryMekanismBlocks.BARREL_PACKAGER.get() != null, "barrel packager is not registered");
        helper.assertTrue(FactoryMekanismBlocks.FACTORY_CHEMICAL_GAUGE.get() != null, "chemical gauge is not registered");
        helper.assertTrue(FactoryMekanismItems.REGULAR_BARREL.get() != null, "chemical barrel is not registered");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "bootstrap")
    public static void networkLinkQualifierTagsArePopulated(GameTestHelper helper) {
        ResourceLocation emptyKey = ResourceLocation.fromNamespaceAndPath(CreateFactoryAbstractions.ID, "empty");
        for (ResourceLocation key : GenericContentExtender.REGISTRY.keySet()) {
            if (key.equals(emptyKey))
                continue;
            TagKey<Item> tag = NetworkLinkQualificationRecipe.tag(key);
            int count = 0;
            for (var holder : BuiltInRegistries.ITEM.getTagOrEmpty(tag))
                count++;
            helper.assertTrue(count > 0,
                    "network_link_qualifier/" + key.getNamespace() + "/" + key.getPath()
                            + " tag has " + count + " entries, expected > 0");
        }
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "bootstrap")
    public static void emptyQualifierTagIsIntentionallyEmpty(GameTestHelper helper) {
        ResourceLocation emptyKey = ResourceLocation.fromNamespaceAndPath(CreateFactoryAbstractions.ID, "empty");
        TagKey<Item> tag = NetworkLinkQualificationRecipe.tag(emptyKey);
        int count = 0;
        for (var holder : BuiltInRegistries.ITEM.getTagOrEmpty(tag))
            count++;
        helper.assertTrue(count == 0,
                "empty qualifier tag should be empty (it is a reset recipe, not a material match); got "
                        + count + " entries");
        helper.succeed();
    }

    private static void assertGenericType(GameTestHelper helper, String namespace, String path) {
        ResourceLocation key = ResourceLocation.fromNamespaceAndPath(namespace, path);
        helper.assertTrue(GenericContentExtender.REGISTRY.containsKey(key), "missing generic type " + key);
    }

    private BootstrapGameTests() {
    }
}
