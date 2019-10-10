package net.zarathul.simpleportals.configuration;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.translation.LanguageMap;
import net.minecraftforge.common.ForgeConfigSpec;

import java.nio.file.Path;

/**
 * Provides helper methods to load the mods config.
 */

public final class Config
{
	// Configs

	public static ForgeConfigSpec ServerConfig;
	public static ForgeConfigSpec ClientConfig;

	// Config builders

	private static final ForgeConfigSpec.Builder ServerConfigBuilder = new ForgeConfigSpec.Builder();
	private static final ForgeConfigSpec.Builder ClientConfigBuilder = new ForgeConfigSpec.Builder();

	// Default values
	
	private static final int defaultMaxSize = 7;
	private static final int defaultPowerCost = 1;
	private static final int defaultPowerCapacity = 64;
	private static final boolean defaultParticlesEnabled = true;
	private static final boolean defaultAmbientSoundEnabled = false;

	//private static final String defaultPowerSource = "oredict:enderpearl";

	// Settings

	public static ForgeConfigSpec.IntValue maxSize;
	public static ForgeConfigSpec.IntValue powerCost;
	public static ForgeConfigSpec.IntValue powerCapacity;
	public static ForgeConfigSpec.BooleanValue particlesEnabled;
	public static ForgeConfigSpec.BooleanValue ambientSoundEnabled;
	//public static ForgeConfigSpec. powerSource;
	public static NonNullList<ItemStack> powerSources;

	// Config file categories

	public static final String CATEGORY_MISC = "misc";

	static
	{
		LanguageMap i18nMap = LanguageMap.getInstance();

		// Server

		ServerConfigBuilder.comment(i18nMap.translateKey("configui.category.misc")).push(CATEGORY_MISC);

		maxSize = ServerConfigBuilder.comment(i18nMap.translateKey("configui.maxSize.tooltip"))
				.defineInRange("maxSize", defaultMaxSize, 3, 128);

		powerCost = ServerConfigBuilder.comment(i18nMap.translateKey("configui.powerCost.tooltip"))
				.defineInRange("powerCost", defaultPowerCost, -1, Integer.MAX_VALUE);

		powerCapacity = ServerConfigBuilder.comment(i18nMap.translateKey("configui.powerCapacity.tooltip"))
				.defineInRange("powerCapacity", defaultPowerCapacity, 0, Integer.MAX_VALUE);

		ServerConfigBuilder.pop();

		// Client

		ClientConfigBuilder.comment(i18nMap.translateKey("configui.category.misc")).push(CATEGORY_MISC);

		particlesEnabled = ClientConfigBuilder.comment(i18nMap.translateKey("configui.particlesEnabled.tooltip"))
				.define("particlesEnabled", defaultParticlesEnabled);

		ambientSoundEnabled = ClientConfigBuilder.comment(i18nMap.translateKey("configui.ambientSoundEnabled.tooltip"))
				.define("ambientSoundEnabled", defaultAmbientSoundEnabled);

		ClientConfigBuilder.pop();

		ServerConfig = ServerConfigBuilder.build();
		ClientConfig = ClientConfigBuilder.build();
	}

	/**
	 * Loads the mods settings from the specified file.
	 * 
	 * @param configSpec
	 * The specification for the contents of the config file.
	 * @param path
	 * The path to the config file.
	 */

	public static final void load(ForgeConfigSpec configSpec, Path path)
	{
		final CommentedFileConfig configData = CommentedFileConfig.builder(path)
				.sync()
				.autosave()
				.writingMode(WritingMode.REPLACE)
				.build();

		configData.load();
		configSpec.setConfig(configData);
	}

	public static void updateValidPowerSources()
	{
		// TODO: replace this somehow with recipe
		powerSources = NonNullList.create();

		/*
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
		*/
	}
}
