package net.zarathul.simpleportals.configuration;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;

import java.nio.file.Path;

/**
 * Provides helper methods to load the mods config.
 */

public final class Config
{
	// Configs

	public static ForgeConfigSpec ConfigSpec;

	// Config builders

	private static final ForgeConfigSpec.Builder ConfigBuilder = new ForgeConfigSpec.Builder();

	// Default values
	
	private static final int defaultMaxSize = 7;
	private static final int defaultPowerCost = 1;
	private static final int defaultPowerCapacity = 64;
	private static final boolean defaultParticlesEnabled = true;
	private static final boolean defaultAmbientSoundEnabled = false;
	private static final String defaultPowerSource = "forge:ender_pearls";

	// Settings

	public static ForgeConfigSpec.IntValue maxSize;
	public static ForgeConfigSpec.IntValue powerCost;
	public static ForgeConfigSpec.IntValue powerCapacity;
	public static ForgeConfigSpec.BooleanValue particlesEnabled;
	public static ForgeConfigSpec.BooleanValue ambientSoundEnabled;
	public static ResourceLocation powerSource;

	private static ForgeConfigSpec.ConfigValue<String> powerSourceString;

	static
	{
		// Server

		ConfigBuilder.push("server");

		maxSize = ConfigBuilder.translation("config.max_size")
				.comment("The maximum size including the frame of a portal.")
				.defineInRange("maxSize", defaultMaxSize, 3, 128);

		powerCost = ConfigBuilder.translation("config.power_cost")
				.comment("The power cost per port. Set to 0 for no cost.")
				.defineInRange("powerCost", defaultPowerCost, -1, Integer.MAX_VALUE);

		powerCapacity = ConfigBuilder.translation("config.power_capacity")
				.comment("The amount of power a portal can store.")
				.defineInRange("powerCapacity", defaultPowerCapacity, 0, Integer.MAX_VALUE);

		powerSourceString = ConfigBuilder.translation("config.power_source")
				.comment("The item that gets converted to power when thrown into a portal (1 power per item). This MUST be a tag.")
				.define("powerSource", defaultPowerSource);

		ConfigBuilder.pop();

		// Client

		ConfigBuilder.push("client");

		particlesEnabled = ConfigBuilder.translation("config.particles_enabled")
				.comment("Enables the portal particle effect.")
				.define("particlesEnabled", defaultParticlesEnabled);

		ambientSoundEnabled = ConfigBuilder.translation("config.ambient_sound_enabled")
				.comment("Enables the portal ambient sound.")
				.define("ambientSoundEnabled", defaultAmbientSoundEnabled);

		ConfigBuilder.pop();

		ConfigSpec = ConfigBuilder.build();
	}

	/**
	 * Loads the mods settings from the specified file.
	 * 
	 * @param configSpec
	 * The specification for the contents of the config file.
	 * @param path
	 * The path to the config file.
	 */

	public static void load(ForgeConfigSpec configSpec, Path path)
	{
		final CommentedFileConfig configData = CommentedFileConfig.builder(path)
				.sync()
				.autosave()
				.writingMode(WritingMode.REPLACE)
				.build();

		configData.load();
		configSpec.setConfig(configData);
	}

	public static void updatePowerSource()
	{
		powerSource = new ResourceLocation(powerSourceString.get());
	}
}
