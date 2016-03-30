package net.zarathul.simpleportals;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.zarathul.simpleportals.registration.Registry;

public class ClientProxy extends CommonProxy
{
	@Override
	public void preInit(FMLPreInitializationEvent event)
	{
		Registry.addCreativeTab();
		
		super.preInit(event);
		
		SimplePortals.clientEventHub = new ClientEventHub();
		MinecraftForge.EVENT_BUS.register(SimplePortals.clientEventHub);
		
		Registry.registerItemModels();
	}

	@Override
	public void init(FMLInitializationEvent event)
	{
		super.init(event);

		Registry.registerWithWaila();
	}

	@Override
	public void postInit(FMLPostInitializationEvent event)
	{
		super.postInit(event);
	}
}
