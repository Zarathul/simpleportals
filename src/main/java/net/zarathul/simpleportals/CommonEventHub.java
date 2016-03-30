package net.zarathul.simpleportals;

import net.minecraft.block.Block;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.event.world.WorldEvent.Load;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.zarathul.simpleportals.blocks.BlockPortal;
import net.zarathul.simpleportals.blocks.BlockPortalFrame;
import net.zarathul.simpleportals.common.PortalWorldSaveData;
import net.zarathul.simpleportals.registration.PortalRegistry;

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
	
	@SubscribeEvent
	public void OnBlockBreak(BreakEvent event)
	{
		if (!event.world.isRemote)
		{
			Block affectedBlock = event.state.getBlock();
			
			if (affectedBlock instanceof BlockPortal || affectedBlock instanceof BlockPortalFrame)
			{
				PortalRegistry.deactivatePortal(event.world, event.pos);
			}
		}
	}
}
