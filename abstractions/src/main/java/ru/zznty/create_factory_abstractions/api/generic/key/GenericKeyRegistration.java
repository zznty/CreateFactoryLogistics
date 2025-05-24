package ru.zznty.create_factory_abstractions.api.generic.key;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface GenericKeyRegistration {
    <K extends GenericKey> GenericKeyProvider<K> provider();

    <K extends GenericKey> GenericKeySerializer<K> serializer();

    @OnlyIn(Dist.CLIENT)
    <K extends GenericKey> GenericKeyClientProvider<K> clientProvider();
}
