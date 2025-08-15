package ru.zznty.create_factory_logistics;

import com.simibubi.create.api.registry.CreateBuiltInRegistries;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttributeType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import ru.zznty.create_factory_logistics.logistics.generic.FluidGenericAttribute;
import ru.zznty.create_factory_logistics.logistics.generic.FluidNoNbtGenericAttribute;

public class FactoryGenericAttributeTypes {
    public static final DeferredRegister<ItemAttributeType> REGISTER =
            DeferredRegister.create(CreateBuiltInRegistries.ITEM_ATTRIBUTE_TYPE.key(), CreateFactoryLogistics.MODID);

    public static final RegistryObject<ItemAttributeType>
            IS_FLUID = REGISTER.register("is_fluid", FluidGenericAttribute.Type::new),
            IS_FLUID_NO_NBT = REGISTER.register("is_fluid_no_nbt", FluidNoNbtGenericAttribute.Type::new);
}
