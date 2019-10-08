package net.zarathul.simpleportals;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.block.Block;
import net.minecraft.command.CommandSource;
import net.minecraft.command.arguments.ArgumentSerializer;
import net.minecraft.command.arguments.ArgumentTypes;
import net.minecraft.item.Item;
import net.minecraft.world.World;
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
import net.zarathul.simpleportals.commands.arguments.AddressArgument;
import net.zarathul.simpleportals.common.PortalWorldSaveData;
import net.zarathul.simpleportals.configuration.Config;
import net.zarathul.simpleportals.items.ItemPortalActivator;
import net.zarathul.simpleportals.items.ItemPortalFrame;
import net.zarathul.simpleportals.items.ItemPowerGauge;
import org.apache.logging.log4j.core.jmx.Server;

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
	public void OnWorldLoad(Load event)
	{
		World world = event.getWorld().getWorld();

		if (!world.isRemote)
		{
			SimplePortals.portalSaveData = PortalWorldSaveData.get((ServerWorld)world);
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

	// Client

	/*
	@SubscribeEvent
	public void OnModelRegistration(ModelRegistryEvent event)
	{
		// register item models
		ModelLoader.setCustomModelResourceLocation(SimplePortals.itemPortalFrame, 0, new ModelResourceLocation(SimplePortals.blockPortalFrame.getRegistryName(), "inventory"));
		ModelLoader.setCustomModelResourceLocation(SimplePortals.itemPowerGauge, 0, new ModelResourceLocation(SimplePortals.blockPowerGauge.getRegistryName(), "inventory"));
		ModelLoader.setCustomModelResourceLocation(SimplePortals.itemPortalActivator, 0, new ModelResourceLocation(SimplePortals.itemPortalActivator.getRegistryName(), "inventory"));
	}
	 */

	// Server

	@SubscribeEvent
	public void onServerStarting(FMLServerStartingEvent event)
	{
		ArgumentTypes.register("sportals_address", AddressArgument.class, new ArgumentSerializer<>(AddressArgument::create));
		CommandDispatcher<CommandSource> dispatcher = event.getCommandDispatcher();
		CommandPortals.register(dispatcher);
	}
}
