package com.landmaster.divisionsigil;

import com.google.common.collect.ImmutableList;
import com.landmaster.divisionsigil.block.CursedEarthBlock;
import com.landmaster.divisionsigil.item.*;
import com.landmaster.divisionsigil.recipe.UnstableRecipe;
import com.landmaster.divisionsigil.stabilization_recipe.StabilizationRitualRecipe;
import com.landmaster.divisionsigil.stabilization_recipe.StandardStabilizationRitualRecipe;
import com.landmaster.divisionsigil.transmutation.CropRevertTransmutation;
import com.landmaster.divisionsigil.transmutation.HoeTransmutation;
import com.landmaster.divisionsigil.transmutation.StandardHoeTransmutation;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.SimpleTier;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import net.neoforged.neoforge.registries.*;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;

import java.util.*;
import java.util.function.Supplier;

@Mod(DivisionSigil.MODID)
@EventBusSubscriber
public class DivisionSigil {
    public static final String MODID = "divisionsigil";

    public static final Logger LOGGER = LogUtils.getLogger();

    public static final TagKey<Item> UNSTABLE_INGOT_TAG = TagKey.create(
            Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath("c", "ingots/unstable")
    );

    public static final Tier UNSTABLE_TIER = new SimpleTier(
            Tiers.DIAMOND.getIncorrectBlocksForDrops(),
            Tiers.DIAMOND.getUses(),
            Tiers.DIAMOND.getSpeed(),
            Tiers.DIAMOND.getAttackDamageBonus(),
            Tiers.DIAMOND.getEnchantmentValue(),
            () -> Ingredient.of(UNSTABLE_INGOT_TAG)
    );

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    public static final DeferredRegister.DataComponents DATA_COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, MODID);
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, MODID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, MODID);
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MODID);

    public static final Supplier<DataComponentType<Long>> INSTABILITY_TIMESTAMP = DATA_COMPONENTS.registerComponentType("instability_timestamp",
            builder -> builder.persistent(Codec.LONG).networkSynchronized(ByteBufCodecs.VAR_LONG));
    public static final Supplier<DataComponentType<Boolean>> UNBREAKABLE = DATA_COMPONENTS.registerComponentType("unbreakable",
            builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL));

    public static final DeferredBlock<CursedEarthBlock> CURSED_EARTH = BLOCKS.registerBlock("cursed_earth", CursedEarthBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.GRASS_BLOCK));
    public static final DeferredItem<BlockItem> CURSED_EARTH_ITEM = ITEMS.registerSimpleBlockItem(CURSED_EARTH);

    public static final DeferredItem<DivisionSigilItem> DIVISION_SIGIL = ITEMS.registerItem("division_sigil", DivisionSigilItem::new, new Item.Properties().stacksTo(1));
    public static final DeferredItem<DivisionSigilUnactivatedItem> DIVISION_SIGIL_UNACTIVATED = ITEMS.registerItem(
            "division_sigil_unactivated", DivisionSigilUnactivatedItem::new, new Item.Properties().stacksTo(1));
    public static final DeferredItem<PseudoInversionSigilItem> PSEUDO_INVERSION_SIGIL = ITEMS.registerItem(
            "pseudo_inversion_sigil", PseudoInversionSigilItem::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)
    );
    public static final DeferredItem<UnstableIngotItem> UNSTABLE_INGOT = ITEMS.registerItem("unstable_ingot", UnstableIngotItem::new,
            new Item.Properties().stacksTo(1));
    public static final DeferredItem<Item> UNSTABLE_NUGGET = ITEMS.registerSimpleItem("unstable_nugget");
    public static final DeferredItem<Item> SEMISTABLE_INGOT = ITEMS.registerSimpleItem("semistable_ingot");
    public static final DeferredItem<MobiusIngotItem> MOBIUS_INGOT = ITEMS.registerItem("mobius_ingot", MobiusIngotItem::new);
    public static final DeferredItem<ReversingHoeItem> REVERSING_HOE = ITEMS.registerItem("reversing_hoe",
            properties -> new ReversingHoeItem(UNSTABLE_TIER, properties),
            new Item.Properties().attributes(HoeItem.createAttributes(UNSTABLE_TIER, -3.0F, 0.0F)));
    public static final DeferredItem<EthericSwordItem> ETHERIC_SWORD = ITEMS.registerItem("etheric_sword", (props)
            -> new EthericSwordItem(UNSTABLE_TIER, props),
            new Item.Properties().attributes(SwordItem.createAttributes(UNSTABLE_TIER, 3, -2.4F)));
    public static final DeferredItem<DestructionPickaxeItem> DESTRUCTION_PICKAXE = ITEMS.registerItem("destruction_pickaxe",
            props -> new DestructionPickaxeItem(UNSTABLE_TIER, props),
            new Item.Properties().attributes(PickaxeItem.createAttributes(UNSTABLE_TIER, 1.0F, -2.8F)));
    public static final DeferredItem<HealingAxeItem> HEALING_AXE = ITEMS.registerItem("healing_axe",
            properties -> new HealingAxeItem(UNSTABLE_TIER, properties),
            new Item.Properties().attributes(AxeItem.createAttributes(UNSTABLE_TIER, 5.0F, -3.0F)));
    public static final DeferredItem<ErosionShovelItem> EROSION_SHOVEL = ITEMS.registerItem("erosion_shovel",
            properties -> new ErosionShovelItem(UNSTABLE_TIER, properties),
            new Item.Properties().attributes(ShovelItem.createAttributes(UNSTABLE_TIER, 1.5F, -3.0F)));
    public static final DeferredItem<BuildersWandItem> BUILDERS_WAND = ITEMS.registerItem("builders_wand", BuildersWandItem::new,
            new Item.Properties().stacksTo(1));

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> SIGIL_TAB = CREATIVE_MODE_TABS.register("division_sigil", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.divisionsigil")) //The language key for the title of your CreativeModeTab
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(DIVISION_SIGIL::toStack)
            .displayItems((parameters, output) -> {
                output.accept(DIVISION_SIGIL_UNACTIVATED);
                output.accept(DIVISION_SIGIL);
                output.accept(PSEUDO_INVERSION_SIGIL);
                output.accept(UNSTABLE_INGOT);
                output.accept(UNSTABLE_NUGGET);
                output.accept(SEMISTABLE_INGOT);
                output.accept(MOBIUS_INGOT);
                output.accept(CURSED_EARTH_ITEM);
                output.accept(REVERSING_HOE);
                output.accept(ETHERIC_SWORD);
                output.accept(DESTRUCTION_PICKAXE);
                output.accept(HEALING_AXE);
                output.accept(EROSION_SHOVEL);
                output.accept(BUILDERS_WAND);
            }).build());

    public static final Supplier<RecipeType<HoeTransmutation>> HOE_TRANSMUTATION_TYPE = RECIPE_TYPES.register("hoe_transmutation", RecipeType::simple);
    public static final Supplier<RecipeType<StabilizationRitualRecipe>> STABILIZATION_RITUAL_TYPE = RECIPE_TYPES.register("stabilization_ritual", RecipeType::simple);

    public static final Supplier<RecipeSerializer<UnstableRecipe>> UNSTABLE_RECIPE = RECIPE_SERIALIZERS.register("unstable_recipe", UnstableRecipe.Serializer::new);
    public static final Supplier<RecipeSerializer<StandardHoeTransmutation>> STANDARD_HOE_TRANSMUTATION
            = RECIPE_SERIALIZERS.register("standard_hoe_transmutation", StandardHoeTransmutation.Serializer::new);
    public static final Supplier<RecipeSerializer<CropRevertTransmutation>> CROP_REVERT
            = RECIPE_SERIALIZERS.register("crop_revert_transmutation", CropRevertTransmutation.Serializer::new);
    public static final Supplier<RecipeSerializer<StandardStabilizationRitualRecipe>> STANDARD_STABILIZATION_RITUAL
            = RECIPE_SERIALIZERS.register("standard_stabilization_ritual", StandardStabilizationRitualRecipe.Serializer::new);

    public static final Supplier<AttachmentType<List<BlockPos>>> INCORRECT_REDSTONE
            = ATTACHMENT_TYPES.register("incorrect_redstone", () -> AttachmentType
            .<List<BlockPos>>builder(() -> ImmutableList.of())
            .sync((holder, to) -> holder == to, ByteBufCodecs.collection(ArrayList::new, BlockPos.STREAM_CODEC))
            .build());

    public static final Supplier<AttachmentType<BlockPos>> END_SIEGE_LOCATION
            = ATTACHMENT_TYPES.register("end_siege_location", () -> AttachmentType
            .builder(() -> BlockPos.ZERO)
            .serialize(BlockPos.CODEC)
            .build());
    public static final Supplier<AttachmentType<Integer>> END_SIEGE_KILL_COUNT
            = ATTACHMENT_TYPES.register("end_siege_kill_count", () -> AttachmentType.builder(() -> 0).serialize(Codec.INT).build());
    public static final Supplier<AttachmentType<UUID>> END_SIEGE_PLAYER
            = ATTACHMENT_TYPES.register("end_siege_player", () -> AttachmentType.builder(() -> new UUID(0, 0)).serialize(UUIDUtil.CODEC).build());

    public static final ResourceKey<Registry<WeightedRandomList<MobSpawnSettings.SpawnerData>>> END_SIEGE_REGISTRY_KEY = ResourceKey.createRegistryKey(
            ResourceLocation.fromNamespaceAndPath(MODID, "end_siege")
    );


    public DivisionSigil(IEventBus modEventBus, ModContainer modContainer) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
        DATA_COMPONENTS.register(modEventBus);
        RECIPE_TYPES.register(modEventBus);
        RECIPE_SERIALIZERS.register(modEventBus);
        ATTACHMENT_TYPES.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    @SubscribeEvent
    private static void modifyComponents(ModifyDefaultComponentsEvent event) {
        event.modify(DIVISION_SIGIL, builder -> builder.set(DataComponents.MAX_DAMAGE, Config.DIVISION_SIGIL_DURABILITY.get()));
        event.modify(EROSION_SHOVEL, builder -> builder.set(DataComponents.TOOL, new Tool(List.of(
                Tool.Rule.deniesDrops(UNSTABLE_TIER.getIncorrectBlocksForDrops()),
                Tool.Rule.minesAndDrops(BlockTags.DIRT, (float) (UNSTABLE_TIER.getSpeed() * Config.EROSION_SHOVEL_SPEED_MULTIPLIER.getAsDouble())),
                Tool.Rule.minesAndDrops(BlockTags.MINEABLE_WITH_SHOVEL, UNSTABLE_TIER.getSpeed())),
                1.0F, 1)));
        event.modify(DESTRUCTION_PICKAXE, builder -> builder.set(DataComponents.TOOL, new Tool(List.of(
                Tool.Rule.deniesDrops(UNSTABLE_TIER.getIncorrectBlocksForDrops()),
                Tool.Rule.minesAndDrops(BlockTags.BASE_STONE_OVERWORLD, (float) (UNSTABLE_TIER.getSpeed() * Config.DESTRUCTION_PICKAXE_SPEED_MULTIPLIER.getAsDouble())),
                Tool.Rule.minesAndDrops(BlockTags.MINEABLE_WITH_PICKAXE, UNSTABLE_TIER.getSpeed())),
                1.0F, 1)));
    }

    @SubscribeEvent
    private static void registerDatapackRegistries(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(END_SIEGE_REGISTRY_KEY, WeightedRandomList.codec(MobSpawnSettings.SpawnerData.CODEC), null, builder -> builder.maxId(0));
    }
}
