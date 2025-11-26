package com.landmaster.divisionsigil.item;

import com.landmaster.divisionsigil.DivisionSigil;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Consumer;

public class DestructionPickaxeItem extends PickaxeItem {
    public DestructionPickaxeItem(Tier p_42961_, Properties p_42964_) {
        super(p_42961_, p_42964_);
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
}
