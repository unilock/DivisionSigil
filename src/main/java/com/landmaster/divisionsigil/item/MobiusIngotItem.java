package com.landmaster.divisionsigil.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class MobiusIngotItem extends Item {
    public MobiusIngotItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isFoil(@Nonnull ItemStack stack) {
        return true;
    }
}
