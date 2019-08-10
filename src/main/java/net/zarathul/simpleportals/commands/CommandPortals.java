package net.zarathul.simpleportals.commands;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Iterables;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.*;
import net.minecraft.command.arguments.BlockPosArgument;
import net.minecraft.command.arguments.DimensionArgument;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.dimension.DimensionType;
import net.zarathul.simpleportals.SimplePortals;
import net.zarathul.simpleportals.commands.arguments.AddressArgument;
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
		Get
	}

	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		dispatcher.register(Commands.literal("sportals").requires((commandSource) -> {
			return commandSource.hasPermissionLevel(4);
		})
		.then(Commands.literal("list")				// sportals list all
			.then(Commands.literal("all")).executes((context) -> {
				return list(context.getSource(),
						ListMode.All,
						null,
						null);
			})
			.then(Commands.literal("address"))		// sportals list address <addressBlockId> <addressBlockId> <addressBlockId> <addressBlockId>
				.then(Commands.argument("addr", AddressArgument.create())).executes(context -> {
				return list(context.getSource(),
						ListMode.Address,
						AddressArgument.getValue(context, "addr"),
						null);
			})
			.then(Commands.literal("dimension"))	// sportals list dimension <dimensionId>
				.then(Commands.argument("dim", DimensionArgument.getDimension())).executes((context) -> {
				return list(context.getSource(),
						ListMode.Dimension,
						null,
						DimensionArgument.func_212592_a(context, "dim"));
			})
		.then(Commands.literal("deactivate"))
			.then(Commands.literal("address"))		// sportals deactivate address <addressBlockId> <addressBlockId> <addressBlockId> <addressBlockId> [dimensionId]
				.then(Commands.argument("addr", AddressArgument.create()))
				.then(Commands.argument("dim", DimensionArgument.getDimension())).executes((context) -> {
					return deactivate(context.getSource(),
							DeactiveMode.Address,
							AddressArgument.getValue(context, "addr"),
							null,
							DimensionArgument.func_212592_a(context, "dim"));
				})
			.then(Commands.literal("position"))		// sportals deactivate position <x> <y> <z> [dimensionId]
				.then(Commands.argument("pos", BlockPosArgument.blockPos()))
				.then(Commands.argument("dim", DimensionArgument.getDimension())).executes((context) -> {
					return deactivate(context.getSource(),
							DeactiveMode.Position,
							null,
							BlockPosArgument.getBlockPos(context, "pos"),
							DimensionArgument.func_212592_a(context, "dim"));
				})
		.then(Commands.literal("power")
			.then(Commands.literal("add"))		// sportals power add <amount> <x> <y> <z> [dimension]
				.then(Commands.argument("amount", IntegerArgumentType.integer(1)))
				.then(Commands.argument("pos", BlockPosArgument.blockPos()))
				.then(Commands.argument("dim", DimensionArgument.getDimension()).executes(context -> {
					return power(context.getSource(),
							PowerMode.Add,
							IntegerArgumentType.getInteger(context, "amount"),
							BlockPosArgument.getBlockPos(context, "pos"),
							DimensionArgument.func_212592_a(context, "dim"));
				})
			.then(Commands.literal("remove"))	// sportals power remove <amount> <x> <y> <z> [dimension]
				.then(Commands.argument("amount", IntegerArgumentType.integer(1)))
				.then(Commands.argument("pos", BlockPosArgument.blockPos()))
				.then(Commands.argument("dim", DimensionArgument.getDimension()).executes(context -> {
					return power(context.getSource(),
							PowerMode.Remove,
							IntegerArgumentType.getInteger(context, "amount"),
							BlockPosArgument.getBlockPos(context, "pos"),
							DimensionArgument.func_212592_a(context, "dim"));
				})
			.then(Commands.literal("get"))		// sportals power get <x> <y> <z> [dimension]
				.then(Commands.argument("pos", BlockPosArgument.blockPos()))
				.then(Commands.argument("dim", DimensionArgument.getDimension()).executes(context -> {
					return power(context.getSource(),
							PowerMode.Get,
							0,
							BlockPosArgument.getBlockPos(context, "pos"),
							DimensionArgument.func_212592_a(context, "dim"));
				})
		.then(Commands.literal("cooldown")		// sportals cooldown <playerName>
			.then(Commands.argument("target", EntityArgument.player())).executes(context  -> {
				ServerPlayerEntity player = EntityArgument.getPlayer(context, "target");
				return cooldown(context.getSource(), player.getName().getString(), player);
			})
		.then(Commands.literal("clear")			// sportals clear confirmed
			.then(Commands.literal("confirmed")).executes((context) -> {
				return clear(context.getSource());
		})
		))))))));
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
				// sportals list address <addressBlockId> <addressBlockId> <addressBlockId> <addressBlockId>
				portals = PortalRegistry.getPortalsWithAddress(address);
				break;

			case Dimension:
				// sportals list dimension <dimensionId>
				portals = PortalRegistry.getPortalsInDimension(dimension.getId());
				break;

			default:
				portals = new ArrayList<>();	// Shut up the compiler.
		}

		SimplePortals.log.info("Registered portals");
		SimplePortals.log.info("|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|");
		SimplePortals.log.info("| Dimension | Position                         | Power | Address                                                                                                                                                |");
		SimplePortals.log.info("|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|");

		for (Portal portal : portals)
		{
			SimplePortals.log.info(String.format("| %9s | %32s | %5d | %-150s |",
					portal.getDimension(), portal.getCorner1().getPos(), PortalRegistry.getPower(portal), portal.getAddress()));
		}

		SimplePortals.log.info("|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|");

		SendTranslatedMessage(source, "commands.sportals.list.success");

		// FIXME: No idea what is expected here. From what I have seen greater than zero seems to mean success?
		return 1;
	}

	private static int deactivate(CommandSource source, DeactiveMode mode, Address address, BlockPos pos, DimensionType dimension)
	{
		List<Portal> portals = null;

		switch (mode)
		{
			case Address:
				// sportals deactivate address <addressBlockId> <addressBlockId> <addressBlockId> <addressBlockId> [dimensionId]
				portals = PortalRegistry.getPortalsWithAddress(address);

				if (dimension != null) portals = portals.stream().filter((portal -> portal.getDimension() == dimension.getId())).collect(Collectors.toList());
				if (portals.size() == 0)
				{
					SendTranslatedMessage(source, "commands.sportals.deactivate.portalNotFoundWithAddress", address, (dimension != null) ? dimension.getId() : "-");
					return 1;	// FIXME: No idea what is expected here. From what I have seen greater than zero seems to mean success?
				}

				break;

			case Position:
				// sportals deactivate position <x> <y> <z> [dimensionId]
				int dimensionId;

				if (dimension != null)
				{
					dimensionId = dimension.getId();
				}
				else
				{
					// Get the dimension the command sender is currently in.
					ServerPlayerEntity player = null;
					// Really ? WHY?
					try { player = source.asPlayer(); } catch (CommandSyntaxException e){};

					if (player != null)
					{
						dimensionId = player.dimension.getId();
					}
					else
					{
						throw new CommandException(new TranslationTextComponent("commands.sportals.deactivate.unknownSender"));
					}
				}

				portals = PortalRegistry.getPortalsAt(pos, dimensionId);
				if (portals.size() == 0) throw new CommandException(new TranslationTextComponent("commands.sportals.deactivate.portalNotFoundAtPos", pos, dimensionId));
				break;
		}

		BlockPos portalPos;

		for (Portal portal : portals)
		{
			portalPos = Iterables.getFirst(portal.getPortalPositions(), null);

			// This should never happen, because active portals should always have portal blocks.
			// Well it's Minecraft so better check it and fall back to the position of corner 1 if needed.
			if (portalPos == null) portalPos = portal.getCorner1().getPos();

			DimensionType dimType = DimensionType.getById(portal.getDimension());
			if (dimType == null) continue;	// FIXME: This should also not happen, unless dimensions get removed.

			PortalRegistry.deactivatePortal(source.getServer().getWorld(dimType), portalPos);
			SendTranslatedMessage(source, "commands.sportals.deactivate.success", portalPos, portal.getDimension());
		}

		return 1;	// FIXME: No idea what is expected here. From what I have seen greater than zero seems to mean success?
	}

	private static int power(CommandSource source, PowerMode mode, int amount, BlockPos pos, DimensionType dimension)
	{
		int dimensionId;

		if (dimension != null)
		{
			dimensionId = dimension.getId();
		}
		else
		{
			// Get the dimension the command sender is currently in.
			ServerPlayerEntity player = null;
			// Really ? WHY?
			try { player = source.asPlayer(); } catch (CommandSyntaxException e){};

			if (player != null)
			{
				dimensionId = player.dimension.getId();
			}
			else
			{
				throw new CommandException(new TranslationTextComponent("commands.sportals.power.unknownSender"));
			}
		}

		List<Portal> portals = PortalRegistry.getPortalsAt(pos, dimensionId);

		if (portals.size() == 0)
		{
			throw new CommandException(new TranslationTextComponent("commands.sportals.power.portalNotFound", pos, dimensionId));
		}
		else if (portals.size() > 1)
		{
			throw new CommandException(new TranslationTextComponent("commands.sportals.power.multiplePortalsFound", pos, dimensionId));
		}

		Portal portal = portals.get(0);

		switch (mode)
		{
			case Add:
				// sportals power add <amount> <x> <y> <z> [dimension]
				amount = amount - PortalRegistry.addPower(portal, amount);
				SendTranslatedMessage(source, "commands.sportals.power.add.success", pos, dimensionId, amount);
				break;

			case Remove:
				// sportals power remove <amount> <x> <y> <z> [dimension]
				amount = Math.min(amount, PortalRegistry.getPower(portal));
				amount = (PortalRegistry.removePower(portal, amount)) ? amount : 0;
				SendTranslatedMessage(source, "commands.sportals.power.remove.success", pos, dimensionId, amount);
				break;

			case Get:
				// sportals power get <x> <y> <z> [dimension]
				amount = PortalRegistry.getPower(portal);
				SendTranslatedMessage(source, "commands.sportals.power.get.success", pos, dimensionId, amount);
				break;
		}

		return 1;	// FIXME: No idea what is expected here. From what I have seen greater than zero seems to mean success?
	}

	private static int cooldown(CommandSource source, String targetName, ServerPlayerEntity target)
	{
		//ServerPlayerEntity player = source.getServer().getPlayerList().getPlayerByUsername(args[1]);

		// sportals cooldown <playerName>
		if (target == null)
			SendTranslatedMessage(source, "commands.tpd.unknownPlayerName", targetName);
		else
			SendTranslatedMessage(source, "commands.sportals.cooldown.success", target.getName(), target.timeUntilPortal, target.timeUntilPortal / 20f);

		return 1;	// FIXME: No idea what is expected here. From what I have seen greater than zero seems to mean success?
	}

	private static int clear(CommandSource source)
	{
		// sportals clear confirmed
		Set<Portal> portals = new HashSet<>(PortalRegistry.getPortals().values());

		for (Portal portal : portals)
		{
			DimensionType dimType = DimensionType.getById(portal.getDimension());
			if (dimType == null) continue;

			PortalRegistry.deactivatePortal(source.getServer().getWorld(dimType), portal.getCorner1().getPos());
		}

		SendTranslatedMessage(source, "commands.sportals.clear.success");

		return 1;	// FIXME: No idea what is expected here. From what I have seen greater than zero seems to mean success?
	}

	private static void SendTranslatedMessage(CommandSource source, String message, Object... args)
	{
		ServerPlayerEntity player = null;
		// Really ? WHY?
		try { player = source.asPlayer(); } catch (CommandSyntaxException e){};

		if (player != null)
		{
			// FIXME: Not sure if 'TranslationTextComponent' does what I think it does. We'll see if it actually formats properly.
			player.sendMessage(new TranslationTextComponent(message, args), ChatType.SYSTEM);
		}
	}

	/*

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos)
	{
		if (args.length == 1)
		{
			return getListOfStringsMatchingLastWord(args, "list", "clear", "deactivate", "power", "cooldown");
		}
		else if (args.length == 2 && "list".equals(args[0]))
		{
			return getListOfStringsMatchingLastWord(args, "all", "address", "dimension");
		}
		else if (args.length == 2 && "deactivate".equals(args[0]))
		{
			return getListOfStringsMatchingLastWord(args, "address", "position");
		}
		else if (args.length == 2 && "power".equals(args[0]))
		{
			return getListOfStringsMatchingLastWord(args, "add", "remove", "get");
		}
		else if (args.length == 2 && "cooldown".equals(args[0]))
		{
			return getListOfStringsMatchingLastWord(args, server.getPlayerList().getOnlinePlayerNames());
		}
		else if (args.length >= 3 && args.length <= 7 && "deactivate".equals(args[0]) && "address".equals(args[1]))
		{
			if (args.length == 7)
			{
				return getListOfStringsMatchingLastWord(args, Arrays.asList(DimensionManager.getIDs()));
			}
			else
			{
				return getListOfStringsMatchingLastWord(args,
					getTabCompletionBlockAddressId(server, sender.getCommandSenderEntity(), targetPos));
			}
		}
		else if (args.length >= 3 && args.length <= 6 && "deactivate".equals(args[0]) && "position".equals(args[1]))
		{
			if (args.length == 6)
			{
				return getListOfStringsMatchingLastWord(args, Arrays.asList(DimensionManager.getIDs()));
			}
			else if (args.length == 3)
			{
				return getListOfStringsMatchingLastWord(args,
					(targetPos != null) ? String.format("%d %d %d", targetPos.getX(), targetPos.getY(), targetPos.getZ()) : "");
			}
		}
		else if (args.length == 3 && "list".equals(args[0]) && "dimension".equals(args[1]))
		{
			return getListOfStringsMatchingLastWord(args, Arrays.asList(DimensionManager.getIDs()));
		}
		else if (args.length >= 3 && args.length <= 6 && "list".equals(args[0]) && "address".equals(args[1]))
		{
			return getListOfStringsMatchingLastWord(args, getTabCompletionBlockAddressId(server, sender.getCommandSenderEntity(), targetPos));
		}
		else if ((args.length == 3 || args.length == 4 || args.length == 6 || args.length == 7) && "power".equals(args[0]))
		{
			if ((args.length == 3 && "get".equals(args[1])) || (args.length == 4 && ("add".equals(args[1]) || "remove".equals(args[1]))))
			{
				return getListOfStringsMatchingLastWord(args,
					(targetPos != null) ? String.format("%d %d %d", targetPos.getX(), targetPos.getY(), targetPos.getZ()) : "");
			}
			else if ((args.length == 6 && "get".equals(args[1])) || (args.length == 7 && ("add".equals(args[1]) || "remove".equals(args[1]))))
			{
				return getListOfStringsMatchingLastWord(args, Arrays.asList(DimensionManager.getIDs()));
			}
		}

		return super.getTabCompletions(server, sender, args, targetPos);
	}

	private String getTabCompletionBlockAddressId(MinecraftServer server, Entity sender, BlockPos pos)
	{
		if (server == null || sender == null || pos == null) return "";

		WorldServer world = server.getWorld(sender.dimension);
		IBlockState block = world.getBlockState(pos);
		String address = PortalRegistry.getAddressBlockId(block);

		return (address != null) ? address : "";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
	{
		if (args.length < 1)
		{
			// sportals <clear | cooldown | deactivate | list | power>
			throw new WrongUsageException("commands.sportals.usage");
		}

		if ("clear".equals(args[0]))
		{
			if (args.length < 2 || !"confirmed".equals(args[1]))
			{
				// sportals clear confirmed
				throw new WrongUsageException("commands.sportals.clear.usage");
			}

			Set<Portal> portals = new HashSet<>(PortalRegistry.getPortals().values());

			for (Portal portal : portals)
			{
				PortalRegistry.deactivatePortal(server.getWorld(portal.getDimension()), portal.getCorner1().getPos());
			}

			notifyCommandListener(sender, this, "commands.sportals.clear.success", sender.getName());
		}
		else if ("list".equals(args[0]))
		{
			if (args.length < 2 || (!"all".equals(args[1]) && !"address".equals(args[1]) && !"dimension".equals(args[1])))
			{
				// sportals list <address | all | dimension>
				throw new WrongUsageException("commands.sportals.list.usage");
			}

			List<Portal> portals = new ArrayList<>();

			if ("all".equals(args[1]))
			{
				ImmutableListMultimap<Address, Portal> addresses = PortalRegistry.getAddresses();

				Set<Address> uniqueAddresses = new HashSet<>(addresses.keys());
				List<Portal> portalsForAddress;

				for (Address address : uniqueAddresses)
				{
					portalsForAddress = addresses.get(address);
					portals.addAll(portalsForAddress);
				}
			}
			else if ("address".equals(args[1]))
			{
				if (args.length != 6)
				{
					// sportals list address <addressBlockId> <addressBlockId> <addressBlockId> <addressBlockId>
					throw new WrongUsageException("commands.sportals.list.address.usage");
				}

				Address address = new Address(args[2], args[3], args[4], args[5]);
				portals = PortalRegistry.getPortalsWithAddress(address);
			}
			else if ("dimension".equals(args[1]))
			{
				if (args.length != 3)
				{
					// sportals list dimension <dimensionId>
					throw new WrongUsageException("commands.sportals.list.dimension.usage");
				}

				int dimension = parseInt(args[2]);
				portals = PortalRegistry.getPortalsInDimension(dimension);
			}

			SimplePortals.log.info("Registered portals");
			SimplePortals.log.info("|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|");
			SimplePortals.log.info("| Dimension | Position                         | Power | Address                                                                                                                                                |");
			SimplePortals.log.info("|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|");

			for (Portal portal : portals)
			{
				SimplePortals.log.info(String.format("| %9s | %32s | %5d | %-150s |",
					portal.getDimension(), portal.getCorner1().getPos(), PortalRegistry.getPower(portal), portal.getAddress()));
			}

			SimplePortals.log.info("|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|");

			notifyCommandListener(sender, this, "commands.sportals.list.success", sender.getName());
		}
		else if ("deactivate".equals(args[0]))
		{
			if (args.length < 2 || (!"address".equals(args[1]) && !"position".equals(args[1])))
			{
				// sportals deactivate <address | position>
				throw new WrongUsageException("commands.sportals.deactivate.usage");
			}

			List<Portal> portals = null;

			if ("address".equals(args[1]))
			{
				if (args.length < 6 || args.length > 7)
				{
					// sportals deactivate address <addressBlockId> <addressBlockId> <addressBlockId> <addressBlockId> [dimensionId]
					throw new WrongUsageException("commands.sportals.deactivate.address.usage");
				}

				Address address = new Address(args[2], args[3], args[4], args[5]);
				portals = PortalRegistry.getPortalsWithAddress(address);

				int dimensionCopy = 0;
				boolean hasDimensionArg = false;

				if (args.length == 7)
				{
					int dimension = parseInt(args[6]);
					portals = portals.stream().filter((portal -> portal.getDimension() == dimension)).collect(Collectors.toList());

					// This is needed to get the dimension value out of the if-clause. If the actual dimension variable
					// is declared outside the if-clause, the Lambda won't compile -_-.
					dimensionCopy = dimension;
					hasDimensionArg = true;
				}

				if (portals.size() == 0)
				{
					throw new CommandException("commands.sportals.deactivate.portalNotFoundWithAddress", address,
						(hasDimensionArg) ? dimensionCopy : "-");
				}
			}
			else if ("position".equals(args[1]))
			{
				if (args.length < 5 || args.length > 6)
				{
					// sportals deactivate position <x> <y> <z> [dimensionId]
					throw new WrongUsageException("commands.sportals.deactivate.position.usage");
				}

				BlockPos portalPos = parseBlockPos(sender, args, 2, false);
				int dimension;

				if (args.length == 6)
				{
					dimension = parseInt(args[5]);
				}
				else
				{
					Entity commandSender = sender.getCommandSenderEntity();

					if (commandSender == null)
					{
						throw new SyntaxErrorException("commands.sportals.deactivate.unknownSender");
					}
					else
					{
						dimension = commandSender.dimension;
					}
				}

				portals = PortalRegistry.getPortalsAt(portalPos, dimension);
				if (portals.size() == 0) throw new CommandException("commands.sportals.deactivate.portalNotFoundAtPos", portalPos, dimension);
			}

			BlockPos portalPos;

			for (Portal portal : portals)
			{
				portalPos = Iterables.getFirst(portal.getPortalPositions(), null);

				// This should never happen, because active portals should always have portal blocks.
				// Well it's Minecraft so better check it and fall back to the position of corner 1 if needed.
				if (portalPos == null) portalPos = portal.getCorner1().getPos();

				PortalRegistry.deactivatePortal(server.getWorld(portal.getDimension()), portalPos);
				notifyCommandListener(sender, this, "commands.sportals.deactivate.success", portalPos, portal.getDimension());
			}
		}
		else if ("power".equals(args[0]))
		{
			if (args.length < 2 || (!"add".equals(args[1]) && !"remove".equals(args[1]) && !"get".equals(args[1])))
			{
				// sportals power <add | get | remove>
				throw new WrongUsageException("commands.sportals.power.usage");
			}

			String command;
			int argumentOffset = 0;

			if ("add".equals(args[1]))
			{
				command = "add";
			}
			else if ("remove".equals(args[1]))
			{
				command = "remove";
			}
			else
			{
				command = "get";
				argumentOffset = 1;
			}

			if ((argumentOffset == 0 && (args.length < 6 || args.length > 7))
				|| (argumentOffset == 1 && (args.length < 5 || args.length > 6)))
			{
				// sportals power add <amount> <x> <y> <z> [dimension]
				// sportals power remove <amount> <x> <y> <z> [dimension]
				// sportals power get <x> <y> <z> [dimension]
				throw new WrongUsageException("commands.sportals.power." + command + ".usage");
			}

			int amount = 0;

			// Get command has no 'amount' argument.
			if (!"get".equals(command))
			{
				amount = parseInt(args[2]);

				if (amount <= 0)
				{
					throw new NumberInvalidException("commands.sportals.power.addRemove.invalidAmount", amount);
				}
			}

			BlockPos portalPos = parseBlockPos(sender, args, 3 - argumentOffset, false);
			int dimension;

			if (args.length == 7 - argumentOffset)
			{
				dimension = parseInt(args[6 - argumentOffset]);
			}
			else
			{
				Entity commandSender = sender.getCommandSenderEntity();

				if (commandSender == null)
				{
					throw new SyntaxErrorException("commands.sportals.power.unknownSender");
				}
				else
				{
					dimension = commandSender.dimension;
				}
			}

			List<Portal> portals = PortalRegistry.getPortalsAt(portalPos, dimension);

			if (portals.size() == 0)
			{
				throw new CommandException("commands.sportals.power.portalNotFound", portalPos, dimension);
			}
			else if (portals.size() > 1)
			{
				throw new CommandException("commands.sportals.power.multiplePortalsFound", portalPos, dimension);
			}

			Portal portal = portals.get(0);

			if ("add".equals(command))
			{
				amount = amount - PortalRegistry.addPower(portal, amount);
			}
			else if ("remove".equals(command))
			{
				amount = Math.min(amount, PortalRegistry.getPower(portal));
				amount = (PortalRegistry.removePower(portal, amount)) ? amount : 0;
			}
			else
			{
				amount = PortalRegistry.getPower(portal);
			}

			notifyCommandListener(sender, this, "commands.sportals.power." + command + ".success", portalPos, dimension, amount);
		}
		else if ("cooldown".equals(args[0]))
		{
			if (args.length != 2)
			{
				// sportals cooldown <playerName>
				throw new WrongUsageException("commands.sportals.cooldown.usage");
			}

			EntityPlayerMP player = server.getPlayerList().getPlayerByUsername(args[1]);

			if (player == null)
			{
				throw new PlayerNotFoundException("commands.tpd.unknownPlayerName", args[1]);
			}

			notifyCommandListener(sender, this, "commands.sportals.cooldown.success", player.getName(), player.timeUntilPortal, player.timeUntilPortal / 20f);
		}
	}

	 */
}
