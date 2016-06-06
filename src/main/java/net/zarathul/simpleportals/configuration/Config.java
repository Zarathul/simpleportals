package net.zarathul.simpleportals.configuration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.oredict.OreDictionary;
import net.zarathul.simpleportals.SimplePortals;
import net.zarathul.simpleportals.registration.Registry;

/**
 * Provides helper methods to load the mods config.
 */
public final class Config
{
	private static Configuration config = null;

	// Default recipes

	public static final Recipe defaultPortalFrameRecipe = new Recipe
	(
		4,
		new RecipePattern(
			"OLO",
			"LPL",
			"OLO"
		),
		new RecipeComponent[]
		{
			new RecipeComponent("L", "oreDict", "gemLapis"),
			new RecipeComponent("O", "minecraft", "obsidian"),
			new RecipeComponent("P", "minecraft", "ender_pearl")
		}
	);

	public static final Recipe defaultPowerGaugeRecipe = new Recipe
	(
		1,
		new RecipePattern(
			String.format("%1$sR%1$s", RecipePattern.EMPTY_SLOT),
			String.format("RFR", RecipePattern.EMPTY_SLOT),
			String.format("%1$sR%1$s", RecipePattern.EMPTY_SLOT)
		),
		new RecipeComponent[]
		{
			new RecipeComponent("R", "oreDict", "dustRedstone"),
			new RecipeComponent("F", SimplePortals.MOD_ID, Registry.ITEM_PORTAL_FRAME_NAME)
		}
	);

	public static final Recipe defaultPortalActivatorRecipe = new Recipe
	(
		1,
		new RecipePattern(
			String.format("E%sE", RecipePattern.EMPTY_SLOT),
			String.format("%1$sI%1$s", RecipePattern.EMPTY_SLOT),
			String.format("%1$sG%1$s", RecipePattern.EMPTY_SLOT)
		),
		new RecipeComponent[]
		{
			new RecipeComponent("E", "oreDict", "gemEmerald"),
			new RecipeComponent("I", "minecraft", "ender_eye"),
			new RecipeComponent("G", "oreDict", "ingotGold")
		}
	);

	// Default values
	
	private static final int defaultMaxSize = 7;
	private static final int defaultPowerCost = 1;
	private static final int defaultPowerCapacity = 64;
	private static final boolean defaultParticlesEnabled = true;
	private static final boolean defaultAmbientSoundEnabled = false;
	private static final ItemStack defaultPowerSource = new ItemStack(Items.ENDER_PEARL);

	// Settings

	public static int maxSize;
	public static int powerCost;
	public static int powerCapacity;
	public static boolean particlesEnabled;
	public static boolean ambientSoundEnabled;
	public static String powerSource;
	public static Recipe portalFrameRecipe;
	public static Recipe powerGaugeRecipe;
	public static Recipe portalActivatorRecipe;
	public static List<ItemStack> powerSources;

	// Config file categories

	public static final String CATEGORY_MISC = Configuration.CATEGORY_GENERAL + Configuration.CATEGORY_SPLITTER + "misc";
	public static final String CATEGORY_RECIPES = Configuration.CATEGORY_GENERAL + Configuration.CATEGORY_SPLITTER + "recipes";

	private static final String CATEGORY_RECIPES_PORTAL_FRAME = CATEGORY_RECIPES + Configuration.CATEGORY_SPLITTER + "portalframe";
	private static final String CATEGORY_RECIPES_POWER_GAUGE = CATEGORY_RECIPES + Configuration.CATEGORY_SPLITTER + "powergauge";
	private static final String CATEGORY_RECIPES_PORTAL_ACTIVATOR = CATEGORY_RECIPES + Configuration.CATEGORY_SPLITTER + "portalactivator";

	/**
	 * Gets the loaded configuration.
	 * 
	 * @return
	 * The last loaded configuration or <code>null</code> if no config has been loaded yet.
	 */
	public static final Configuration getConfig()
	{
		return config;
	}

	/**
	 * Loads the mods settings from the specified file.
	 * 
	 * @param configFile
	 * The file to load the settings from.
	 */
	public static final void load(File configFile)
	{
		config = new Configuration(configFile);
		config.load();
		sync();
	}

	/**
	 * Synchronizes the config GUI and the config file.
	 */
	public static void sync()
	{
		Property prop;

		// Misc

		config.getCategory(CATEGORY_MISC).setLanguageKey("configui.category.misc").setComment(I18n.translateToLocal("configui.category.misc.tooltip"));
		
		prop = config.get(CATEGORY_MISC, "maxSize", defaultMaxSize);
		prop.setComment(I18n.translateToLocal("configui.maxSize.tooltip"));
		prop.setLanguageKey("configui.maxSize").setMinValue(3);
		maxSize = prop.getInt();

		prop = config.get(CATEGORY_MISC, "powerCost", defaultPowerCost);
		prop.setComment(I18n.translateToLocal("configui.powerCost.tooltip"));
		prop.setLanguageKey("configui.powerCost").setMinValue(0);
		powerCost = prop.getInt();

		prop = config.get(CATEGORY_MISC, "powerCapacity", defaultPowerCapacity);
		prop.setComment(I18n.translateToLocal("configui.powerCapacity.tooltip"));
		prop.setLanguageKey("configui.powerCapacity").setMinValue(0);
		powerCapacity = prop.getInt();
		
		prop = config.get(CATEGORY_MISC, "powerSource", defaultPowerSource.getItem().getRegistryName().toString());
		prop.setComment(I18n.translateToLocal("configui.powerSource.tooltip"));
		prop.setLanguageKey("configui.powerSource");
		powerSource = prop.getString();
		
		updateValidPowerSources();

		prop = config.get(CATEGORY_MISC, "particlesEnabled", defaultParticlesEnabled);
		prop.setComment(I18n.translateToLocal("configui.particlesEnabled.tooltip"));
		prop.setLanguageKey("configui.particlesEnabled");
		particlesEnabled = prop.getBoolean();

		prop = config.get(CATEGORY_MISC, "ambientSoundEnabled", defaultAmbientSoundEnabled);
		prop.setComment(I18n.translateToLocal("configui.ambientSoundEnabled.tooltip"));
		prop.setLanguageKey("configui.ambientSoundEnabled");
		ambientSoundEnabled = prop.getBoolean();
		
		// Recipes

		config.getCategory(CATEGORY_RECIPES).setLanguageKey("configui.category.recipes").setComment(I18n.translateToLocal("configui.category.recipes.tooltip"));
		config.getCategory(CATEGORY_RECIPES_PORTAL_FRAME).setLanguageKey("configui.category.portalFrame");
		config.getCategory(CATEGORY_RECIPES_POWER_GAUGE).setLanguageKey("configui.category.powerGauge");
		config.getCategory(CATEGORY_RECIPES_PORTAL_ACTIVATOR).setLanguageKey("configui.category.portalActivator");

		portalFrameRecipe = loadRecipe(config, CATEGORY_RECIPES_PORTAL_FRAME, Config.defaultPortalFrameRecipe);
		powerGaugeRecipe = loadRecipe(config, CATEGORY_RECIPES_POWER_GAUGE, Config.defaultPowerGaugeRecipe);
		portalActivatorRecipe = loadRecipe(config, CATEGORY_RECIPES_PORTAL_ACTIVATOR, Config.defaultPortalActivatorRecipe);

		if (config.hasChanged())
		{
			config.save();
		}
	}
	
	private static void updateValidPowerSources()
	{
		Item item = Item.getByNameOrId(powerSource);
		
		if (item != null)
		{
			powerSources = new ArrayList<ItemStack>();
			powerSources.add(new ItemStack(item));
		}
		else
		{
			String[] oreDictComponents = powerSource.split(":");
			String oreDictName = (oreDictComponents != null && oreDictComponents.length > 1 && oreDictComponents[0].equals("oreDict")) ? oreDictComponents[1] : null;
			powerSources = OreDictionary.getOres(oreDictName, false);
		}
	}

	/**
	 * Loads a recipe from the config file.
	 * 
	 * @param config
	 * The configuration interface.
	 * @param category
	 * The category containing the recipe.
	 * @param defaultRecipe
	 * The default values for the recipe.
	 * @return
	 * The recipe loaded from the config.
	 */
	private static Recipe loadRecipe(Configuration config, String category, Recipe defaultRecipe)
	{
		Property prop = config.get(category, "shapeless", defaultRecipe.isShapeless);
		prop.setComment(I18n.translateToLocal("configui.shapeless.tooltip"));
		prop.setLanguageKey("configui.shapeless").setRequiresMcRestart(true);
		boolean shapeless = prop.getBoolean();

		prop = config.get(category, "yield", defaultRecipe.yield);
		prop.setComment(I18n.translateToLocal("configui.yield.tooltip"));
		prop.setLanguageKey("configui.yield").setRequiresMcRestart(true).setMinValue(1).setMaxValue(64);
		int yield = prop.getInt();

		prop = config.get(category, "components", defaultRecipe.getComponentList());
		prop.setComment(I18n.translateToLocal("configui.components.tooltip"));
		prop.setLanguageKey("configui.components").setRequiresMcRestart(true).setMaxListLength(27);
		String[] components = prop.getStringList();

		prop = config.get(category, "pattern", defaultRecipe.pattern.rows.toArray(new String[0])).setIsListLengthFixed(true).setMaxListLength(3);
		prop.setComment(I18n.translateToLocal("configui.pattern.tooltip"));
		prop.setLanguageKey("configui.pattern").setRequiresMcRestart(true);
		String[] pattern = prop.getStringList();

		return new Recipe(yield, (shapeless) ? null : new RecipePattern(pattern), Recipe.toComponents(components));
	}
}
