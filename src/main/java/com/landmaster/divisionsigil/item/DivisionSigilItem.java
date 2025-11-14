package com.landmaster.divisionsigil.item;

import com.landmaster.divisionsigil.DivisionSigil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

import javax.annotation.Nonnull;

@EventBusSubscriber(modid = DivisionSigil.MODID)
public class DivisionSigilItem extends Item {
    public DivisionSigilItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean hasCraftingRemainingItem(@Nonnull ItemStack stack) {
        return true;
    }

    @Nonnull
    @Override
    public ItemStack getCraftingRemainingItem(@Nonnull ItemStack itemStack) {
        if (itemStack.getDamageValue() + 1 >= itemStack.getMaxDamage()) {
            return DivisionSigil.DIVISION_SIGIL_UNACTIVATED.toStack();
        }

        var result = itemStack.copy();
        result.setDamageValue(Math.min(itemStack.getDamageValue() + 1, itemStack.getMaxDamage()));
        return result;
    }

    protected static boolean checkTimeCondition(Level level) {
        return Math.abs(level.getDayTime() - 18000) < 500;
    }

    protected static boolean checkRedstoneCondition(Level level, BlockPos pos) {
        return BlockPos.betweenClosedStream(pos.offset(-1, 0, -1), pos.offset(1, 0, 1))
                .allMatch(redstonePos -> redstonePos.equals(pos) || level.getBlockState(redstonePos).is(Blocks.REDSTONE_WIRE));
    }

    protected static boolean checkDirtCondition(Level level, BlockPos pos) {
        return BlockPos.betweenClosedStream(pos.offset(-2, -1, -2), pos.offset(2, -1, 2))
                .allMatch(dirtPos -> level.getBlockState(dirtPos).is(BlockTags.DIRT));
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (event.getSource().getEntity() instanceof Player player && event.getEntity() instanceof Mob mob) {
            var inventory = player.getInventory();
            var level = event.getEntity().level();
            if (!level.isClientSide()
                    && level.dimension() == Level.OVERWORLD
                    && checkTimeCondition(level)) {
                BlockPos.betweenClosedStream(AABB.ofSize(mob.position(), 5, 5, 5)).forEach(pos -> {
                    if (level.getBlockState(pos).is(Blocks.ENCHANTING_TABLE)
                        && level.canSeeSky(pos)) {
                        if (checkRedstoneCondition(level, pos) && checkDirtCondition(level, pos)) {
                            int sigilsActivated = 0;
                            for (int i = 0; i < inventory.getContainerSize(); ++i) {
                                if (inventory.getItem(i).getItem() == DivisionSigil.DIVISION_SIGIL_UNACTIVATED.get()) {
                                    inventory.setItem(i, DivisionSigil.DIVISION_SIGIL.toStack());
                                    ++sigilsActivated;
                                }
                            }
                            if (sigilsActivated > 0) {
                                player.displayClientMessage(Component.translatable("message.divisionsigil.sigils_activated", sigilsActivated), false);
                                for (var transmutePos: BlockPos.betweenClosed(pos.offset(-7, -7, -7),
                                        pos.offset(7, 7, 7))) {
                                    if (pos.distSqr(transmutePos) <= 7*7 && level.getBlockState(transmutePos).is(BlockTags.DIRT)
                                    && level.canSeeSky(transmutePos.above())) {
                                        level.setBlock(transmutePos, DivisionSigil.CURSED_EARTH.get().defaultBlockState(), 11);
                                    }
                                }
                                inventory.setChanged();
                            }
                        }
                    }
                });
            }
        }
    }
}
