package com.landmaster.divisionsigil.stabilization_recipe;

import com.landmaster.divisionsigil.DivisionSigil;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import java.util.*;

public record StandardStabilizationRitualRecipe(
        Map<DirectionalElement, List<Ingredient>> ingredients,
        Map<DirectionalElement, Integer> requirements
) implements StabilizationRitualRecipe {
    public static final MapCodec<StandardStabilizationRitualRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Codec.simpleMap(DirectionalElement.CODEC, Ingredient.LIST_CODEC_NONEMPTY, DirectionalElement.KEYABLE)
                            .fieldOf("ingredients").forGetter(StandardStabilizationRitualRecipe::ingredients),
                    Codec.simpleMap(DirectionalElement.CODEC, Codec.INT, DirectionalElement.KEYABLE)
                            .fieldOf("requirements").forGetter(StandardStabilizationRitualRecipe::requirements)
            ).apply(instance, StandardStabilizationRitualRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, StandardStabilizationRitualRecipe> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.map(cap -> new EnumMap<>(DirectionalElement.class),
                    DirectionalElement.STREAM_CODEC,
                    ByteBufCodecs.collection(ArrayList::new, Ingredient.CONTENTS_STREAM_CODEC)),
            StandardStabilizationRitualRecipe::ingredients,
            ByteBufCodecs.map(cap -> new EnumMap<>(DirectionalElement.class), DirectionalElement.STREAM_CODEC, ByteBufCodecs.VAR_INT),
            StandardStabilizationRitualRecipe::requirements,
            StandardStabilizationRitualRecipe::new
    );

    @Nonnull
    @Override
    public RecipeSerializer<? extends StandardStabilizationRitualRecipe> getSerializer() {
        return DivisionSigil.STANDARD_STABILIZATION_RITUAL.get();
    }

    @Override
    public int amountMatched(StabilizationRitualRecipeInput stabilizationRitualRecipeInput, DirectionalElement element) {
        var chest = stabilizationRitualRecipeInput.chests().get(element);
        if (chest == null) {
            return 0;
        }
        var subIngredients = new LinkedList<>(ingredients.get(element));
        int count = 0;
        for (int i=0; i<chest.getContainerSize(); ++i) {
            int subCount = 0;
            var it = subIngredients.iterator();
            var stack = chest.getItem(i);
            while (stack.getCount() > subCount && it.hasNext()) {
                var ingredient = it.next();
                if (ingredient.test(stack)) {
                    ++subCount;
                    it.remove();
                }
            }
            count += subCount;
        }
        return count;
    }

    @Override
    public int matchRequirement(DirectionalElement element) {
        return requirements.get(element);
    }

    public static class Serializer implements RecipeSerializer<StandardStabilizationRitualRecipe> {

        @Nonnull
        @Override
        public MapCodec<StandardStabilizationRitualRecipe> codec() {
            return CODEC;
        }

        @Nonnull
        @Override
        public StreamCodec<RegistryFriendlyByteBuf, StandardStabilizationRitualRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
