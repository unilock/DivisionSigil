package com.landmaster.divisionsigil;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue EXPLODE_TIME = BUILDER
            .comment("Number of ticks before unstable ingots explode")
            .defineInRange("explodeTime", 200, 0, Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue AXE_RESTORE_TIME = BUILDER
            .comment("Amount of ticks between each instance of hunger restoration with the Healing Axe")
            .defineInRange("axeRestoreTime", 200, 0, Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue AXE_HUNGER_AMOUNT = BUILDER
            .comment("Amount of hunger to restore automatically at a time for the Healing Axe")
            .defineInRange("axeHungerAmount", 1, 0, 20);

    public static final ModConfigSpec.DoubleValue AXE_SATURATION_AMOUNT = BUILDER
            .comment("Amount of saturation to restore automatically at a time for the Healing Axe")
            .defineInRange("axeSaturationAmount", 1.0, 0.0, 20.0);

    public static final ModConfigSpec.DoubleValue AXE_HEAL_AMOUNT = BUILDER
            .comment("Amount to heal mobs by with the axe")
            .defineInRange("axeHealAmount", 5.0, 0.0, 1000.0);

    public static final ModConfigSpec.DoubleValue AXE_LIFE_COST = BUILDER
            .comment("Amount of health paid by attacking with the Healing Axe")
            .defineInRange("axeLifeCost", 4.0, 0.0, 1000.0);

    public static final ModConfigSpec.DoubleValue AXE_UNDEAD_DAMAGE_MULTIPLIER = BUILDER
            .comment("Damage multiplier against undead when attacking with the Healing Axe")
            .defineInRange("axeUndeadDamageMultiplier", 4.0, 0.0, 100.0);

    public static final ModConfigSpec.IntValue DIVISION_SIGIL_DURABILITY = BUILDER
            .comment("Number of uses per division sigil activation")
            .defineInRange("divisionSigilDurability", 256, 1, Integer.MAX_VALUE);

    public static final ModConfigSpec.DoubleValue DESTRUCTION_PICKAXE_SPEED_MULTIPLIER = BUILDER
            .comment("Speed multiplier when mining ordinary stone-like materials with a destruction pickaxe")
            .defineInRange("destructionPickaxeMultiplier", 5.0, 1.0, 100.0);

    public static final ModConfigSpec.DoubleValue EROSION_SHOVEL_SPEED_MULTIPLIER = BUILDER
            .comment("Speed multiplier when mining ordinary dirt-like materials with a destruction pickaxe")
            .defineInRange("erosionShovelMultiplier", 5.0, 1.0, 100.0);

    public static final ModConfigSpec.IntValue BUILDERS_WAND_MAX_BLOCKS = BUILDER
            .comment("Maximum blocks that the Builder's Wand can place")
            .defineInRange("wandMaxBlocks", 64, 1, Integer.MAX_VALUE);

    public static final ModConfigSpec.BooleanValue ENABLE_DIVISION_SIGIL_RITUAL = BUILDER
            .comment("Whether the normal division sigil ritual should be enabled (disable if modpack author wants division sigils activated by some other means)")
            .define("enableRitual", true);

    public static final ModConfigSpec.DoubleValue CURSED_EARTH_SPAWN_RATE = BUILDER
            .comment("Cursed earth spawn probability per random tick")
            .defineInRange("cursedEarthSpawnRate", 1.0, 0.0, 1.0);

    public static final ModConfigSpec.DoubleValue END_SIEGE_ENDERMAN_BLOCK_DISTANCE = BUILDER
            .comment("Normal End spawns are prevented from spawning below this distance from an end siege")
            .defineInRange("endermanBlockDistance", 300.0, 0.0, 10000000.0);

    public static final ModConfigSpec.DoubleValue END_SIEGE_SPAWN_ATTEMPT_PROBABILITY = BUILDER
            .comment("Probability of an end siege monster spawn attempt per tick")
            .defineInRange("endSiegeSpawnProbability", 0.05, 0.0, 1.0);

    public static final ModConfigSpec.IntValue END_SIEGE_SPAWN_ATTEMPT_DISTANCE = BUILDER
            .comment("Maximum distance from beacon where end siege monsters will attempt to spawn")
            .defineInRange("endSiegeSpawnDistance", 100, 0, 500);

    public static final ModConfigSpec.IntValue END_SIEGE_KILL_COUNT = BUILDER
            .comment("Number of mobs to kill for the End Siege")
            .defineInRange("endSiegeKillCount", 100, 0, 1000);

    static final ModConfigSpec SPEC = BUILDER.build();
}
