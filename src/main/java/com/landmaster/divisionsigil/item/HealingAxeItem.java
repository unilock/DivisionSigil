package com.landmaster.divisionsigil.item;

import com.landmaster.divisionsigil.Config;
import com.landmaster.divisionsigil.DivisionSigil;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Consumer;

@EventBusSubscriber
public class HealingAxeItem extends AxeItem {
    public HealingAxeItem(Tier tier, Properties properties) {
        super(tier, properties);
    }

    @Override
    public float getAttackDamageBonus(@Nonnull Entity target, float damage, @Nonnull DamageSource damageSource) {
        double result = super.getAttackDamageBonus(target, damage, damageSource);
        if (target.getType().is(EntityTypeTags.INVERTED_HEALING_AND_HARM)) {
            result += damage * (Config.AXE_UNDEAD_DAMAGE_MULTIPLIER.getAsDouble() - 1);
        }
        return (float) result;
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
    public static void onAttack(AttackEntityEvent event) {
        if (!event.getEntity().level().isClientSide() && event.getEntity().getWeaponItem().getItem() instanceof HealingAxeItem) {
            if (event.getTarget() instanceof LivingEntity livingEntity) {
                if (!livingEntity.isInvertedHealAndHarm()) {
                    livingEntity.heal((float)Config.AXE_HEAL_AMOUNT.getAsDouble());
                    event.setCanceled(true);
                }
                payHP(event.getEntity());
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerUpdate(PlayerTickEvent.Pre event) {
        Level level = event.getEntity().level();
        if (!level.isClientSide()) {
            if (event.getEntity().getMainHandItem().getItem() instanceof HealingAxeItem
            && level.getGameTime() % Config.AXE_RESTORE_TIME.getAsInt() == 0) {
                event.getEntity().getFoodData().eat(Config.AXE_HUNGER_AMOUNT.getAsInt(), (float)Config.AXE_SATURATION_AMOUNT.getAsDouble());
            }
        }
    }

    private static void payHP(Player player) {
        player.hurt(player.damageSources().playerAttack(player), (float)Config.AXE_LIFE_COST.getAsDouble());
    }
}
