package com.landmaster.divisionsigil.item;

import com.landmaster.divisionsigil.Config;
import com.landmaster.divisionsigil.DivisionSigil;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.text.DecimalFormat;
import java.util.Optional;

@EventBusSubscriber
public class UnstableIngotItem extends Item {
    public UnstableIngotItem(Properties properties) {
        super(properties);
    }


    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        instabilityCounter(event.getItemStack(), event.getContext().level()).ifPresent((counter) -> {
            event.getToolTip().add(Component.translatable("tooltip.divisionsigil.instability_counter",
                    (new DecimalFormat("0.0")).format(Math.max((Config.EXPLODE_TIME.getAsInt() - counter) / 20.0, 0))));
        });
    }

    public static Optional<Long> instabilityCounter(ItemStack stack, Level level) {
        var timestamp = stack.get(DivisionSigil.INSTABILITY_TIMESTAMP);
        if (timestamp == null || level == null) {
            return Optional.empty();
        }
        return Optional.of(level.getGameTime() - timestamp);
    }
}
