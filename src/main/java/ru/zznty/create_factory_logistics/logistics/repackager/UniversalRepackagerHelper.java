package ru.zznty.create_factory_logistics.logistics.repackager;

import com.google.common.collect.Iterators;
import com.google.common.collect.UnmodifiableIterator;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.packager.repackager.PackageRepackageHelper;
import net.createmod.catnip.data.Iterate;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import ru.zznty.create_factory_logistics.logistics.composite.CompositePackageItem;

import java.util.List;

public class UniversalRepackagerHelper extends PackageRepackageHelper {
    @Override
    public List<BigItemStack> repack(int orderId, RandomSource r) {
        List<BigItemStack> exportingPackages = super.repack(orderId, r);
        if (exportingPackages.isEmpty()) return exportingPackages;

        UnmodifiableIterator<List<ItemStack>> partitioned = Iterators.partition(collectedPackages.get(orderId)
                        .stream().filter(box -> box.getItem().getClass() != PackageItem.class).iterator(),
                Iterate.horizontalDirections.length);

        int i = 0;
        while (partitioned.hasNext() && i < exportingPackages.size()) {
            exportingPackages.set(i, new BigItemStack(CompositePackageItem.of(exportingPackages.get(i).stack, partitioned.next())));
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
}
