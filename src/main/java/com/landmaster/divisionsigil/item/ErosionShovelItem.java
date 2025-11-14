package com.landmaster.divisionsigil.item;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.block.FallingBlock;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;

@EventBusSubscriber
public class ErosionShovelItem extends ShovelItem {
    public ErosionShovelItem(Tier tier, Properties properties) {
        super(tier, properties);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player
                && player.getMainHandItem().getItem() instanceof ErosionShovelItem
                && event.getState().getBlock() instanceof FallingBlock) {
            var upperPos = event.getPos().above();
            var upperBlockState = event.getLevel().getBlockState(upperPos);
            if (upperBlockState.getBlock() == event.getState().getBlock()) {
                player.gameMode.destroyBlock(upperPos);
            }
        }
    }
}
