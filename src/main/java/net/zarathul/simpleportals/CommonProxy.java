package net.zarathul.simpleportals;

import jline.internal.Log;
import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.*;
import net.zarathul.simpleportals.configuration.Config;
import net.zarathul.simpleportals.registration.Registry;

public class CommonProxy {
	public void preInit(FMLPreInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(new Registry());
		SimplePortals.log = event.getModLog();

		SimplePortals.commonEventHub = new CommonEventHub();
		MinecraftForge.EVENT_BUS.register(SimplePortals.commonEventHub);

		Config.load(event.getSuggestedConfigurationFile());
	}

	public void init(FMLInitializationEvent event) {
	}

	public void postInit(FMLPostInitializationEvent event) {
	}
	
	public void registerItemRenderer(Item item, int meta, String id) {
		
	}
	
}
