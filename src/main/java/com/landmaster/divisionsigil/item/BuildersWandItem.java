package com.landmaster.divisionsigil.item;

import com.landmaster.divisionsigil.Config;
import com.landmaster.divisionsigil.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nonnull;
import java.util.*;

public class BuildersWandItem extends Item {
    public BuildersWandItem(Properties properties) {
        super(properties);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Nonnull
    @Override
    public InteractionResult useOn(@Nonnull UseOnContext context) {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        if (player != null) {
            var item = level.getBlockState(context.getClickedPos()).getBlock().asItem();
            boolean success = false;
            if (item instanceof BlockItem blockItem) {
                var hitResult = context.getHitResult();
                var blockPlacements = computeBlockPlacements(player, hitResult, Config.BUILDERS_WAND_MAX_BLOCKS.getAsInt());
                var inventory = player.getInventory();
                int itemIndex = 0;
                placementLoop:
                for (var pos : blockPlacements) {
                    while (!inventory.getItem(itemIndex).is(item)) {
                        ++itemIndex;
                        if (itemIndex >= inventory.getContainerSize()) {
                            break placementLoop;
                        }
                    }
                    var subResult = blockItem.place(
                            new BlockPlaceContext(player, context.getHand(), inventory.getItem(itemIndex), hitResult.withPosition(pos))
                    );
                    var templateBlockState = level.getBlockState(pos.relative(hitResult.getDirection().getOpposite()));
                    var newBlockState = level.getBlockState(pos);
                    for (var property: Util.ORIENTATION_PROPERTIES) {
                        var value = templateBlockState.getOptionalValue(property);
                        if (value.isPresent()) {
                            newBlockState = newBlockState.trySetValue((Property)property, (Comparable)value.get());
                        }
                    }
                    level.setBlock(pos, newBlockState, 2);
                    success |= subResult.indicateItemUse();
                }
            }
            return success ? InteractionResult.SUCCESS : InteractionResult.FAIL;
        }
        return super.useOn(context);
    }

    public static SequencedSet<BlockPos> computeBlockPlacements(Player player, BlockHitResult result, int maxAmount) {
        var level = player.level();
        var item = level.getBlockState(result.getBlockPos()).getBlock().asItem();
        var itemCount = player.getInventory().countItem(item);
        return computeBlockPlacements(level, result, Math.min(
                maxAmount, player.isCreative() && itemCount > 0 ? Integer.MAX_VALUE : itemCount
        ));
    }

    public static SequencedSet<BlockPos> computeBlockPlacements(Level level, BlockHitResult hitResult, int maxAmount) {
        if (maxAmount <= 0 || level.getBlockEntity(hitResult.getBlockPos()) != null) {
            return Collections.emptyNavigableSet();
        }

        Deque<BlockPos> queue = new ArrayDeque<>();
        SequencedSet<BlockPos> result = new LinkedHashSet<>();
        var dir = hitResult.getDirection();
        var initialPos = hitResult.getBlockPos().relative(dir);
        var templateBlock = level.getBlockState(hitResult.getBlockPos()).getBlock();
        if (level.getBlockState(initialPos).isAir()) {
            queue.addLast(initialPos);
            result.add(initialPos);
        }
        var candidateDirs = Util.AXIS_TO_PLANE_DIRS.get(dir.getAxis());
        queueLoop:
        while (!queue.isEmpty()) {
            var curPos = queue.removeFirst();
            for (var candidateDir: candidateDirs) {
                if (result.size() >= maxAmount) {
                    break queueLoop;
                }
                var candPos = curPos.relative(candidateDir);
                if (!result.contains(candPos) && level.getBlockState(candPos).isAir()
                        && level.getBlockState(candPos.relative(dir.getOpposite())).getBlock() == templateBlock) {
                    queue.addLast(candPos);
                    result.add(candPos);
                }
            }
        }
        return Collections.unmodifiableSequencedSet(result);
    }
}
