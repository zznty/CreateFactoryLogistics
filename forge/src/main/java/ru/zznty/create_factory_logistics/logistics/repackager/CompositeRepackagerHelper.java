package ru.zznty.create_factory_logistics.logistics.repackager;

import com.google.common.collect.Iterators;
import com.google.common.collect.UnmodifiableIterator;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.packager.repackager.PackageRepackageHelper;
import com.simibubi.create.content.logistics.stockTicker.PackageOrderWithCrafts;
import net.createmod.catnip.data.Iterate;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import ru.zznty.create_factory_logistics.logistics.composite.CompositePackageItem;

import java.util.ArrayList;
import java.util.List;

public class CompositeRepackagerHelper extends PackageRepackageHelper {
    @Override
    public List<BigItemStack> repack(int orderId, RandomSource r) {
        List<BigItemStack> exportingPackages = super.repack(orderId, r);

        List<ItemStack> nonItemPackages = collectedPackages.get(orderId)
                .stream().filter(box -> box.getItem().getClass() != PackageItem.class).toList();

        // fluid-only orders produce no item packages, so the jars would be voided by the repackager
        if (exportingPackages.isEmpty()) return repackWithoutItems(orderId, nonItemPackages);

        UnmodifiableIterator<List<ItemStack>> partitioned = Iterators.partition(nonItemPackages.iterator(),
                Iterate.horizontalDirections.length);

        int i = 0;
        while (partitioned.hasNext() && i < exportingPackages.size()) {
            exportingPackages.set(i, new BigItemStack(CompositePackageItem.of(exportingPackages.get(i).stack, partitioned.next().stream().toList())));
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

    private List<BigItemStack> repackWithoutItems(int orderId, List<ItemStack> nonItemPackages) {
        List<BigItemStack> exportingPackages = new ArrayList<>();
        if (nonItemPackages.isEmpty()) return exportingPackages;

        String address = "";
        PackageOrderWithCrafts orderContext = null;
        for (ItemStack box : collectedPackages.get(orderId)) {
            address = PackageItem.getAddress(box);
            if (box.hasTag() && box.getTag().getCompound("Fragment").contains("OrderContext"))
                orderContext = PackageItem.getOrderContext(box);
        }

        if (nonItemPackages.size() == 1) {
            ItemStack box = nonItemPackages.get(0).copy();
            box.getOrCreateTag().remove("Fragment");
            exportingPackages.add(new BigItemStack(box));
        } else {
            UnmodifiableIterator<List<ItemStack>> partitioned = Iterators.partition(nonItemPackages.iterator(),
                    Iterate.horizontalDirections.length);
            while (partitioned.hasNext())
                exportingPackages.add(new BigItemStack(CompositePackageItem.of(ItemStack.EMPTY, partitioned.next().stream().toList())));
        }

        for (int i = 0; i < exportingPackages.size(); i++) {
            ItemStack box = exportingPackages.get(i).stack;
            PackageItem.addAddress(box, address);
            boolean isFinal = i == exportingPackages.size() - 1;
            PackageItem.setOrder(box, orderId, 0, true, 0, true, isFinal ? orderContext : null);
        }

        return exportingPackages;
    }
}
