package net.zarathul.simpleportals;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.zarathul.simpleportals.configuration.Config;

public class CommonProxy
{
	public void preInit(FMLPreInitializationEvent event)
	{
		SimplePortals.log = event.getModLog();
		
		SimplePortals.commonEventHub = new CommonEventHub();
		MinecraftForge.EVENT_BUS.register(SimplePortals.commonEventHub);
		
		Config.load(event.getSuggestedConfigurationFile());
	}

	public void init(FMLInitializationEvent event)
	{
		FMLInterModComms.sendFunctionMessage(
			"theoneprobe",
			"getTheOneProbe",
			"net.zarathul.simpleportals.theoneprobe.TheOneProbeCompat$GetTheOneProbe");
	}

	public void postInit(FMLPostInitializationEvent event)
	{
	}
}
