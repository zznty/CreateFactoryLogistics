package ru.zznty.create_factory_abstractions.generic.support;

import com.simibubi.create.content.logistics.packager.IdentifiedInventory;
import com.simibubi.create.content.logistics.packager.PackagerBlockEntity;
import net.createmod.catnip.data.Pair;
import org.apache.commons.lang3.mutable.MutableBoolean;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;

import javax.annotation.Nullable;

public interface GenericPackagerLinkBlockEntity {
    Pair<PackagerBlockEntity, GenericRequest> processRequest(GenericStack stack, String address,
                                                             int linkIndex, MutableBoolean finalLink, int orderId,
                                                             @Nullable GenericOrder orderContext,
                                                             @Nullable IdentifiedInventory ignoredHandler);
}
