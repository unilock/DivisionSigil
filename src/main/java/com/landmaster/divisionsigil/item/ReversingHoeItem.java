package com.landmaster.divisionsigil.item;

import com.landmaster.divisionsigil.DivisionSigil;
import com.landmaster.divisionsigil.transmutation.HoeTransmutationInput;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

@EventBusSubscriber
public class ReversingHoeItem extends HoeItem {
    public ReversingHoeItem(Tier tier, Properties properties) {
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

    @SubscribeEvent
    public static void onToolUse(BlockEvent.BlockToolModificationEvent event) {
        if (event.getLevel() instanceof ServerLevel level && event.getHeldItemStack().getItem() instanceof ReversingHoeItem) {
            var recipes = level.getRecipeManager().getRecipesFor(
                    DivisionSigil.HOE_TRANSMUTATION_TYPE.get(), new HoeTransmutationInput(event.getPos()), level);
            var it = recipes
                    .stream()
                    .map(RecipeHolder::value)
                    .sorted(Comparator.reverseOrder())
                    .iterator();
            while (it.hasNext()) {
                var transmutation = it.next();
                var state = transmutation.newBlockState(level, event.getPos());
                if (state.isPresent()) {
                    if (event.getState() == state.get()) {
                        event.setCanceled(true);
                    } else {
                        event.setFinalState(state.get());
                    }
                    break;
                }
            }
        }
    }
}
