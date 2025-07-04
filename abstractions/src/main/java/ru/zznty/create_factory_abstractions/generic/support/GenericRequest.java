package ru.zznty.create_factory_abstractions.generic.support;

import com.simibubi.create.content.logistics.packager.PackagingRequest;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableInt;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;

import javax.annotation.Nullable;

public record GenericRequest(GenericStack stack, MutableInt count, String address, int linkIndex,
                             MutableBoolean finalLink, MutableInt packageCounter, int orderId,
                             @Nullable GenericOrder context) {

    public static GenericRequest create(GenericStack stack, int count, String address, int linkIndex,
                                        MutableBoolean finalLink, int packageCount, int orderId,
                                        @Nullable GenericOrder context) {
        return new GenericRequest(stack, new MutableInt(count), address, linkIndex, finalLink,
                                  new MutableInt(packageCount), orderId, context);
    }

    public int getCount() {
        return count.intValue();
    }

    public void subtract(int toSubtract) {
        count.setValue(getCount() - toSubtract);
    }

    public boolean isEmpty() {
        return getCount() == 0;
    }

    public static GenericRequest from(PackagingRequest request) {
        return new GenericRequest(
                GenericStack.wrap(request.item()),
                request.count(),
                request.address(),
                request.linkIndex(),
                request.finalLink(),
                request.packageCounter(),
                request.orderId(),
                request.context() == null ? null : GenericOrder.of(request.context())
        );
    }
}
