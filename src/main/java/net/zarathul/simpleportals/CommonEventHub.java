package net.zarathul.simpleportals;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.world.WorldEvent.Load;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.zarathul.simpleportals.blocks.BlockPortal;
import net.zarathul.simpleportals.blocks.BlockPortalFrame;
import net.zarathul.simpleportals.blocks.BlockPowerGauge;
import net.zarathul.simpleportals.common.PortalWorldSaveData;
import net.zarathul.simpleportals.items.ItemPortalActivator;
import net.zarathul.simpleportals.items.ItemPortalFrame;
import net.zarathul.simpleportals.items.ItemPowerGauge;

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
		if (!event.getWorld().isRemote)
		{
			SimplePortals.portalSaveData = PortalWorldSaveData.get(event.getWorld());
		}
	}

	@SubscribeEvent
	public void OnBlockRegistration(RegistryEvent.Register<Block> event)
	{
		SimplePortals.blockPortal = new BlockPortal();
		SimplePortals.blockPortalFrame = new BlockPortalFrame();
		SimplePortals.blockPowerGauge = new BlockPowerGauge();

		event.getRegistry().registerAll(
			SimplePortals.blockPortal,
			SimplePortals.blockPortalFrame,
			SimplePortals.blockPowerGauge
		);
	}

	@SubscribeEvent
	public void OnItemRegistration(RegistryEvent.Register<Item> event)
	{
		SimplePortals.itemPortalFrame = new ItemPortalFrame(SimplePortals.blockPortalFrame);
		SimplePortals.itemPowerGauge = new ItemPowerGauge(SimplePortals.blockPowerGauge);
		SimplePortals.itemPortalActivator = new ItemPortalActivator();

		event.getRegistry().registerAll(
			SimplePortals.itemPortalFrame,
			SimplePortals.itemPowerGauge,
			SimplePortals.itemPortalActivator
		);
	}
}
