package net.zarathul.simpleportals.commands;

import com.google.common.collect.ImmutableListMultimap;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.BlockPosArgument;
import net.minecraft.command.arguments.DimensionArgument;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.dimension.DimensionType;
import net.zarathul.simpleportals.SimplePortals;
import net.zarathul.simpleportals.commands.arguments.BlockArgument;
import net.zarathul.simpleportals.configuration.Config;
import net.zarathul.simpleportals.registration.Address;
import net.zarathul.simpleportals.registration.Portal;
import net.zarathul.simpleportals.registration.PortalRegistry;

import java.util.*;
import java.util.stream.Collectors;

public class CommandPortals
{
	private enum ListMode
	{
		All,
		Address,
		Dimension
	}

	private enum DeactiveMode
	{
		Address,
		Position
	}

	private enum PowerMode
	{
		Add,
		Remove,
		Get,
		Items
	}

	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		dispatcher.register(
			Commands.literal("sportals")
				.requires((commandSource) -> {
					return commandSource.hasPermissionLevel(4);
				})
				.executes(context -> {
					SendTranslatedMessage(context.getSource(), "commands.sportals.info");
					return 1;
				})
				.then(
					Commands.literal("list")
						.executes(context -> {
							SendTranslatedMessage(context.getSource(), "commands.sportals.list.info");
							return 1;
						})
						.then(
							Commands.literal("all")		// sportals list all
								.executes(context -> {
									return list(context.getSource(), ListMode.All, null, null);
								})
						)
						.then(
							Commands.argument("address1", BlockArgument.block())
								.then(
									Commands.argument("address2", BlockArgument.block())
										.then(
											Commands.argument("address3", BlockArgument.block())
												.then(
													Commands.argument("address4", BlockArgument.block())		// sportals list <addressBlockId> <addressBlockId> <addressBlockId> <addressBlockId>
														.executes(context -> {
															Address address = new Address(PortalRegistry.getAddressBlockId(BlockArgument.getBlock(context, "address1")),
																PortalRegistry.getAddressBlockId(BlockArgument.getBlock(context, "address2")),
																PortalRegistry.getAddressBlockId(BlockArgument.getBlock(context, "address3")),
																PortalRegistry.getAddressBlockId(BlockArgument.getBlock(context, "address4")));

															return list(context.getSource(), ListMode.Address, address, null);
														})
												)
										)
								)
						)
						.then(
							Commands.argument("dimension", DimensionArgument.getDimension())		// sportals list <dimension>
								.executes(context -> {
									return list(context.getSource(), ListMode.Dimension, null, DimensionArgument.getDimensionArgument(context, "dimension"));
								})
						)
				)
				.then(
					Commands.literal("deactivate")
						.executes(context -> {
							SendTranslatedMessage(context.getSource(), "commands.sportals.deactivate.info");
							return 1;
						})
						.then(
							Commands.argument("address1", BlockArgument.block())
								.then(
									Commands.argument("address2", BlockArgument.block())
										.then(
											Commands.argument("address3", BlockArgument.block())
												.then(
													Commands.argument("address4", BlockArgument.block())
														.executes(context -> {
															Address address = new Address(PortalRegistry.getAddressBlockId(BlockArgument.getBlock(context, "address1")),
																	PortalRegistry.getAddressBlockId(BlockArgument.getBlock(context, "address2")),
																	PortalRegistry.getAddressBlockId(BlockArgument.getBlock(context, "address3")),
																	PortalRegistry.getAddressBlockId(BlockArgument.getBlock(context, "address4")));

															return deactivate(context.getSource(), DeactiveMode.Address, address, null, null);
														})
														.then(
															Commands.argument("dimension", DimensionArgument.getDimension())		// sportals deactivate <addressBlockId> <addressBlockId> <addressBlockId> <addressBlockId> [dimension]
																.executes(context -> {
																	Address address = new Address(PortalRegistry.getAddressBlockId(BlockArgument.getBlock(context, "address1")),
																			PortalRegistry.getAddressBlockId(BlockArgument.getBlock(context, "address2")),
																			PortalRegistry.getAddressBlockId(BlockArgument.getBlock(context, "address3")),
																			PortalRegistry.getAddressBlockId(BlockArgument.getBlock(context, "address4")));

																	DimensionType dimension = DimensionArgument.getDimensionArgument(context, "dimension");

																	return deactivate(context.getSource(), DeactiveMode.Address, address, null, dimension);
																})
														)
												)
										)
								)
						)
						.then(
							Commands.argument("position", BlockPosArgument.blockPos())
								.executes(context -> {
									return deactivate(context.getSource(), DeactiveMode.Position, null, BlockPosArgument.getBlockPos(context, "position"), null);
								})
								.then(
									Commands.argument("dimension", DimensionArgument.getDimension())		// sportals deactivate <x> <y> <z> [dimension]
										.executes(context -> {
											return deactivate(context.getSource(), DeactiveMode.Position, null, BlockPosArgument.getBlockPos(context, "position"), DimensionArgument.getDimensionArgument(context, "dimension"));
										})
								)
						)
				)
				.then(
					Commands.literal("power")
						.executes(context -> {
							SendTranslatedMessage(context.getSource(), "commands.sportals.power.info");
							return 1;
						})
						.then(
							Commands.literal("add")
								.then(
									Commands.argument("amount", IntegerArgumentType.integer(1))
										.then(
											Commands.argument("position", BlockPosArgument.blockPos())
												.executes(context -> {
													return power(context.getSource(), PowerMode.Add, IntegerArgumentType.getInteger(context, "amount"), BlockPosArgument.getBlockPos(context, "position"), null);
												})
												.then(
													Commands.argument("dimension", DimensionArgument.getDimension())		// sportals power add <amount> <x> <y> <z> [dimension]
														.executes(context -> {
															return power(context.getSource(), PowerMode.Add, IntegerArgumentType.getInteger(context, "amount"), BlockPosArgument.getBlockPos(context, "position"), DimensionArgument.getDimensionArgument(context, "dimension"));
														})
												)
										)
								)
						)
						.then(
							Commands.literal("remove")
								.then(
									Commands.argument("amount", IntegerArgumentType.integer(1))
										.then(
											Commands.argument("position", BlockPosArgument.blockPos())
												.executes(context -> {
													return power(context.getSource(), PowerMode.Remove, IntegerArgumentType.getInteger(context, "amount"), BlockPosArgument.getBlockPos(context, "position"), null);
												})
												.then(
													Commands.argument("dimension", DimensionArgument.getDimension())		// sportals power remove <amount> <x> <y> <z> [dimension]
														.executes(context -> {
															return power(context.getSource(), PowerMode.Remove, IntegerArgumentType.getInteger(context, "amount"), BlockPosArgument.getBlockPos(context, "position"), DimensionArgument.getDimensionArgument(context, "dimension"));
														})
												)
										)
								)
						)
						.then(
							Commands.literal("get")
								.then(
									Commands.argument("position", BlockPosArgument.blockPos())
										.executes(context -> {
											return power(context.getSource(), PowerMode.Get, 0, BlockPosArgument.getBlockPos(context, "position"), null);
										})
										.then(
											Commands.argument("dimension", DimensionArgument.getDimension())		// sportals power get <x> <y> <z> [dimension]
												.executes(context -> {
													return power(context.getSource(), PowerMode.Get, 0, BlockPosArgument.getBlockPos(context, "position"), DimensionArgument.getDimensionArgument(context, "dimension"));
												})
										)
								)
						)
						.then(
							Commands.literal("items")
								.executes(context -> {
									Tag<Item> powerTag = ItemTags.getCollection().get(Config.powerSource);

									if (powerTag == null)
									{
										SendTranslatedMessage(context.getSource(), "commands.errors.no_power_items", Config.powerSource);
										return 1;
									}

									Collection<Item> itemsWithPowerTag = powerTag.getAllElements();

									if (itemsWithPowerTag.size() == 0)
									{
										SendTranslatedMessage(context.getSource(), "commands.errors.no_power_items", Config.powerSource);
										return 1;
									}

									SendTranslatedMessage(context.getSource(), "commands.sportals.power.items.success", itemsWithPowerTag.size());

									for (Item powerSource : itemsWithPowerTag)
									{
										SendTranslatedMessage(context.getSource(), powerSource.getTranslationKey());
									}

									return 1;
								})
						)
				)
				.then(
					Commands.literal("cooldown")
						.executes(context -> {
							SendTranslatedMessage(context.getSource(), "commands.sportals.cooldown.info");
							return 1;
						})
						.then(
							Commands.argument("player", EntityArgument.player())		// sportals cooldown <player>
								.executes(context -> {
									return cooldown(context.getSource(), EntityArgument.getPlayer(context, "player"));
								})
						)
				)
				.then(
					Commands.literal("clear")
						.executes(context -> {
							SendTranslatedMessage(context.getSource(), "commands.sportals.clear.info");
							return 1;
						})
						.then(
							Commands.literal("confirmed")		// sportals clear confirmed
								.executes(context -> {
									return clear(context.getSource());
								})
						)
				)
		);
	}

	private static int list(CommandSource source, ListMode mode, Address address, DimensionType dimension)
	{
		List<Portal> portals;

		switch (mode)
		{
			case All:
				// sportals list all
				ImmutableListMultimap<Address, Portal> addresses = PortalRegistry.getAddresses();

				Set<Address> uniqueAddresses = new HashSet<>(addresses.keys());
				List<Portal> portalsForAddress;
				portals = new ArrayList<>();

				for (Address addr : uniqueAddresses)
				{
					portalsForAddress = addresses.get(addr);
					portals.addAll(portalsForAddress);
				}

				break;

			case Address:
				// sportals list <addressBlockId> <addressBlockId> <addressBlockId> <addressBlockId>
				portals = PortalRegistry.getPortalsWithAddress(address);
				break;

			case Dimension:
				// sportals list <dimension>
				portals = PortalRegistry.getPortalsInDimension(dimension);
				break;

			default:
				portals = new ArrayList<>();
		}

		SimplePortals.log.info("Registered portals");
		SimplePortals.log.info("|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|");
		SimplePortals.log.info("| Dimension                                | Position                    | Power | Address                                                                                                                                                |");
		SimplePortals.log.info("|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|");

		BlockPos portalBlockPos;
		String formattedPortalBlockPos;

		for (Portal portal : portals)
		{
			portalBlockPos = portal.getCorner1().getPos();
			formattedPortalBlockPos = String.format("x=%d, y=%d, z=%d", portalBlockPos.getX(), portalBlockPos.getY(), portalBlockPos.getZ());

			SimplePortals.log.info(String.format("| %40s | %27s | %5d | %-150s |",
				portal.getDimension().getRegistryName(), formattedPortalBlockPos, PortalRegistry.getPower(portal), portal.getAddress()));
		}

		SimplePortals.log.info("|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|");

		SendTranslatedMessage(source, "commands.sportals.list.success");

		return 1;
	}

	private static int deactivate(CommandSource source, DeactiveMode mode, Address address, BlockPos pos, DimensionType dimension)
	{
		List<Portal> portals = null;

		switch (mode)
		{
			case Address:
				// sportals deactivate <addressBlockId> <addressBlockId> <addressBlockId> <addressBlockId> [dimension]
				portals = PortalRegistry.getPortalsWithAddress(address);

				if (portals == null || portals.size() == 0)
				{
					if (dimension != null)
					{
						SendTranslatedMessage(source, "commands.errors.portal_not_found_with_address_in_dimension", address, dimension.getRegistryName());
					}
					else
					{
						SendTranslatedMessage(source, "commands.errors.portal_not_found_with_address", address);
					}

					return 0;
				}

				if (dimension != null)
				{
					// filter out all portals that are not in the specified dimension
					final DimensionType dimensionCopy = dimension;	// This is necessary because Java wants closures in lambda expressions to be effectively final.
					portals = portals.stream().filter((portal -> portal.getDimension() == dimensionCopy)).collect(Collectors.toList());
				}

				break;

			case Position:
				// sportals deactivate <x> <y> <z> [dimension]
				if (dimension == null)
				{
					try
					{
						// Get the dimension the command sender is currently in.
						ServerPlayerEntity player = source.asPlayer();
						dimension = player.dimension;
					}
					catch (CommandSyntaxException ex)
					{
						throw new CommandException(new TranslationTextComponent("commands.errors.unknown_sender_dimension"));
					}
				}

				portals = PortalRegistry.getPortalsAt(pos, dimension);
				if (portals == null || portals.size() == 0)	throw new CommandException(new TranslationTextComponent("commands.errors.portal_not_found_at_pos_in_dimension", pos.getX(), pos.getY(), pos.getZ(), dimension.getRegistryName()));

				break;
		}

		BlockPos portalPos;

		for (Portal portal : portals)
		{
			portalPos = portal.getCorner1().getPos();
			DimensionType dimType = portal.getDimension();
			if (dimType == null) throw new CommandException(new TranslationTextComponent("commands.errors.missing_dimension", portal.getDimension()));

			PortalRegistry.deactivatePortal(source.getServer().getWorld(dimType), portalPos);
			SendTranslatedMessage(source, "commands.sportals.deactivate.success", portalPos.getX(), portalPos.getY(), portalPos.getZ(), dimType.getRegistryName());
		}

		return 1;
	}

	private static int power(CommandSource source, PowerMode mode, int amount, BlockPos pos, DimensionType dimension)
	{
		if (dimension == null)
		{
			// Get the dimension the command sender is currently in.
			try
			{
				ServerPlayerEntity player = source.asPlayer();
				dimension = player.dimension;
			}
			catch (CommandSyntaxException ex)
			{
				throw new CommandException(new TranslationTextComponent("commands.errors.unknown_sender_dimension"));
			}
		}

		List<Portal> portals = PortalRegistry.getPortalsAt(pos, dimension);

		if (portals == null || portals.size() == 0)
		{
			throw new CommandException(new TranslationTextComponent("commands.errors.portal_not_found_at_pos_in_dimension", pos.getX(), pos.getY(), pos.getZ(), dimension.getRegistryName()));
		}
		else if (portals.size() > 1)
		{
			throw new CommandException(new TranslationTextComponent("commands.errors.multiple_portals_found_at_pos_in_dimension", pos.getX(), pos.getY(), pos.getZ(), dimension.getRegistryName()));
		}

		Portal portal = portals.get(0);

		switch (mode)
		{
			case Add:
				// sportals power add <amount> <x> <y> <z> [dimension]
				amount = amount - PortalRegistry.addPower(portal, amount);
				SendTranslatedMessage(source, "commands.sportals.power.add.success", amount, pos.getX(), pos.getY(), pos.getZ(), dimension.getRegistryName());
				break;

			case Remove:
				// sportals power remove <amount> <x> <y> <z> [dimension]
				amount = Math.min(amount, PortalRegistry.getPower(portal));
				amount = (PortalRegistry.removePower(portal, amount)) ? amount : 0;
				SendTranslatedMessage(source, "commands.sportals.power.remove.success", amount, pos.getX(), pos.getY(), pos.getZ(), dimension.getRegistryName());
				break;

			case Get:
				// sportals power get <x> <y> <z> [dimension]
				amount = PortalRegistry.getPower(portal);
				SendTranslatedMessage(source, "commands.sportals.power.get.success", pos.getX(), pos.getY(), pos.getZ(), dimension.getRegistryName(), amount);
				break;
		}

		return 1;
	}

	private static int cooldown(CommandSource source, ServerPlayerEntity target)
	{
		// sportals cooldown <player>
		SendTranslatedMessage(source, "commands.sportals.cooldown.success", target.getName(), target.timeUntilPortal, target.timeUntilPortal / 20f);	// This assumes normal tickrate of 20 (TPS).

		return 1;
	}

	private static int clear(CommandSource source)
	{
		// sportals clear confirmed
		PortalRegistry.clear();
		SendTranslatedMessage(source, "commands.sportals.clear.success");

		return 1;
	}

	private static void SendTranslatedMessage(CommandSource source, String message, Object... args)
	{
		source.sendFeedback(new TranslationTextComponent(message, args), true);
	}
}
