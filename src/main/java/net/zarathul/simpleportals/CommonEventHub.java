package net.zarathul.simpleportals;

import net.minecraftforge.event.world.WorldEvent.Load;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.zarathul.simpleportals.common.PortalWorldSaveData;

/**
 * Hosts Forge event handlers on both the server and client side.
 */
public final class CommonEventHub
{
	public CommonEventHub()
	{
	}
	
	@SubscribeEvent
	public void OnWorldLoad(Load event)
	{
		if (!event.world.isRemote)
		{
			SimplePortals.portalSaveData = PortalWorldSaveData.get(event.world);
		}
	}
}
