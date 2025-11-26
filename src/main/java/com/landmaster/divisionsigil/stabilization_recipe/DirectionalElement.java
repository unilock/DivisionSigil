package com.landmaster.divisionsigil.stabilization_recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Keyable;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;

import javax.annotation.Nonnull;
import java.util.Locale;

public enum DirectionalElement implements StringRepresentable {
    FIRE(Direction.NORTH), EARTH(Direction.SOUTH), WATER(Direction.EAST), AIR(Direction.WEST);

    public static final DirectionalElement[] VALUES = DirectionalElement.values();

    public static final Codec<DirectionalElement> CODEC = StringRepresentable.fromEnum(DirectionalElement::values);

    public static final StreamCodec<FriendlyByteBuf, DirectionalElement> STREAM_CODEC = NeoForgeStreamCodecs.enumCodec(DirectionalElement.class);

    public static final Keyable KEYABLE = StringRepresentable.keys(VALUES);

    public final Direction direction;

    DirectionalElement(Direction direction) {
        this.direction = direction;
    }

    @Nonnull
    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.US);
    }

    public String getTranslateKey() {
        return String.format("gui.divisionsigil.%s_chest", name().toLowerCase(Locale.US));
    }
}
