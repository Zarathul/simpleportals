package net.zarathul.simpleportals;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.zarathul.simpleportals.configuration.Config;

/**
 * Hosts Forge event handlers on the client side.
 */
public final class ClientEventHub
{
	@SubscribeEvent
	public void OnConfigChanged(OnConfigChangedEvent event)
	{
		if (SimplePortals.MOD_ID.equals(event.getModID()))
		{
			Config.sync();
		}
	}

	@SubscribeEvent
	public void OnModelRegistration(ModelRegistryEvent event)
	{
		// register item models
		ModelLoader.setCustomModelResourceLocation(SimplePortals.itemPortalFrame, 0, new ModelResourceLocation(SimplePortals.blockPortalFrame.getRegistryName(), "inventory"));
		ModelLoader.setCustomModelResourceLocation(SimplePortals.itemPowerGauge, 0, new ModelResourceLocation(SimplePortals.blockPowerGauge.getRegistryName(), "inventory"));
		ModelLoader.setCustomModelResourceLocation(SimplePortals.itemPortalActivator, 0, new ModelResourceLocation(SimplePortals.itemPortalActivator.getRegistryName(), "inventory"));
	}
}
