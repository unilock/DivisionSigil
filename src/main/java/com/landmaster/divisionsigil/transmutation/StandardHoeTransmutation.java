package com.landmaster.divisionsigil.transmutation;

import com.landmaster.divisionsigil.DivisionSigil;
import com.landmaster.divisionsigil.util.Util;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;

import javax.annotation.Nonnull;
import java.util.Optional;

public record StandardHoeTransmutation(
        BlockPredicate inputBlock,
        HolderSet<Biome> biomeList,
        boolean isBiomeAllowlist,
        WeightedRandomList<WeightedEntry.Wrapper<BlockState>> outputBlock,
        int priority
) implements HoeTransmutation {
    public static final MapCodec<StandardHoeTransmutation> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    BlockPredicate.CODEC.fieldOf("input").forGetter(StandardHoeTransmutation::inputBlock),
                    RegistryCodecs.homogeneousList(Registries.BIOME).optionalFieldOf("biomes", HolderSet.empty()).forGetter(StandardHoeTransmutation::biomeList),
                    Codec.BOOL.optionalFieldOf("isBiomeAllowlist", true).forGetter(StandardHoeTransmutation::isBiomeAllowlist),
                    WeightedRandomList.codec(WeightedEntry.Wrapper.codec(BlockState.CODEC)).fieldOf("output").forGetter(StandardHoeTransmutation::outputBlock),
                    Codec.INT.optionalFieldOf("priority", 0).forGetter(StandardHoeTransmutation::priority)
            ).apply(instance, StandardHoeTransmutation::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, StandardHoeTransmutation> STREAM_CODEC = StreamCodec.composite(
            BlockPredicate.STREAM_CODEC, StandardHoeTransmutation::inputBlock,
            ByteBufCodecs.holderSet(Registries.BIOME), StandardHoeTransmutation::biomeList,
            ByteBufCodecs.BOOL, StandardHoeTransmutation::isBiomeAllowlist,
            Util.weightedListStreamCodec(Util.weightedEntryStreamCodec(ByteBufCodecs.idMapper(Block.BLOCK_STATE_REGISTRY))), StandardHoeTransmutation::outputBlock,
            ByteBufCodecs.INT, StandardHoeTransmutation::priority,
            StandardHoeTransmutation::new
    );

    @Override
    public Optional<BlockState> newBlockState(Level level, BlockPos pos) {
        if (matches(new HoeTransmutationInput(pos), level)) {
            return outputBlock.getRandom(level.random).map(WeightedEntry.Wrapper::data);
        }
        return Optional.empty();
    }

    @Override
    public boolean matches(HoeTransmutationInput hoeTransmutationInput, @Nonnull Level level) {
        var pos = hoeTransmutationInput.pos();
        return inputBlock.matches(new BlockInWorld(level, pos, false))
                && (biomeList.size() == 0 || (isBiomeAllowlist == biomeList.contains(level.getBiome(pos))));
    }

    @Nonnull
    @Override
    public RecipeSerializer<? extends StandardHoeTransmutation> getSerializer() {
        return DivisionSigil.STANDARD_HOE_TRANSMUTATION.get();
    }

    public static class Serializer implements RecipeSerializer<StandardHoeTransmutation> {

        @Nonnull
        @Override
        public MapCodec<StandardHoeTransmutation> codec() {
            return CODEC;
        }

        @Nonnull
        @Override
        public StreamCodec<RegistryFriendlyByteBuf, StandardHoeTransmutation> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
