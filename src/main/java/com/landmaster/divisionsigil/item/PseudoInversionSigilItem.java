package com.landmaster.divisionsigil.item;

import com.google.common.collect.ImmutableList;
import com.landmaster.divisionsigil.Config;
import com.landmaster.divisionsigil.DivisionSigil;
import com.landmaster.divisionsigil.stabilization_recipe.DirectionalElement;
import com.landmaster.divisionsigil.stabilization_recipe.StabilizationRitualRecipeInput;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@EventBusSubscriber
public class PseudoInversionSigilItem extends Item {
    public PseudoInversionSigilItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isFoil(@Nonnull ItemStack stack) {
        return true;
    }

    @Override
    public boolean hasCraftingRemainingItem(@Nonnull ItemStack stack) {
        return true;
    }

    @Nonnull
    @Override
    public ItemStack getCraftingRemainingItem(@Nonnull ItemStack itemStack) {
        return itemStack;
    }

    private static final byte[][] REDSTONE_POSITIONS = {
            {1,0,0,0,0,0,0,0,0},
            {1,0,1,1,1,1,1,1,1},
            {1,0,1,0,0,0,0,0,1},
            {1,0,1,0,1,1,1,0,1},
            {1,0,1,0,0,0,1,0,1},
            {1,0,1,1,1,0,1,0,1},
            {1,0,0,0,0,0,1,0,1},
            {1,1,1,1,1,1,1,0,1},
            {0,0,0,0,0,0,0,0,1}
    };

    protected static List<BlockPos> checkRedstoneCondition(Level level, BlockPos pos) {
        List<BlockPos> incorrectBlocks = new ArrayList<>();
        for (int i=0; i<9; ++i) {
            for (int j=0; j<9; ++j) {
                var testPos = pos.offset(j - 4, 0, i - 4);
                if ((REDSTONE_POSITIONS[i][j] != 0)
                        ^ level.getBlockState(testPos).is(Blocks.REDSTONE_WIRE)) {
                    incorrectBlocks.add(testPos);
                }
            }
        }
        return incorrectBlocks;
    }

    protected static StabilizationRitualRecipeInput getRecipeInput(Level level, BlockPos pos) {
        Map<DirectionalElement, ChestBlockEntity> chests = new EnumMap<>(DirectionalElement.class);
        for (DirectionalElement element: DirectionalElement.VALUES) {
            if (level.getBlockEntity(pos.relative(element.direction, 5)) instanceof ChestBlockEntity blockEntity) {
                chests.put(element, blockEntity);
            }
        }
        return new StabilizationRitualRecipeInput(chests);
    }

    protected static boolean checkChestCondition(Level level, BlockPos pos) {
        var recipeInput = getRecipeInput(level, pos);
        return level.getRecipeManager().getRecipeFor(DivisionSigil.STABILIZATION_RITUAL_TYPE.get(), recipeInput, level)
                .isPresent();
    }

    public static boolean isRitualEnabled(Level level) {
        return !level.getRecipeManager().getAllRecipesFor(DivisionSigil.STABILIZATION_RITUAL_TYPE.get()).isEmpty();
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (event.getSource().getEntity() instanceof Player player) {
            var level = player.level();
            if (!level.isClientSide() && level.dimension() == Level.END) {
                if (event.getEntity().getType() == EntityType.IRON_GOLEM) {
                    var it = BlockPos.betweenClosedStream(AABB.ofSize(event.getEntity().position(), 5, 5, 5)).iterator();
                    while (it.hasNext()) {
                        var pos = it.next();
                        if (level.getBlockState(pos).is(Blocks.BEACON)) {
                            if (checkRedstoneCondition(level, pos).isEmpty() && checkChestCondition(level, pos)) {
                                player.setData(DivisionSigil.END_SIEGE_LOCATION, pos);
                                player.displayClientMessage(Component.translatable("message.divisionsigil.stabilization.siege_started", Config.END_SIEGE_KILL_COUNT.getAsInt()), false);
                                break;
                            }
                        }
                    }
                }
                event.getEntity().getExistingData(DivisionSigil.END_SIEGE_PLAYER).ifPresent(uuid -> {
                    if (player.getUUID().equals(uuid) && player.hasData(DivisionSigil.END_SIEGE_LOCATION)) {
                        int newKillCount = player.getData(DivisionSigil.END_SIEGE_KILL_COUNT) + 1;
                        if (newKillCount < Config.END_SIEGE_KILL_COUNT.getAsInt()) {
                            player.setData(DivisionSigil.END_SIEGE_KILL_COUNT, newKillCount);
                            player.displayClientMessage(Component.translatable("message.divisionsigil.stabilization.kill_count", newKillCount, Config.END_SIEGE_KILL_COUNT.getAsInt()), false);
                        } else {
                            var inventory = player.getInventory();
                            for (int i=0; i<inventory.getContainerSize(); ++i) {
                                if (inventory.getItem(i).is(DivisionSigil.DIVISION_SIGIL)) {
                                    inventory.setItem(i, DivisionSigil.PSEUDO_INVERSION_SIGIL.toStack());
                                    break;
                                }
                            }
                            player.displayClientMessage(Component.translatable("message.divisionsigil.stabilization.complete"), false);
                            player.removeData(DivisionSigil.END_SIEGE_LOCATION);
                            player.removeData(DivisionSigil.END_SIEGE_KILL_COUNT);
                        }
                    }
                });
            }
        }
    }

    @SubscribeEvent
    public static void joinLevel(EntityJoinLevelEvent event) {
        if (event.getEntity().level().dimension() != Level.END) {
            event.getEntity().removeData(DivisionSigil.END_SIEGE_LOCATION);
        }
    }

    @SubscribeEvent
    public static void mobTick(EntityTickEvent.Pre event) {
        var level = event.getEntity().level();
        if (!level.isClientSide()) {
            event.getEntity().getExistingData(DivisionSigil.END_SIEGE_PLAYER).ifPresent(uuid -> {
                var player = level.getPlayerByUUID(uuid);
                if (player == null || !player.hasData(DivisionSigil.END_SIEGE_LOCATION)) {
                    event.getEntity().discard();
                }
            });
        }
    }

    @SubscribeEvent
    public static void playerTick(PlayerTickEvent.Pre event) {
        var player = event.getEntity();
        if (player.level() instanceof ServerLevel level) {
            player.getExistingData(DivisionSigil.END_SIEGE_LOCATION).ifPresent(pos -> {
                if (level.dimension() == Level.END && level.getRandom().nextDouble() < Config.END_SIEGE_SPAWN_ATTEMPT_PROBABILITY.getAsDouble()) {
                    var endSiegeMobs = level.registryAccess().registryOrThrow(DivisionSigil.END_SIEGE_REGISTRY_KEY)
                            .get(ResourceLocation.fromNamespaceAndPath(DivisionSigil.MODID, "end_siege"));
                    if (endSiegeMobs != null) {
                        endSiegeMobs.getRandom(level.getRandom()).ifPresent(spawnerData -> {
                            int numSpawns = level.getRandom().nextIntBetweenInclusive(spawnerData.minCount, spawnerData.maxCount);

                            int attemptDistance = Config.END_SIEGE_SPAWN_ATTEMPT_DISTANCE.getAsInt();

                            int candX, candZ;
                            do {
                                candX = level.getRandom().nextIntBetweenInclusive(-attemptDistance, attemptDistance);
                                candZ = level.getRandom().nextIntBetweenInclusive(-attemptDistance, attemptDistance);
                            } while (candX * candX + candZ * candZ > attemptDistance * attemptDistance);
                            int yOffset = (int) Math.sqrt(attemptDistance * attemptDistance - candX * candX - candZ * candZ);

                            var candPos = new BlockPos.MutableBlockPos(pos.getX() + candX, pos.getY() + yOffset, pos.getZ() + candZ);

                            while (candPos.getY() >= Math.max(pos.getY() - yOffset, level.getMinBuildHeight())) {
                                if (level.getBlockState(candPos.below()).isFaceSturdy(level, candPos.below(), Direction.UP)) {
                                    var aabb = AABB.ofSize(candPos.getCenter(), 10, 6, 10);
                                    if (level.noCollision(spawnerData.type.getSpawnAABB(candPos.getX() + 0.5, candPos.getY(), candPos.getZ() + 0.5))
                                            && level.getEntitiesOfClass(LivingEntity.class, aabb).size() < 8) {
                                        for (int i=0; i<numSpawns; ++i) {
                                            var mob = spawnerData.type.spawn(level, candPos, MobSpawnType.NATURAL);
                                            if (mob instanceof LivingEntity livingMob) {
                                                livingMob.setData(DivisionSigil.END_SIEGE_PLAYER, player.getUUID());
                                            }
                                        }
                                    }
                                    break;
                                }
                                candPos.move(0, -1, 0);
                            }
                        });
                    }
                }
            });
        }
    }

    @SubscribeEvent
    public static void calculatePotentialSpawns(LevelEvent.PotentialSpawns event) {
        if (event.getMobCategory() == MobCategory.MONSTER && event.getLevel() instanceof Level level && level.dimension() == Level.END) {
            for (Player player: level.players()) {
                var endSiegeLoc = player.getExistingData(DivisionSigil.END_SIEGE_LOCATION);
                var blockDistance = Config.END_SIEGE_ENDERMAN_BLOCK_DISTANCE.getAsDouble();
                if (endSiegeLoc.isPresent()) {
                    if (event.getPos().distSqr(endSiegeLoc.get()) < blockDistance * blockDistance) {
                        while (!event.getSpawnerDataList().isEmpty()) {
                            event.removeSpawnerData(event.getSpawnerDataList().getFirst());
                        }
                        break;
                    }
                }
            }
        }
    }

    @EventBusSubscriber(Dist.CLIENT)
    static class ClientEvents {
        @SubscribeEvent
        private static void onRender(RenderLevelStageEvent event) {
            if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES) {
                var camera = event.getCamera();

                if (camera.getEntity() instanceof Player player) {
                    var bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
                    var vertexConsumer = bufferSource.getBuffer(RenderType.lines());

                    for (var pos : player.getData(DivisionSigil.INCORRECT_REDSTONE)) {
                        LevelRenderer.renderShape(event.getPoseStack(), vertexConsumer, Shapes.block(),
                                pos.getX() - camera.getPosition().x,
                                pos.getY() - camera.getPosition().y,
                                pos.getZ() - camera.getPosition().z,
                                1, 0, 0, 1);
                    }
                }
            }
        }
    }
}
