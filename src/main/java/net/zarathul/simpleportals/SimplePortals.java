package net.zarathul.simpleportals;

import org.apache.logging.log4j.Logger;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
import net.zarathul.simpleportals.blocks.*;
import net.zarathul.simpleportals.common.PortalWorldSaveData;
import net.zarathul.simpleportals.items.ItemPortalActivator;

@Mod(modid = SimplePortals.MOD_ID, name = SimplePortals.MOD_READABLE_NAME, version = SimplePortals.VERSION, updateJSON = "https://raw.githubusercontent.com/Zarathul/mcmodversions/master/simpleportals.json", guiFactory = "net.zarathul.simpleportals.configuration.ConfigGuiFactory", acceptedMinecraftVersions = "[1.12,1.13)")
public class SimplePortals {
	@Instance(value = SimplePortals.MOD_ID)
	public static SimplePortals instance;

	@SidedProxy(clientSide = "net.zarathul.simpleportals.ClientProxy", serverSide = "net.zarathul.simpleportals.ServerProxy")
	public static CommonProxy proxy;

	// blocks
	public static BlockPortal blockPortal;
	public static BlockPortalFrame blockPortalFrame;
	public static BlockPowerGauge blockPowerGauge;

	// items
	public static ItemPortalActivator itemPortalActivator;
	public static ItemBlock itemPortalFrame;
	public static ItemBlock itemPowerGauge;

	// creative tabs
	public static CreativeTabs creativeTab;

	// event hub
	public static ClientEventHub clientEventHub;
	public static CommonEventHub commonEventHub;

	// world save data handler
	public static PortalWorldSaveData portalSaveData;

	// logger
	public static Logger log;

	// constants
	public static final String MOD_ID = "simpleportals";
	public static final String MOD_READABLE_NAME = "Simple Portals";
	public static final String MOD_TAB_NAME = "Simple Mods";
	public static final String VERSION = "@VERSION@";

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		proxy.preInit(event);
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		proxy.init(event);
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		proxy.postInit(event);
	}
}