package com.landmaster.divisionsigil.recipe;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.landmaster.divisionsigil.Config;
import com.landmaster.divisionsigil.DivisionSigil;
import com.landmaster.divisionsigil.item.UnstableIngotItem;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import javax.annotation.Nonnull;
import java.util.*;

@EventBusSubscriber
public class UnstableRecipe extends ShapedRecipe {
    public UnstableRecipe() {
        super("", CraftingBookCategory.MISC,
                ShapedRecipePattern.of(ImmutableMap.of(
                        'i', Ingredient.of(Items.IRON_INGOT),
                        's', Ingredient.of(DivisionSigil.DIVISION_SIGIL),
                        'd', Ingredient.of(Items.DIAMOND)), "i", "s", "d"),
                DivisionSigil.UNSTABLE_INGOT.toStack());
    }

    private static final List<String> PARENT_CLASSES = ImmutableList.of(
            "net.minecraft.world.inventory.CraftingMenu",
            "net.minecraft.world.inventory.ResultSlot");

    private static final Set<Player> playersToTrack = Collections.newSetFromMap(new WeakHashMap<>());

    @Override
    public boolean matches(@Nonnull CraftingInput craftingInput, @Nonnull Level level) {
        if (!super.matches(craftingInput, level)) {
            return false;
        }
        var stackTrace = Thread.currentThread().getStackTrace();
        return Arrays.stream(stackTrace)
                .anyMatch(trace -> PARENT_CLASSES.contains(trace.getClassName()));
    }

    @Nonnull
    @Override
    public RecipeSerializer<? extends UnstableRecipe> getSerializer() {
        return DivisionSigil.UNSTABLE_RECIPE.get();
    }

    @SubscribeEvent
    public static void onCraft(PlayerEvent.ItemCraftedEvent event) {
        if (!event.getEntity().level().isClientSide() && event.getEntity().containerMenu instanceof CraftingMenu menu) {
            boolean unstableCrafted = false;
            for (var slot: menu.slots) {
                if (!(slot instanceof ResultSlot)) {
                    if (slot.getItem().is(DivisionSigil.UNSTABLE_INGOT)) {
                        slot.getItem().update(DivisionSigil.INSTABILITY_TIMESTAMP, event.getEntity().level().getGameTime(), v -> v);
                        unstableCrafted = true;
                    }
                }
            }
            if (menu.getCarried().is(DivisionSigil.UNSTABLE_INGOT)) {
                menu.getCarried().update(DivisionSigil.INSTABILITY_TIMESTAMP, event.getEntity().level().getGameTime(), v -> v);
                unstableCrafted = true;
            }
            if (unstableCrafted) {
                playersToTrack.add(event.getEntity());
                menu.broadcastChanges();
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerUpdate(PlayerTickEvent.Pre event) {
        if (!event.getEntity().level().isClientSide() && event.getEntity().containerMenu instanceof CraftingMenu menu) {
            if (playersToTrack.contains(event.getEntity())) {
                boolean unstableCounterChanged = false;
                boolean doTriggerExplosion = false;

                for (var slot: menu.slots) {
                    if (!(slot instanceof ResultSlot)) {
                        if (slot.getItem().is(DivisionSigil.UNSTABLE_INGOT)) {
                            doTriggerExplosion |= UnstableIngotItem.instabilityCounter(slot.getItem(), event.getEntity().level()).orElse(0L) >= Config.EXPLODE_TIME.getAsInt();
                            unstableCounterChanged = true;
                        }
                    }
                }

                if (menu.getCarried().is(DivisionSigil.UNSTABLE_INGOT)) {
                    doTriggerExplosion |= UnstableIngotItem.instabilityCounter(menu.getCarried(), event.getEntity().level()).orElse(0L) >= Config.EXPLODE_TIME.getAsInt();
                    unstableCounterChanged = true;
                }

                if (unstableCounterChanged) {
                    menu.broadcastChanges();
                }

                if (doTriggerExplosion) {
                    triggerUnstableExplosion(event.getEntity(), false);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onItemDrop(ItemTossEvent event) {
        if (event.getEntity().getItem().is(DivisionSigil.UNSTABLE_INGOT) && !event.getPlayer().level().isClientSide()) {
            event.setCanceled(true);

            triggerUnstableExplosion(event.getPlayer(), false);
        }
    }

    @SubscribeEvent
    public static void onCloseContainer(PlayerContainerEvent.Close event) {
        if (event.getContainer() instanceof CraftingMenu menu && !event.getEntity().level().isClientSide()) {
            triggerUnstableExplosion(event.getEntity(), true);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!event.getEntity().level().isClientSide()) {
            clearUnstableIngots(event.getEntity());
        }
    }

    private static boolean clearUnstableIngots(Player player) {
        boolean actuallyCleared = false;
        actuallyCleared |= ContainerHelper.clearOrCountMatchingItems(
                player.getInventory(), stack -> stack.is(DivisionSigil.UNSTABLE_INGOT), Integer.MAX_VALUE, false
        ) > 0;
        if (player.containerMenu instanceof CraftingMenu menu) {
            actuallyCleared |= ContainerHelper.clearOrCountMatchingItems(
                    menu.craftSlots, stack -> stack.is(DivisionSigil.UNSTABLE_INGOT), Integer.MAX_VALUE, false
            ) > 0;
            if (menu.getCarried().is(DivisionSigil.UNSTABLE_INGOT)) {
                actuallyCleared = true;
                menu.setCarried(ItemStack.EMPTY);
            }
            menu.broadcastChanges();
        }
        playersToTrack.remove(player);
        return actuallyCleared;
    }

    private static void triggerUnstableExplosion(Player player, boolean onlyIfActuallyCleared) {
        if (clearUnstableIngots(player) || !onlyIfActuallyCleared) {
            player.level().explode(player, player.getX(), player.getY(), player.getZ(), 5, Level.ExplosionInteraction.TRIGGER);
        }
    }

    public static class Serializer implements RecipeSerializer<UnstableRecipe> {

        @Nonnull
        @Override
        public MapCodec<UnstableRecipe> codec() {
            return MapCodec.unit(new UnstableRecipe());
        }

        @Nonnull
        @Override
        public StreamCodec<RegistryFriendlyByteBuf, UnstableRecipe> streamCodec() {
            return new StreamCodec<>() {
                @Override
                public void encode(@Nonnull RegistryFriendlyByteBuf o, @Nonnull UnstableRecipe unstableRecipe) {
                }

                @Nonnull
                @Override
                public UnstableRecipe decode(@Nonnull RegistryFriendlyByteBuf registryFriendlyByteBuf) {
                    return new UnstableRecipe();
                }
            };
        }
    }
}
