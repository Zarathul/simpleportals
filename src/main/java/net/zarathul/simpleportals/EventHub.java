package net.zarathul.simpleportals;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.item.Item;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.world.WorldEvent.Load;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.zarathul.simpleportals.blocks.BlockPortal;
import net.zarathul.simpleportals.blocks.BlockPortalFrame;
import net.zarathul.simpleportals.blocks.BlockPowerGauge;
import net.zarathul.simpleportals.commands.CommandPortals;
import net.zarathul.simpleportals.commands.CommandTeleport;
import net.zarathul.simpleportals.common.PortalWorldSaveData;
import net.zarathul.simpleportals.common.TeleportTask;
import net.zarathul.simpleportals.common.Utils;
import net.zarathul.simpleportals.configuration.Config;
import net.zarathul.simpleportals.items.ItemPortalActivator;
import net.zarathul.simpleportals.items.ItemPortalFrame;
import net.zarathul.simpleportals.items.ItemPowerGauge;
import net.zarathul.simpleportals.theoneprobe.TheOneProbeCompat;

/**
 * Hosts Forge event handlers on both the server and client side.
 */
public final class EventHub
{
	@SubscribeEvent
	public static void onServerStarting(FMLServerStartingEvent event)
	{
		CommandPortals.register(event.getCommandDispatcher());
		CommandTeleport.register(event.getCommandDispatcher());
	}

	@SubscribeEvent
	public static void onInteropSetup(InterModEnqueueEvent event)
	{
		if (ModList.get().isLoaded("theoneprobe"))
		{
			SimplePortals.log.debug("Sending compatibility request to TheOneProbe.");
			InterModComms.sendTo("theoneprobe", "getTheOneProbe", () -> new TheOneProbeCompat());
		}
		else
		{
			SimplePortals.log.debug("TheOneProbe not found. Skipping compatibility request.");
		}
	}

	@SubscribeEvent
	public static void onServerTick(TickEvent.ServerTickEvent event)
	{
		if (event.phase == TickEvent.Phase.END)
		{
			TeleportTask task;
			MinecraftServer mcServer;

			while (true)
			{
				task = SimplePortals.TELEPORT_QUEUE.peek();
				if (task == null) return;

				mcServer = task.player.getServer();
				if (mcServer == null)
				{
					// No point in keeping the task if there's no server. Should never happen but who knows.
					SimplePortals.TELEPORT_QUEUE.poll();
				}
				else if (mcServer.getTickCounter() > (task.creationTickCount + Config.playerTeleportationDelay.get()))
				{
					// Task is due.
					SimplePortals.TELEPORT_QUEUE.poll();
					Utils.teleportTo(task.player, task.dimension, task.pos, task.facing);
				}
				else
				{
					// Task was not due yet, so if there are others they won't be either.
					return;
				}
			}
		}
	}

	@SubscribeEvent
	public static void onConfigLoaded(ModConfig.Loading event)
	{
		if (event.getConfig().getType() == ModConfig.Type.COMMON)
		{
			Config.updatePowerSource();
		}
	}

	@SubscribeEvent
	public static void onConfigChanged(ModConfig.Reloading event)
	{
		if (event.getConfig().getType() == ModConfig.Type.COMMON)
		{
			Config.updatePowerSource();
		}
	}

	@SubscribeEvent
	public static void OnWorldLoad(Load event)
	{
		World world = event.getWorld().getWorld();

		// WorldSavedData can no longer be stored per map but only per dimension. So store the registry in the overworld.
		if (!world.isRemote && world.dimension.getType() == DimensionType.OVERWORLD && world instanceof ServerWorld)
		{
			SimplePortals.portalSaveData = PortalWorldSaveData.get((ServerWorld)world);
		}
	}

	@SubscribeEvent
	public static void OnBlockRegistration(RegistryEvent.Register<Block> event)
	{
		SimplePortals.blockPortal = new BlockPortal();
		SimplePortals.blockPortalFrame = new BlockPortalFrame();
		SimplePortals.blockPowerGauge = new BlockPowerGauge();

		event.getRegistry().registerAll(
			SimplePortals.blockPortal,
			SimplePortals.blockPortalFrame,
			SimplePortals.blockPowerGauge
		);

		DistExecutor.callWhenOn(Dist.CLIENT, () ->
				() -> {
					RenderTypeLookup.setRenderLayer(SimplePortals.blockPortal, RenderType.getTranslucent());
					return null;
				}
		);
	}

	@SubscribeEvent
	public static void OnItemRegistration(RegistryEvent.Register<Item> event)
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
