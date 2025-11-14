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

    static final ModConfigSpec SPEC = BUILDER.build();
}
