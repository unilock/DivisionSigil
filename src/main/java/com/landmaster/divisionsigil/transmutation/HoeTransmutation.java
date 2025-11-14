package com.landmaster.divisionsigil.transmutation;

import com.landmaster.divisionsigil.DivisionSigil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import java.util.Optional;

public interface HoeTransmutation extends Comparable<HoeTransmutation>, Recipe<HoeTransmutationInput> {
    Optional<BlockState> newBlockState(Level level, BlockPos pos);

    int priority();

    @Override
    default int compareTo(@Nonnull HoeTransmutation o) {
        return Integer.compare(priority(), o.priority());
    }

    @Nonnull
    @Override
    default RecipeType<? extends Recipe<HoeTransmutationInput>> getType() {
        return DivisionSigil.HOE_TRANSMUTATION_TYPE.get();
    }

    @Override
    default boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Nonnull
    @Override
    default ItemStack assemble(@Nonnull HoeTransmutationInput hoeTransmutationInput, @Nonnull HolderLookup.Provider provider) {
        return ItemStack.EMPTY;
    }

    @Nonnull
    @Override
    default ItemStack getResultItem(@Nonnull HolderLookup.Provider provider) {
        return ItemStack.EMPTY;
    }
}
