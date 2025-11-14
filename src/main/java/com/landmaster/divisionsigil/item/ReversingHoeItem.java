package com.landmaster.divisionsigil.item;

import com.landmaster.divisionsigil.DivisionSigil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Tier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;

@EventBusSubscriber
public class ReversingHoeItem extends HoeItem {
    public ReversingHoeItem(Tier tier, Properties properties) {
        super(tier, properties);
    }

    @SubscribeEvent
    public static void onToolUse(BlockEvent.BlockToolModificationEvent event) {
        if (event.getLevel() instanceof ServerLevel level && event.getHeldItemStack().getItem() instanceof ReversingHoeItem) {
            level.registryAccess().registry(DivisionSigil.HOE_TRANSMUTATIONS_KEY).ifPresent(registry -> {
                for (var transmutation: registry) {
                    var state = transmutation.newBlockState(level, event.getPos());
                    if (state.isPresent()) {
                        if (event.getState() == state.get()) {
                            event.setCanceled(true);
                        } else {
                            event.setFinalState(state.get());
                        }
                        break;
                    }
                }
            });
        }
    }
}
