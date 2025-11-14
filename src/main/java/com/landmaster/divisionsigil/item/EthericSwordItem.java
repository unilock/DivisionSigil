package com.landmaster.divisionsigil.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;

import javax.annotation.Nonnull;

public class EthericSwordItem extends SwordItem {
    public EthericSwordItem(Tier tier, Properties properties) {
        super(tier, properties);
    }

    @Override
    public void postHurtEnemy(@Nonnull  ItemStack stack, @Nonnull LivingEntity target, @Nonnull LivingEntity attacker) {
        // no-op (do not damage tool)
    }
}
