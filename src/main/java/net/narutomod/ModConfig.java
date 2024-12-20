package net.narutomod;

import net.minecraftforge.common.config.Config;

@Config(modid = NarutomodMod.MODID)
@ElementsNarutomodMod.ModElement.Tag
public class ModConfig extends ElementsNarutomodMod.ModElement {
    @Config.Comment("If enabled tailed beasts spawn naturally around the world.")
	public static boolean SPAWN_TAILED_BEASTS = true;

    @Config.Comment("If true KG will be auto assigned to players in due time, after 300 ninjaXp reachched.")
	public static boolean AUTO_KEKKEIGENKAI_ASSIGNMENT = true;

	@Config.Comment("If enabled players has a chance of spawning as jinchuriki.")
	public static boolean SPAWN_AS_JINCHURIKI = true;

	@Config.Comment("If enabled, rinnegan/tenseigan/ems gained without the prerequisite achievements will be removed.")
	public static boolean REMOVE_CHEAT_DOJUTSUS = false;

	@Config.Comment("Itachi's spawn weight (0~20). 0 to stop spawning.")
	public static int SPAWN_WEIGHT_ITACHI = 1;

	@Config.Comment("Kisame's spawn weight (0~20). 0 to stop spawning.")
	public static int SPAWN_WEIGHT_KISAME = 1;

	@Config.Comment("sasori's spawn weight (0~20). 0 to stop spawning.")
	public static int SPAWN_WEIGHT_SASORI = 1;

	@Config.Comment("sasori's spawn weight (0~20). 0 to stop spawning.")
	public static int SPAWN_WEIGHT_DEIDARA = 1;

	@Config.Comment("Hidan's spawn weight (0~20). 0 to stop spawning.")
	public static int SPAWN_WEIGHT_HIDAN = 1;

	@Config.Comment("Zabuza's spawn weight (0~20). 0 to stop spawning.")
	public static int SPAWN_WEIGHT_ZABUZA = 1;

	@Config.Comment("White zetsu's spawn weight (0~20). 0 to stop spawning.")
	public static int SPAWN_WEIGHT_WHITEZETSU = 10;

	@Config.Comment("Whether or not bosses are aggressive on sight")
	public static boolean AGGRESSIVE_BOSSES = false;

	@Config.Comment("Stupid arms in the back Naruto run animation")
	public static boolean NARUTO_RUN = true;

	@Config.Comment("Itachi's chance to be real (1~100). Lower value means higher chance. 1 means it will be real everytime.")
	public static int ITACHI_REAL_CHANCE = 10;

	@Config.Comment("Chakra regeneration rate. 0.006 means 0.6% of your max chakra every 4 seconds")
	public static float CHAKRA_REGEN_RATE = 0.006F;

	@Config.Comment("Disable this to not allow any jutsu scrolls in loot chests")
	public static boolean ENABLE_JUTSU_SCROLLS_IN_LOOTCHESTS = true;

	@Config.Comment("Amaterasu flame on block duration (reference: vanilla fire is 3)")
	public static int AMATERASU_BLOCK_DURATION = 100;

	@Config.Comment("Ninja XP gain multiplier (higher value gains NinjaXp faster. default=0.5)")
	public static double NINJAXP_MULTIPLIER = 0.5D;

	@Config.Comment("Max NinjaXP attainable (default = 100000.0")
	public static double MAX_NINJAXP = 100000.0D;

	@Config.Comment("Jutsu XP gain multiplier (higher value gains NinjaXp faster. default=1)")
	public static int JUTSUXP_MULTIPLIER = 1;

	@Config.Comment("Chakra percent on spawn (spawn with chakra lol default=1)")
	public static float CHAKRA_PERCENT = 1;

	public ModConfig(ElementsNarutomodMod instance) {
		super(instance, 837);
	}
}
