package com.landmaster.divisionsigil.mixin;

import com.landmaster.divisionsigil.DivisionSigil;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StackedContents.class)
public class StackedContentsMixin {
    @Shadow
    public void accountStack(ItemStack stack) {}

    @Inject(method = "accountSimpleStack(Lnet/minecraft/world/item/ItemStack;)V", at = @At("HEAD"), cancellable = true)
    public void injectAccountSimpleStack(ItemStack stack, CallbackInfo callbackInfo) {
        if (stack.is(DivisionSigil.DIVISION_SIGIL)) {
            accountStack(stack);
            callbackInfo.cancel();
        }
    }
}
