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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CompositeRepackagerHelper extends PackageRepackageHelper {
    @Override
    public List<BigItemStack> repack(int orderId, RandomSource r) {
        List<BigItemStack> exportingPackages = super.repack(orderId, r);

        List<ItemStack> nonPackageItems = collectedPackages.get(orderId)
                .stream()
                .filter(box -> box.getItem().getClass() != PackageItem.class)
                .collect(Collectors.toList());

        // if its a single jar, dont make a composite package and instead a normal jar
        if (exportingPackages.isEmpty() && nonPackageItems.size() == 1) {
            List<BigItemStack> result = new ArrayList<>();
            result.add(new BigItemStack(nonPackageItems.get(0)));
            return result;
        }

        UnmodifiableIterator<List<ItemStack>> partitioned = Iterators.partition(
                nonPackageItems.iterator(),
                Iterate.horizontalDirections.length
        );

        int i = 0;
        while (partitioned.hasNext() && i < exportingPackages.size()) {
            List<ItemStack> additional = partitioned.next();
            ItemStack base = exportingPackages.get(i).stack;
            exportingPackages.set(i, new BigItemStack(CompositePackageItem.of(base, additional)));
            i++;
        }

        while (partitioned.hasNext()) {
            List<ItemStack> fluidGroup = partitioned.next();
            ItemStack dummy = ItemStack.EMPTY;
            exportingPackages.add(new BigItemStack(CompositePackageItem.of(dummy, fluidGroup)));
        }

        return exportingPackages;
    }
}
