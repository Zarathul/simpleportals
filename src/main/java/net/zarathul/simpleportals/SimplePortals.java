package net.zarathul.simpleportals;

import net.minecraft.command.arguments.ArgumentSerializer;
import net.minecraft.command.arguments.ArgumentTypes;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.zarathul.simpleportals.blocks.BlockPortal;
import net.zarathul.simpleportals.blocks.BlockPortalFrame;
import net.zarathul.simpleportals.blocks.BlockPowerGauge;
import net.zarathul.simpleportals.commands.arguments.BlockArgument;
import net.zarathul.simpleportals.common.PortalWorldSaveData;
import net.zarathul.simpleportals.common.TeleportTask;
import net.zarathul.simpleportals.configuration.Config;
import net.zarathul.simpleportals.configuration.gui.ConfigGuiFactory;
import net.zarathul.simpleportals.items.ItemPortalActivator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.concurrent.LinkedBlockingQueue;

@Mod(SimplePortals.MOD_ID)
public class SimplePortals
{
	// block and item names
	public static final String BLOCK_PORTAL_NAME = "portal";
	public static final String BLOCK_PORTAL_FRAME_NAME = "portal_frame";
	public static final String BLOCK_POWER_GAUGE_NAME = "power_gauge";
	public static final String ITEM_PORTAL_FRAME_NAME = "portal_frame";
	public static final String ITEM_POWER_GAUGE_NAME = "power_gauge";
	public static final String ITEM_PORTAL_ACTIVATOR_NAME = "portal_activator";

	// blocks
	public static BlockPortal blockPortal;
	public static BlockPortalFrame blockPortalFrame;
	public static BlockPowerGauge blockPowerGauge;

	// items
	public static ItemPortalActivator itemPortalActivator;
	public static BlockItem itemPortalFrame;
	public static BlockItem itemPowerGauge;

	// creative tab
	public static ItemGroup creativeTab = MakeCreativeTab();
	// world save data handler
	public static PortalWorldSaveData portalSaveData;
	
	// constants
	public static final String MOD_ID = "simpleportals";

	// logger
	public static final Logger log = LogManager.getLogger(MOD_ID);

	public static LinkedBlockingQueue<TeleportTask> TELEPORT_QUEUE = new LinkedBlockingQueue<>();

	public SimplePortals()
	{
		// Register custom argument types for command parser
		ArgumentTypes.register("sportals_block", BlockArgument.class, new ArgumentSerializer<>(BlockArgument::block));

		// Setup configs
		ModLoadingContext Mlc = ModLoadingContext.get();
		Mlc.registerConfig(ModConfig.Type.COMMON, Config.CommonConfigSpec, MOD_ID + "-common.toml");
		Mlc.registerConfig(ModConfig.Type.CLIENT, Config.ClientConfigSpec, MOD_ID + "-client.toml");
		Config.load(Config.CommonConfigSpec, FMLPaths.CONFIGDIR.get().resolve(MOD_ID + "-common.toml"));
		Config.load(Config.ClientConfigSpec, FMLPaths.CONFIGDIR.get().resolve(MOD_ID + "-client.toml"));

		// Setup config UI
		ConfigGuiFactory.setConfigHolder("net.zarathul.simpleportals.configuration.Config");
		DistExecutor.callWhenOn(Dist.CLIENT, () ->
			() -> {
				Mlc.registerExtensionPoint(ExtensionPoint.CONFIGGUIFACTORY, () -> ConfigGuiFactory::getConfigGui);
				return null;
			}
		);

		// Setup event listeners
		IEventBus SetupEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		SetupEventBus.register(EventHub.class);
		MinecraftForge.EVENT_BUS.register(EventHub.class);
	}

	static ItemGroup MakeCreativeTab()
	{
		// Checks if a "Simple Mods" tab already exists, otherwise makes one.
		return Arrays.stream(ItemGroup.GROUPS)
			.filter(tab -> tab.getPath().equals(SimplePortals.MOD_ID))
			.findFirst()
			.orElseGet(() ->
				new ItemGroup(SimplePortals.MOD_ID)
				{
					@OnlyIn(Dist.CLIENT)
					private ItemStack iconStack;

					@Override
					@OnlyIn(Dist.CLIENT)
					public ItemStack createIcon()
					{
						if (iconStack == null) iconStack = new ItemStack(itemPortalFrame);

						return iconStack;
					}
				}
			);
	}
}