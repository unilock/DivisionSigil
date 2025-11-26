package com.landmaster.divisionsigil.block;

import com.landmaster.divisionsigil.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nonnull;

public class CursedEarthBlock extends Block {
    public CursedEarthBlock(Properties props) {
        super(props);
    }

    @Override
    public int getFireSpreadSpeed(@Nonnull BlockState state, @Nonnull BlockGetter level, @Nonnull BlockPos pos, @Nonnull Direction direction) {
        return 140;
    }

    @Override
    public int getFlammability(@Nonnull BlockState state, @Nonnull BlockGetter level, @Nonnull BlockPos pos, @Nonnull Direction direction) {
        return 20;
    }

    @Override
    protected void randomTick(@Nonnull BlockState state, ServerLevel level, @Nonnull BlockPos pos, @Nonnull RandomSource random) {
        if (!level.isClientSide() && random.nextDouble() < Config.CURSED_EARTH_SPAWN_RATE.getAsDouble()) {
            var posAbove = pos.above();

            if (level.canSeeSky(posAbove) && level.isDay() && level.getBlockState(posAbove).isAir() && !level.isRainingAt(posAbove)) {
                var fireState = BaseFireBlock.getState(level, posAbove);
                level.setBlock(posAbove, fireState, 11);
            }

            var aabb = AABB.ofSize(pos.getCenter(), 7, 4, 7);
            if (level.getEntitiesOfClass(LivingEntity.class, aabb).size() < 8) {
                var structureManager = level.structureManager();
                var generator = level.getChunkSource().getGenerator();
                var monsterEntry = NaturalSpawner.getRandomSpawnMobAt(
                        level, structureManager, generator, MobCategory.MONSTER, level.random, posAbove);
                monsterEntry.ifPresent(spawnerData -> {
                    if (spawnerData.type.canSummon()
                    && SpawnPlacements.isSpawnPositionOk(spawnerData.type, level, posAbove)
                    && SpawnPlacements.checkSpawnRules(spawnerData.type, level, MobSpawnType.NATURAL, posAbove, level.random)
                    && level.noCollision(spawnerData.type.getSpawnAABB(posAbove.getX() + 0.5, posAbove.getY(), posAbove.getZ() + 0.5))) {
                        var monster = spawnerData.type.spawn(level, posAbove, MobSpawnType.NATURAL);
                        if (monster instanceof Mob mob) {
                            mob.setPersistenceRequired();
                        }
                    }
                });
            }
        }
    }
}
