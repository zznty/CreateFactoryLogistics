package ru.zznty.create_factory_logistics.data;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.kinetics.deployer.DeployerApplicationRecipe;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipeBuilder;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Items;
import ru.zznty.create_factory_logistics.CreateFactoryLogistics;
import ru.zznty.create_factory_logistics.FactoryItems;

import java.util.function.UnaryOperator;

public class SequencedAssemblyRecipeGen extends FactoryRecipeProvider {
    GeneratedRecipe FLUID_MECHANISM = create("fluid_mechanism", b -> b.require(AllItems.COPPER_SHEET)
            .transitionTo(FactoryItems.INCOMPLETE_FLUID_MECHANISM)
            .addOutput(FactoryItems.FLUID_MECHANISM, 250)
            .addOutput(AllItems.COPPER_SHEET, 8)
            .addOutput(AllItems.ANDESITE_ALLOY, 8)
            .addOutput(AllBlocks.COGWHEEL, 5)
            .addOutput(Items.GOLD_NUGGET, 2)
            .addOutput(AllBlocks.SHAFT, 2)
            .addOutput(AllItems.CRUSHED_COPPER, 2)
            .addOutput(AllBlocks.MECHANICAL_PUMP, 1)
            .loops(3)
            .addStep(DeployerApplicationRecipe::new, rb -> rb.require(AllBlocks.COGWHEEL))
            .addStep(DeployerApplicationRecipe::new, rb -> rb.require(AllBlocks.FLUID_PIPE))
            .addStep(DeployerApplicationRecipe::new, rb -> rb.require(Items.GOLD_NUGGET)));

    public SequencedAssemblyRecipeGen(PackOutput output) {
        super(output);
    }

    protected GeneratedRecipe create(String name, UnaryOperator<SequencedAssemblyRecipeBuilder> transform) {
        GeneratedRecipe generatedRecipe =
                c -> transform.apply(new SequencedAssemblyRecipeBuilder(CreateFactoryLogistics.resource(name)))
                        .build(c);
        all.add(generatedRecipe);
        return generatedRecipe;
    }

    @Override
    public String getName() {
        return "Create Factory Logistics Sequenced Assembly Recipes";
    }
}
