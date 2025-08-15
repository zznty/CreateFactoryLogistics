package ru.zznty.create_factory_logistics;

import com.simibubi.create.api.registry.CreateBuiltInRegistries;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttributeType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import ru.zznty.create_factory_logistics.logistics.generic.FluidGenericAttribute;
import ru.zznty.create_factory_logistics.logistics.generic.FluidNoNbtGenericAttribute;
import ru.zznty.create_factory_logistics.logistics.generic.GenericAttributeType;

public class FactoryGenericAttributeTypes {
    public static final DeferredRegister<ItemAttributeType> REGISTER =
            DeferredRegister.create(CreateBuiltInRegistries.ITEM_ATTRIBUTE_TYPE.key(), CreateFactoryLogistics.MODID);

    public static final DeferredHolder<ItemAttributeType, GenericAttributeType>
            IS_FLUID = REGISTER.register("is_fluid", FluidGenericAttribute.Type::new),
            IS_FLUID_NO_NBT = REGISTER.register("is_fluid_no_nbt", FluidNoNbtGenericAttribute.Type::new);
}
