package com.landmaster.divisionsigil.mixin;

import com.landmaster.divisionsigil.DivisionSigil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FireBlock.class)
public class FireBlockMixin {
    @Inject(method = "checkBurnOut", at = @At(value = "INVOKE", target = "onCaughtFire", shift = At.Shift.AFTER), cancellable = true)
    private void injectCheckBurnOut(Level level, BlockPos pos, int chance, RandomSource random, int age, Direction face, CallbackInfo info) {
        var blockState = level.getBlockState(pos);
        if (blockState.getBlock() == DivisionSigil.CURSED_EARTH.get()) {
            level.setBlock(pos, Blocks.DIRT.defaultBlockState(), 11);
            info.cancel();
        }
    }
}
