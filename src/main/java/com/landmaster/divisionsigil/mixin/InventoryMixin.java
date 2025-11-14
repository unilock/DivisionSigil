package com.landmaster.divisionsigil.mixin;

import com.landmaster.divisionsigil.DivisionSigil;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Inventory.class)
public class InventoryMixin {
    @Shadow
    public NonNullList<ItemStack> items;

    @Inject(method = "findSlotMatchingUnusedItem(Lnet/minecraft/world/item/ItemStack;)I",
            at = @At(value = "INVOKE", target = "get(I)Ljava/lang/Object;", shift = At.Shift.AFTER), cancellable = true)
    private void injectFindSlotMatchingUnusedItem(ItemStack stack, CallbackInfoReturnable<Integer> callback, @Local int i) {
        if (stack.is(DivisionSigil.DIVISION_SIGIL) && items.get(i).is(DivisionSigil.DIVISION_SIGIL)) {
            callback.setReturnValue(i);
        }
    }
}
