package com.landmaster.divisionsigil;

import com.landmaster.divisionsigil.item.UnstableIngotItem;
import net.minecraft.client.Minecraft;
import net.minecraft.util.FastColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = DivisionSigil.MODID, dist = Dist.CLIENT)
@EventBusSubscriber
public class DivisionSigilClient {
    public DivisionSigilClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    public static void registerBlockColorHandlers(RegisterColorHandlersEvent.Block event) {
        event.register((state, level, pos, tintIndex) -> 0xFF220022, DivisionSigil.CURSED_EARTH.get());
    }

    @SubscribeEvent
    public static void registerItemColorHandlers(RegisterColorHandlersEvent.Item event) {
        event.register((stack, index) -> 0xFF220022, DivisionSigil.CURSED_EARTH_ITEM);
        event.register((stack, index) -> {
            var clientLevel = Minecraft.getInstance().level;
            var redness = Math.clamp(
                    UnstableIngotItem.instabilityCounter(stack, clientLevel).orElse(0L) * 255 / Config.EXPLODE_TIME.getAsInt(), 0, 255);
            return FastColor.ARGB32.color(255, 255 - redness, 255 - redness);
        }, DivisionSigil.UNSTABLE_INGOT);
    }
}
