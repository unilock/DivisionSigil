package com.landmaster.divisionsigil.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.Direction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.random.Weight;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;

import javax.annotation.Nonnull;
import java.util.*;

public class Util {
    public static <B extends ByteBuf, T> StreamCodec<B, WeightedEntry.Wrapper<T>> weightedEntryStreamCodec(StreamCodec<B, T> dataCodec) {
        return StreamCodec.composite(
                dataCodec, WeightedEntry.Wrapper::data,
                ByteBufCodecs.INT.map(Weight::of, Weight::asInt), WeightedEntry.Wrapper::weight,
                WeightedEntry.Wrapper::new
        );
    }

    public static <B extends ByteBuf, E extends WeightedEntry> StreamCodec<B, WeightedRandomList<E>> weightedListStreamCodec(StreamCodec<B, E> entryCodec) {
        return ByteBufCodecs.<B, E, List<E>>collection(ArrayList::new, entryCodec)
                .map(WeightedRandomList::create, WeightedRandomList::unwrap);
    }

    public static final Map<Direction.Axis, List<Direction>> AXIS_TO_PLANE_DIRS = Maps.immutableEnumMap(ImmutableMap.of(
            Direction.Axis.X, ImmutableList.of(Direction.UP, Direction.NORTH, Direction.DOWN, Direction.SOUTH),
            Direction.Axis.Y, ImmutableList.of(Direction.NORTH, Direction.WEST, Direction.SOUTH, Direction.EAST),
            Direction.Axis.Z, ImmutableList.of(Direction.UP, Direction.WEST, Direction.DOWN, Direction.EAST)
    ));

    public static final List<Property<?>> ORIENTATION_PROPERTIES = ImmutableList.of(
            BlockStateProperties.FACING, BlockStateProperties.HORIZONTAL_FACING, BlockStateProperties.FACING_HOPPER,
            BlockStateProperties.AXIS, BlockStateProperties.HORIZONTAL_AXIS, BlockStateProperties.ORIENTATION,
            BlockStateProperties.VERTICAL_DIRECTION
    );
}
