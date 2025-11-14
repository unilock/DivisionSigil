package com.landmaster.divisionsigil.transmutation;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

import javax.annotation.Nonnull;

public record HoeTransmutationInput(BlockPos pos) implements RecipeInput {
    @Nonnull
    @Override
    public ItemStack getItem(int i) {
        throw new IllegalArgumentException("HoeTransmutationInput has no items");
    }

    @Override
    public int size() {
        return 0;
    }
}
