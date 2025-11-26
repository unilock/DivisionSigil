package com.landmaster.divisionsigil.stabilization_recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.level.block.entity.ChestBlockEntity;

import javax.annotation.Nonnull;
import java.util.Map;

public record StabilizationRitualRecipeInput(Map<DirectionalElement, ChestBlockEntity> chests) implements RecipeInput {
    @Nonnull
    @Override
    public ItemStack getItem(int i) {
        if (i < 0) {
            throw new IllegalArgumentException("index should be >= 0; got: %d".formatted(i));
        }
        if (i >= size()) {
            throw new IllegalArgumentException("index should be < %d; got: %d".formatted(size(), i));
        }
        var chest = chests.get(DirectionalElement.VALUES[i / 27]);
        if (chest != null) {
            return chest.getItem(i % 27);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public int size() {
        return 27 * DirectionalElement.VALUES.length;
    }
}
