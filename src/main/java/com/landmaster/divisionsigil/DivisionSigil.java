package com.landmaster.divisionsigil;

import com.landmaster.divisionsigil.block.CursedEarthBlock;
import com.landmaster.divisionsigil.item.*;
import com.landmaster.divisionsigil.recipe.UnstableRecipe;
import com.landmaster.divisionsigil.transmutation.CropRevertTransmutation;
import com.landmaster.divisionsigil.transmutation.HoeTransmutation;
import com.landmaster.divisionsigil.transmutation.StandardHoeTransmutation;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.SimpleTier;
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
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Supplier;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(DivisionSigil.MODID)
@EventBusSubscriber
public class DivisionSigil {
    public static final String MODID = "divisionsigil";

    public static final Logger LOGGER = LogUtils.getLogger();

    public static final ResourceKey<Registry<HoeTransmutation>> HOE_TRANSMUTATIONS_KEY = ResourceKey.createRegistryKey(
            ResourceLocation.fromNamespaceAndPath(MODID, "transmutation")
    );
    public static final ResourceKey<Registry<MapCodec<? extends HoeTransmutation>>> HOE_TRANSMUTATIONS_DISPATCH_KEY
            = ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(MODID, "transmutation_dispatch"));

    public static final Registry<MapCodec<? extends HoeTransmutation>> HOE_TRANSMUTATIONS_DISPATCH = new RegistryBuilder<>(HOE_TRANSMUTATIONS_DISPATCH_KEY).create();


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
    ) {
        @Nonnull
        @Override
        public Tool createToolProperties(@Nonnull TagKey<Block> block) {
            if (block == BlockTags.MINEABLE_WITH_PICKAXE) {
                return new Tool(List.of(
                        Tool.Rule.deniesDrops(this.getIncorrectBlocksForDrops()),
                        Tool.Rule.minesAndDrops(BlockTags.BASE_STONE_OVERWORLD, this.getSpeed() * 5),
                        Tool.Rule.minesAndDrops(BlockTags.BASE_STONE_NETHER, this.getSpeed() * 5),
                        Tool.Rule.minesAndDrops(block, this.getSpeed())),
                        1.0F, 1);
            }
            return super.createToolProperties(block);
        }
    };

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    public static final DeferredRegister.DataComponents DATA_COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, MODID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, MODID);

    public static final Supplier<DataComponentType<Long>> INSTABILITY_TIMESTAMP = DATA_COMPONENTS.registerComponentType("instability_timestamp",
            builder -> builder.persistent(Codec.LONG).networkSynchronized(ByteBufCodecs.VAR_LONG));

    public static final DeferredBlock<CursedEarthBlock> CURSED_EARTH = BLOCKS.registerBlock("cursed_earth", CursedEarthBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.GRASS_BLOCK));
    public static final DeferredItem<BlockItem> CURSED_EARTH_ITEM = ITEMS.registerSimpleBlockItem(CURSED_EARTH);

    public static final DeferredItem<DivisionSigilItem> DIVISION_SIGIL = ITEMS.registerItem("division_sigil", DivisionSigilItem::new,
            new Item.Properties().durability(256));
    public static final DeferredItem<Item> DIVISION_SIGIL_UNACTIVATED = ITEMS.registerSimpleItem("division_sigil_unactivated", new Item.Properties().stacksTo(1));
    public static final DeferredItem<UnstableIngotItem> UNSTABLE_INGOT = ITEMS.registerItem("unstable_ingot", UnstableIngotItem::new,
            new Item.Properties().stacksTo(1));
    public static final DeferredItem<Item> UNSTABLE_NUGGET = ITEMS.registerSimpleItem("unstable_nugget");
    public static final DeferredItem<Item> SEMISTABLE_INGOT = ITEMS.registerSimpleItem("semistable_ingot");
    public static final DeferredItem<ReversingHoeItem> REVERSING_HOE = ITEMS.registerItem("reversing_hoe",
            properties -> new ReversingHoeItem(UNSTABLE_TIER, properties));
    public static final DeferredItem<Item> ETHERIC_SWORD = ITEMS.registerItem("etheric_sword", (props) -> new EthericSwordItem(UNSTABLE_TIER, props));
    public static final DeferredItem<Item> DESTRUCTION_PICKAXE = ITEMS.registerItem("destruction_pickaxe", props -> new PickaxeItem(UNSTABLE_TIER, props));
    public static final DeferredItem<HealingAxeItem> HEALING_AXE = ITEMS.registerItem("healing_axe",
            properties -> new HealingAxeItem(UNSTABLE_TIER, properties));
    public static final DeferredItem<ShovelItem> EROSION_SHOVEL = ITEMS.registerItem("erosion_shovel",
            properties -> new ErosionShovelItem(UNSTABLE_TIER, properties));

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> SIGIL_TAB = CREATIVE_MODE_TABS.register("division_sigil", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.divisionsigil")) //The language key for the title of your CreativeModeTab
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(DIVISION_SIGIL::toStack)
            .displayItems((parameters, output) -> {
                output.accept(DIVISION_SIGIL_UNACTIVATED);
                output.accept(DIVISION_SIGIL);
                output.accept(UNSTABLE_INGOT);
                output.accept(UNSTABLE_NUGGET);
                output.accept(SEMISTABLE_INGOT);
                output.accept(CURSED_EARTH_ITEM);
                output.accept(REVERSING_HOE);
                output.accept(ETHERIC_SWORD);
                output.accept(DESTRUCTION_PICKAXE);
                output.accept(HEALING_AXE);
                output.accept(EROSION_SHOVEL);
            }).build());

    public static final Supplier<RecipeSerializer<UnstableRecipe>> UNSTABLE_RECIPE = RECIPE_SERIALIZERS.register("unstable_recipe", UnstableRecipe.Serializer::new);

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public DivisionSigil(IEventBus modEventBus, ModContainer modContainer) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
        DATA_COMPONENTS.register(modEventBus);
        RECIPE_SERIALIZERS.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    @SubscribeEvent
    private static void registerRegistries(NewRegistryEvent event) {
        event.register(HOE_TRANSMUTATIONS_DISPATCH);
    }

    @SubscribeEvent
    private static void registerDatapackRegistries(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(HOE_TRANSMUTATIONS_KEY, HoeTransmutation.CODEC);
    }

    @SubscribeEvent
    private static void register(RegisterEvent event) {
        event.register(HOE_TRANSMUTATIONS_DISPATCH_KEY, registry -> {
            registry.register(ResourceLocation.fromNamespaceAndPath(MODID, "standard"), StandardHoeTransmutation.CODEC);
            registry.register(ResourceLocation.fromNamespaceAndPath(MODID, "crop_revert"), CropRevertTransmutation.CODEC);
        });
    }
}
