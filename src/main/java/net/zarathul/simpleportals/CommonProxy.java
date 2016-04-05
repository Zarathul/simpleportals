package net.zarathul.simpleportals;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.zarathul.simpleportals.configuration.Config;
import net.zarathul.simpleportals.registration.Registry;

public class CommonProxy
{
	public void preInit(FMLPreInitializationEvent event)
	{
		SimplePortals.log = event.getModLog();
		
		SimplePortals.commonEventHub = new CommonEventHub();
		MinecraftForge.EVENT_BUS.register(SimplePortals.commonEventHub);
		
		Config.load(event.getSuggestedConfigurationFile());
		Registry.registerBlocks();
		Registry.registerItems();
	}

	public void init(FMLInitializationEvent event)
	{
		Registry.registerRecipes();
	}

	public void postInit(FMLPostInitializationEvent event)
	{
	}
}
