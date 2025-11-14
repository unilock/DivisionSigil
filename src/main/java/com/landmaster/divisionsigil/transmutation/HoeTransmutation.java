package com.landmaster.divisionsigil.transmutation;

import com.landmaster.divisionsigil.DivisionSigil;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;
import java.util.function.Function;

public interface HoeTransmutation {
    Optional<BlockState> newBlockState(ServerLevel level, BlockPos pos);

    MapCodec<? extends HoeTransmutation> type();

    Codec<HoeTransmutation> CODEC = DivisionSigil.HOE_TRANSMUTATIONS_DISPATCH.byNameCodec().dispatch(
            HoeTransmutation::type, Function.identity()
    );
}
