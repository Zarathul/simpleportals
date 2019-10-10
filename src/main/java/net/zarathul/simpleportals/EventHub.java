package net.zarathul.simpleportals;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.block.Block;
import net.minecraft.command.CommandSource;
import net.minecraft.command.arguments.ArgumentSerializer;
import net.minecraft.command.arguments.ArgumentTypes;
import net.minecraft.item.Item;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.world.WorldEvent.Load;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.zarathul.simpleportals.blocks.BlockPortal;
import net.zarathul.simpleportals.blocks.BlockPortalFrame;
import net.zarathul.simpleportals.blocks.BlockPowerGauge;
import net.zarathul.simpleportals.commands.CommandPortals;
import net.zarathul.simpleportals.commands.CommandTeleport;
import net.zarathul.simpleportals.commands.arguments.BlockArgument;
import net.zarathul.simpleportals.common.PortalWorldSaveData;
import net.zarathul.simpleportals.configuration.Config;
import net.zarathul.simpleportals.items.ItemPortalActivator;
import net.zarathul.simpleportals.items.ItemPortalFrame;
import net.zarathul.simpleportals.items.ItemPowerGauge;

/**
 * Hosts Forge event handlers on both the server and client side.
 */
public final class EventHub
{
	// Common

	@SubscribeEvent
	public static void onConfigLoaded(ModConfig.Loading event)
	{
		Config.updateValidPowerSources();
	}

	@SubscribeEvent
	public static void onConfigChanged(ModConfig.ConfigReloading event)
	{
		Config.updateValidPowerSources();
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

	// Server

	@SubscribeEvent
	public static void onServerStarting(FMLServerStartingEvent event)
	{
		CommandPortals.register(event.getCommandDispatcher());
		CommandTeleport.register(event.getCommandDispatcher());
	}
}
