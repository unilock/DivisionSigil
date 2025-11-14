package com.landmaster.divisionsigil.item;

import com.landmaster.divisionsigil.DivisionSigil;
import com.landmaster.divisionsigil.transmutation.HoeTransmutationInput;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;

import java.util.Comparator;

@EventBusSubscriber
public class ReversingHoeItem extends HoeItem {
    public ReversingHoeItem(Tier tier, Properties properties) {
        super(tier, properties);
    }

    @SubscribeEvent
    public static void onToolUse(BlockEvent.BlockToolModificationEvent event) {
        if (event.getLevel() instanceof ServerLevel level && event.getHeldItemStack().getItem() instanceof ReversingHoeItem) {
            var recipes = level.getRecipeManager().getRecipesFor(
                    DivisionSigil.HOE_TRANSMUTATION_TYPE.get(), new HoeTransmutationInput(event.getPos()), level);
            var it = recipes
                    .stream()
                    .map(RecipeHolder::value)
                    .sorted(Comparator.reverseOrder())
                    .iterator();
            while (it.hasNext()) {
                var transmutation = it.next();
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
        }
    }
}
