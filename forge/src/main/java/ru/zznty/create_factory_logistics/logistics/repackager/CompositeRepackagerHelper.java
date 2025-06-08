package ru.zznty.create_factory_logistics.logistics.repackager;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.UnmodifiableIterator;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.packager.repackager.PackageRepackageHelper;
import com.simibubi.create.content.logistics.packager.repackager.RepackagerBlockEntity;
import net.createmod.catnip.data.Iterate;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import ru.zznty.create_factory_logistics.logistics.composite.CompositePackageItem;

import java.util.List;

public class CompositeRepackagerHelper extends PackageRepackageHelper {
    private final RepackagerBlockEntity blockEntity;

    public CompositeRepackagerHelper(RepackagerBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    @Override
    public List<BigItemStack> repack(int orderId, RandomSource r) {
        List<BigItemStack> exportingPackages = super.repack(orderId, r);
        if (exportingPackages.isEmpty()) return exportingPackages;

        UnmodifiableIterator<List<ItemStack>> partitioned = Iterators.partition(collectedPackages.get(orderId)
                        .stream().filter(box -> box.getItem().getClass() != PackageItem.class).iterator(),
                Iterate.horizontalDirections.length);

        int i = 0;
        while (partitioned.hasNext() && i < exportingPackages.size()) {
            exportingPackages.set(i, new BigItemStack(CompositePackageItem.of(blockEntity.getLevel().registryAccess(),
                    exportingPackages.get(i).stack, partitioned.next().stream().toList())));
            i++;
        }

        if (partitioned.hasNext())
            partitioned.forEachRemaining(list -> {
                for (ItemStack box : list) {
                    exportingPackages.add(new BigItemStack(box));
                }
            });

        return exportingPackages;
    }

    @Override
    public boolean isFragmented(ItemStack box) {
        return box.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).contains("Fragment");
    }

    @Override
    public int addPackageFragment(ItemStack box) {
        int collectedOrderId = PackageItem.getOrderId(box);
        if (collectedOrderId == -1)
            return -1;

        List<ItemStack> collectedOrder = collectedPackages.computeIfAbsent(collectedOrderId, $ -> Lists.newArrayList());
        collectedOrder.add(box);

        if (!isOrderComplete(collectedOrderId))
            return -1;

        return collectedOrderId;
    }

    private boolean isOrderComplete(int orderId) {
        boolean finalLinkReached = false;
        Links:
        for (int linkCounter = 0; linkCounter < 1000; linkCounter++) {
            if (finalLinkReached)
                break;
            Packages:
            for (int packageCounter = 0; packageCounter < 1000; packageCounter++) {
                for (ItemStack box : collectedPackages.get(orderId)) {
                    //noinspection deprecation
                    CompoundTag fragment = box.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).getUnsafe().getCompound("Fragment");
                    if (linkCounter != fragment.getInt("LinkIndex"))
                        continue;
                    if (packageCounter != fragment.getInt("Index"))
                        continue;
                    finalLinkReached = fragment.getBoolean("IsFinalLink");
                    if (fragment.getBoolean("IsFinal"))
                        continue Links;
                    continue Packages;
                }
                return false;
            }
        }
        return true;
    }
}
