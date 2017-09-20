package net.zarathul.simpleportals.configuration;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.oredict.OreDictionary;

import java.io.File;

/**
 * Provides helper methods to load the mods config.
 */
public final class Config
{
	private static Configuration config = null;

	// Default values
	
	private static final int defaultMaxSize = 7;
	private static final int defaultPowerCost = 1;
	private static final int defaultPowerCapacity = 64;
	private static final boolean defaultParticlesEnabled = true;
	private static final boolean defaultAmbientSoundEnabled = false;
	private static final String defaultPowerSource = "oredict:enderpearl";

	// Settings

	public static int maxSize;
	public static int powerCost;
	public static int powerCapacity;
	public static boolean particlesEnabled;
	public static boolean ambientSoundEnabled;
	public static String powerSource;
	public static NonNullList<ItemStack> powerSources;

	// Config file categories

	public static final String CATEGORY_MISC = Configuration.CATEGORY_GENERAL + Configuration.CATEGORY_SPLITTER + "misc";

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
		
		prop = config.get(CATEGORY_MISC, "powerSource", defaultPowerSource);
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
			powerSources = NonNullList.withSize(1, new ItemStack(item));
		}
		else
		{
			String[] oreDictComponents = powerSource.split(":");
			String oreDictName = (oreDictComponents.length > 1 && oreDictComponents[0].toLowerCase().equals("oredict"))
				? oreDictComponents[1]
				: null;

			NonNullList<ItemStack> oreDictEntries = OreDictionary.getOres(oreDictName, false);

			// Compile list of the ItemStacks returned by the ore dictionary and their respective subtypes

			powerSources = NonNullList.create();

			for (ItemStack stack : oreDictEntries)
			{
				if (stack.getMetadata() == OreDictionary.WILDCARD_VALUE)
				{
					stack.getItem().getSubItems(CreativeTabs.SEARCH, powerSources);
				}
				else
				{
					powerSources.add(stack);
				}
			}
		}
	}
}
