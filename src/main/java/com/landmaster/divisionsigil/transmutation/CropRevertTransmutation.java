package com.landmaster.divisionsigil.transmutation;

import com.landmaster.divisionsigil.DivisionSigil;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import java.util.Optional;

public class CropRevertTransmutation implements HoeTransmutation {
    public static final CropRevertTransmutation INSTANCE = new CropRevertTransmutation();

    public static final MapCodec<CropRevertTransmutation> CODEC = MapCodec.unit(INSTANCE);
    public static final StreamCodec<RegistryFriendlyByteBuf, CropRevertTransmutation> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    protected CropRevertTransmutation() {}

    @Override
    public Optional<BlockState> newBlockState(Level level, BlockPos pos) {
        var blockState = level.getBlockState(pos);
        if (blockState.getBlock() instanceof CropBlock cropBlock) {
            return Optional.of(cropBlock.getStateForAge(Math.max(cropBlock.getAge(blockState) - 1, 0)));
        }
        return Optional.empty();
    }

    @Override
    public int priority() {
        return 0;
    }

    @Override
    public boolean matches(HoeTransmutationInput hoeTransmutationInput, Level level) {
        return level.getBlockState(hoeTransmutationInput.pos()).getBlock() instanceof CropBlock;
    }

    @Nonnull
    @Override
    public RecipeSerializer<? extends CropRevertTransmutation> getSerializer() {
        return DivisionSigil.CROP_REVERT.get();
    }

    public static class Serializer implements RecipeSerializer<CropRevertTransmutation> {

        @Nonnull
        @Override
        public MapCodec<CropRevertTransmutation> codec() {
            return CODEC;
        }

        @Nonnull
        @Override
        public StreamCodec<RegistryFriendlyByteBuf, CropRevertTransmutation> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
