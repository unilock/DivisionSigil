package com.landmaster.divisionsigil.stabilization_recipe;

import com.landmaster.divisionsigil.DivisionSigil;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

public interface StabilizationRitualRecipe extends Recipe<StabilizationRitualRecipeInput> {
    int amountMatched(StabilizationRitualRecipeInput stabilizationRitualRecipeInput, DirectionalElement element);

    int matchRequirement(DirectionalElement element);

    @Override
    default boolean matches(@Nonnull StabilizationRitualRecipeInput stabilizationRitualRecipeInput, @Nonnull Level level) {
        for (var element: DirectionalElement.VALUES) {
            if (amountMatched(stabilizationRitualRecipeInput, element) < matchRequirement(element)) {
                return false;
            }
        }
        return true;
    }

    @Override
    default boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Nonnull
    @Override
    default ItemStack assemble(@Nonnull StabilizationRitualRecipeInput stabilizationRitualRecipeInput, @Nonnull HolderLookup.Provider provider) {
        return DivisionSigil.PSEUDO_INVERSION_SIGIL.toStack();
    }

    @Nonnull
    @Override
    default ItemStack getResultItem(@Nonnull HolderLookup.Provider provider) {
        return DivisionSigil.PSEUDO_INVERSION_SIGIL.toStack();
    }

    @Nonnull
    @Override
    default RecipeType<?> getType() {
        return DivisionSigil.STABILIZATION_RITUAL_TYPE.get();
    }
}
