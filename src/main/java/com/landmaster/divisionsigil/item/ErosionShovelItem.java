package com.landmaster.divisionsigil.item;

import com.landmaster.divisionsigil.DivisionSigil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.FallingBlock;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Consumer;

@EventBusSubscriber
public class ErosionShovelItem extends ShovelItem {
    public ErosionShovelItem(Tier tier, Properties properties) {
        super(tier, properties);
    }

    @Override
    public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, @Nullable T entity, @Nonnull Consumer<Item> onBroken) {
        return stack.getOrDefault(DivisionSigil.UNBREAKABLE, false) ? 0 : super.damageItem(stack, amount, entity, onBroken);
    }

    @Override
    public void appendHoverText(@Nonnull ItemStack stack, @Nonnull TooltipContext context, @Nonnull List<Component> tooltipComponents, @Nonnull TooltipFlag tooltipFlag) {
        if (stack.getOrDefault(DivisionSigil.UNBREAKABLE, false)) {
            tooltipComponents.add(Component.translatable("tooltip.divisionsigil.unbreakable"));
        }
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
