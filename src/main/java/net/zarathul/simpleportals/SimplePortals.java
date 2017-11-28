package net.zarathul.simpleportals;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.zarathul.simpleportals.blocks.BlockPortal;
import net.zarathul.simpleportals.blocks.BlockPortalFrame;
import net.zarathul.simpleportals.blocks.BlockPowerGauge;
import net.zarathul.simpleportals.commands.CommandPortals;
import net.zarathul.simpleportals.commands.CommandTeleport;
import net.zarathul.simpleportals.common.PortalWorldSaveData;
import net.zarathul.simpleportals.items.ItemPortalActivator;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;

@Mod(modid = SimplePortals.MOD_ID, name = SimplePortals.MOD_READABLE_NAME, version = SimplePortals.VERSION,
     updateJSON = "https://raw.githubusercontent.com/Zarathul/mcmodversions/master/simpleportals.json",
     guiFactory = "net.zarathul.simpleportals.configuration.ConfigGuiFactory")
public class SimplePortals
{
	@Instance(value = SimplePortals.MOD_ID)
	public static SimplePortals instance;

	@SidedProxy(clientSide = "net.zarathul.simpleportals.ClientProxy", serverSide = "net.zarathul.simpleportals.ServerProxy")
	public static CommonProxy proxy;

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
	public static ItemBlock itemPortalFrame;
	public static ItemBlock itemPowerGauge;

	// creative tabs
	public static CreativeTabs creativeTab;

	// event hubs
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
	public void preInit(FMLPreInitializationEvent event)
	{
		proxy.preInit(event);
	}

	@EventHandler
	public void init(FMLInitializationEvent event)
	{
		proxy.init(event);
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{
		proxy.postInit(event);
	}

	@EventHandler
	public void onServerStarting(FMLServerStartingEvent event)
	{
		event.registerServerCommand(new CommandTeleport());
		event.registerServerCommand(new CommandPortals());
	}

	/**
	 * Adds a creative mode tab.
	 */
	@SideOnly(Side.CLIENT)
	public static final void addCreativeTab()
	{
		// Check if a a "Simple Mods" tab already exists, otherwise make one.
		creativeTab = Arrays.stream(CreativeTabs.CREATIVE_TAB_ARRAY)
			.filter(tab -> tab.getTabLabel().equals(SimplePortals.MOD_TAB_NAME))
			.findFirst()
			.orElseGet(() ->
				new CreativeTabs(SimplePortals.MOD_TAB_NAME)
				{
					private ItemStack iconStack;

					@Override
					public String getTranslatedTabLabel()
					{
						return this.getTabLabel();
					}

					@Override
					public ItemStack getTabIconItem()
					{
						if (iconStack == null) iconStack = new ItemStack(SimplePortals.itemPortalFrame);

						return iconStack;
					}
				}
			);
	}
}