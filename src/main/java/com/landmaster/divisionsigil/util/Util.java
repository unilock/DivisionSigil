package com.landmaster.divisionsigil.util;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.random.Weight;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.random.WeightedRandomList;

import java.util.ArrayList;
import java.util.List;

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
}
