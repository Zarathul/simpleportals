package net.zarathul.simpleportals;

import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.zarathul.simpleportals.blocks.BlockPortal;
import net.zarathul.simpleportals.blocks.BlockPortalFrame;
import net.zarathul.simpleportals.blocks.BlockPowerGauge;
import net.zarathul.simpleportals.common.PortalWorldSaveData;
import net.zarathul.simpleportals.configuration.Config;
import net.zarathul.simpleportals.items.ItemPortalActivator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;

@Mod(SimplePortals.MOD_ID)
public class SimplePortals
{
	// block and item names
	public static final String BLOCK_PORTAL_NAME = "blockPortal";
	public static final String BLOCK_PORTAL_FRAME_NAME = "blockPortalFrame";
	public static final String BLOCK_POWER_GAUGE_NAME = "blockPowerGauge";
	public static final String ITEM_PORTAL_FRAME_NAME = "itemPortalFrame";
	public static final String ITEM_POWER_GAUGE_NAME = "itemPowerGauge";
	public static final String ITEM_PORTAL_ACTIVATOR_NAME = "itemPortalActivator";

	// blocks
	public static BlockPortal blockPortal;
	public static BlockPortalFrame blockPortalFrame;
	public static BlockPowerGauge blockPowerGauge;

	// items
	public static ItemPortalActivator itemPortalActivator;
	public static BlockItem itemPortalFrame;
	public static BlockItem itemPowerGauge;

	// creative tabs
	public static ItemGroup creativeTab;

	// world save data handler
	public static PortalWorldSaveData portalSaveData;
	
	// constants
	public static final String MOD_ID = "simpleportals";
	public static final String MOD_READABLE_NAME = "Simple Portals";
	public static final String MOD_TAB_NAME = "Simple Mods";
	public static final String VERSION = "@VERSION@";

	// logger
	public static final Logger log = LogManager.getLogger(MOD_ID);

	public SimplePortals()
	{
		// Setup configs
		ModLoadingContext Mlc = ModLoadingContext.get();
		Mlc.registerConfig(ModConfig.Type.COMMON, Config.CommonConfig);
		Mlc.registerConfig(ModConfig.Type.CLIENT, Config.ClientConfig);

		Config.load(Config.CommonConfig, FMLPaths.CONFIGDIR.get().resolve(MOD_ID + "-common.toml"));
		Config.load(Config.ClientConfig, FMLPaths.CONFIGDIR.get().resolve(MOD_ID + "-client.toml"));

		// Setup event listeners
		IEventBus SetupEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		//SetupEventBus.addListener(this::CommonInit);
		SetupEventBus.addListener(this::ClientInit);

		MinecraftForge.EVENT_BUS.register(EventHub.class);
	}

	/*
	private void CommonInit(final FMLCommonSetupEvent event)
	{
		//Config.load(event.getSuggestedConfigurationFile());
	}
    */

	private void ClientInit(final FMLClientSetupEvent event)
	{
		// Check if a a "Simple Mods" tab already exists, otherwise make one.
		creativeTab = Arrays.stream(ItemGroup.GROUPS)
			.filter(tab -> tab.getTabLabel().equals(SimplePortals.MOD_TAB_NAME))
			.findFirst()
			.orElseGet(() ->
				new ItemGroup(SimplePortals.MOD_TAB_NAME)
				{
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