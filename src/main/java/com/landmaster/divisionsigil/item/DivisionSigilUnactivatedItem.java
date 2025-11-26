package com.landmaster.divisionsigil.item;

import com.landmaster.divisionsigil.Config;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import javax.annotation.Nonnull;

public class DivisionSigilUnactivatedItem extends Item {
    public DivisionSigilUnactivatedItem(Properties properties) {
        super(properties);
    }

    @Nonnull
    @Override
    public InteractionResult onItemUseFirst(@Nonnull ItemStack stack, @Nonnull UseOnContext context) {
        if (Config.ENABLE_DIVISION_SIGIL_RITUAL.getAsBoolean()) {
            var level = context.getLevel();
            var pos = context.getClickedPos();
            var player = context.getPlayer();
            if (player != null
                    && level.getBlockState(pos).is(Blocks.ENCHANTING_TABLE)
                    && level.dimension() == Level.OVERWORLD) {
                if (!level.isClientSide()) {
                    boolean allConditionsFulfilled = true;
                    if (!DivisionSigilItem.checkTimeCondition(level)) {
                        player.displayClientMessage(Component.translatable("message.divisionsigil.midnight_condition"), false);
                        allConditionsFulfilled = false;
                    }
                    if (!level.canSeeSky(pos)) {
                        player.displayClientMessage(Component.translatable("message.divisionsigil.sky_condition"), false);
                        allConditionsFulfilled = false;
                    }
                    if (!DivisionSigilItem.checkRedstoneCondition(level, pos)) {
                        player.displayClientMessage(Component.translatable("message.divisionsigil.redstone_condition"), false);
                        allConditionsFulfilled = false;
                    }
                    if (!DivisionSigilItem.checkDirtCondition(level, pos)) {
                        player.displayClientMessage(Component.translatable("message.divisionsigil.dirt_condition"), false);
                        allConditionsFulfilled = false;
                    }
                    if (allConditionsFulfilled) {
                        player.displayClientMessage(Component.translatable("message.divisionsigil.kill_condition"), false);
                    }
                }
                return InteractionResult.SUCCESS_NO_ITEM_USED;
            }
        }
        return super.onItemUseFirst(stack, context);
    }
}
