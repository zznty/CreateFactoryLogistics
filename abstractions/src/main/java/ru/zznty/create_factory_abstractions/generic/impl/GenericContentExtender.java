package ru.zznty.create_factory_abstractions.generic.impl;

import net.createmod.catnip.data.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.NewRegistryEvent;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegistryBuilder;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import ru.zznty.create_factory_abstractions.api.generic.extensibility.*;
import ru.zznty.create_factory_abstractions.api.generic.key.*;
import ru.zznty.create_factory_abstractions.generic.key.EmptyKeyProvider;
import ru.zznty.create_factory_abstractions.generic.key.EmptyKeySerializer;
import ru.zznty.create_factory_abstractions.generic.key.item.ItemKey;
import ru.zznty.create_factory_abstractions.generic.key.item.ItemKeyClientProvider;
import ru.zznty.create_factory_abstractions.generic.key.item.ItemKeyProvider;
import ru.zznty.create_factory_abstractions.generic.key.item.ItemKeySerializer;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

@ApiStatus.Internal
public final class GenericContentExtender {
    private static final Map<String, GenericContentExtension> EXTENSIONS = new HashMap<>(); // <modId, extension>
    public static final String ID = "create_factory_abstractions";
    public static Supplier<IForgeRegistry<GenericKeyRegistration>> REGISTRY;
    public static Map<Class<?>, GenericKeyRegistration> REGISTRATIONS = new HashMap<>(); // <key, provider>

    public static void enqueueExtension(String modId, GenericContentExtension extension) {
        EXTENSIONS.putIfAbsent(modId, extension);
    }

    public static void register(IEventBus bus) {
        bus.register(GenericContentExtender.class);
    }

    @SubscribeEvent
    private static void onRegistry(NewRegistryEvent event) {
        REGISTRY = event.create(new RegistryBuilder<GenericKeyRegistration>()
                                        .setName(ResourceLocation.fromNamespaceAndPath(ID, "generic_keys"))
                                        .setDefaultKey(ResourceLocation.fromNamespaceAndPath(ID, "empty"))
                                        .disableSaving()
                                        .disableOverrides(),
                                GenericContentExtender::fillKeys);
    }

    private static void fillKeys(IForgeRegistry<GenericKeyRegistration> registry) {
        registry.register(ResourceLocation.fromNamespaceAndPath(ID, "empty"),
                          new Registration<>(new EmptyKeyProvider(), new EmptyKeySerializer(), null));
        Registration<ItemKey> itemKeyRegistration = new Registration<>(new ItemKeyProvider(), new ItemKeySerializer(),
                                                                       DistExecutor.safeCallWhenOn(Dist.CLIENT,
                                                                                                   () -> ItemKeyClientProvider::new));
        registry.register(ResourceLocation.fromNamespaceAndPath(ID, "item"),
                          itemKeyRegistration);
        REGISTRATIONS.put(ItemKey.class, itemKeyRegistration);
    }

    @SubscribeEvent
    private static void onRegisterExtensions(RegisterEvent event) {
        event.register(REGISTRY.get().getRegistryKey(), helper -> {
            for (Map.Entry<String, GenericContentExtension> entry : EXTENSIONS.entrySet()) {
                GenericContentExtension extension = entry.getValue();
                Map<String, Pair<CommonRegistrationBuilderImpl<?>, ClientRegistrationBuilderImpl<?>>> registrations = new HashMap<>();
                Map<String, Class<?>> keyClasses = new HashMap<>();
                extension.registerCommon(new CommonContentRegistration() {
                    @Override
                    public <Key extends GenericKey> void register(String id,
                                                                  Class<Key> keyClass,
                                                                  Consumer<CommonRegistrationBuilder<Key>> builder) {
                        CommonRegistrationBuilderImpl<Key> registrationBuilder = new CommonRegistrationBuilderImpl<>();
                        builder.accept(registrationBuilder);
                        registrations.putIfAbsent(id, Pair.of(registrationBuilder, null));
                        keyClasses.putIfAbsent(id, keyClass);
                    }
                });

                DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> () -> {
                    extension.registerClient(new ClientContentRegistration() {
                        @Override
                        public <Key extends GenericKey> void register(String id,
                                                                      Consumer<ClientRegistrationBuilder<Key>> builder) {
                            ClientRegistrationBuilderImpl<Key> registrationBuilder = new ClientRegistrationBuilderImpl<>();
                            builder.accept(registrationBuilder);
                            registrations.computeIfPresent(id,
                                                           (s, pair) -> Pair.of(pair.getFirst(), registrationBuilder));
                        }
                    });
                });

                for (Map.Entry<String, Pair<CommonRegistrationBuilderImpl<?>, ClientRegistrationBuilderImpl<?>>> pairEntry : registrations.entrySet()) {
                    Pair<CommonRegistrationBuilderImpl<?>, ClientRegistrationBuilderImpl<?>> pair = pairEntry.getValue();
                    Pair<? extends GenericKeyProvider<?>, ? extends GenericKeySerializer<?>> commonPair = pair.getFirst().build();
                    ClientRegistrationBuilderImpl<?> clientRegistrationBuilder = pair.getSecond();
                    //noinspection rawtypes,unchecked
                    GenericKeyRegistration registration = new Registration(commonPair.getFirst(),
                                                                           commonPair.getSecond(),
                                                                           clientRegistrationBuilder == null ?
                                                                           null
                                                                                                             :
                                                                           clientRegistrationBuilder.build());
                    helper.register(ResourceLocation.fromNamespaceAndPath(entry.getKey(), pairEntry.getKey()),
                                    registration);

                    Class<?> keyClass = keyClasses.get(pairEntry.getKey());
                    REGISTRATIONS.put(keyClass, registration);
                }
            }
        });
    }

    private record Registration<Key extends GenericKey>(GenericKeyProvider<Key> keyProvider,
                                                        GenericKeySerializer<Key> keySerializer,
                                                        @Nullable GenericKeyClientProvider<Key> clientKeyProvider) implements GenericKeyRegistration {

        @Override
        public <K extends GenericKey> GenericKeyProvider<K> provider() {
            //noinspection unchecked
            return (GenericKeyProvider<K>) keyProvider;
        }

        @Override
        public <K extends GenericKey> GenericKeySerializer<K> serializer() {
            //noinspection unchecked
            return (GenericKeySerializer<K>) keySerializer;
        }

        @OnlyIn(Dist.CLIENT)
        @Override
        public <K extends GenericKey> GenericKeyClientProvider<K> clientProvider() {
            //noinspection unchecked
            return (GenericKeyClientProvider<K>) clientKeyProvider;
        }
    }
}
