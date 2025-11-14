package com.landmaster.divisionsigil.transmutation;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

public class CropRevertTransmutation implements HoeTransmutation {
    public static final MapCodec<CropRevertTransmutation> CODEC = MapCodec.unit(new CropRevertTransmutation());

    @Override
    public Optional<BlockState> newBlockState(ServerLevel level, BlockPos pos) {
        var blockState = level.getBlockState(pos);
        if (blockState.getBlock() instanceof CropBlock cropBlock) {
            return Optional.of(cropBlock.getStateForAge(Math.max(cropBlock.getAge(blockState) - 1, 0)));
        }
        return Optional.empty();
    }

    @Override
    public MapCodec<? extends HoeTransmutation> type() {
        return CODEC;
    }
}
