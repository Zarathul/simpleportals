package net.zarathul.simpleportals.commands;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Iterables;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.zarathul.simpleportals.SimplePortals;
import net.zarathul.simpleportals.registration.Address;
import net.zarathul.simpleportals.registration.Portal;
import net.zarathul.simpleportals.registration.PortalRegistry;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class CommandPortals extends CommandBase
{
	@Override
	public String getName()
	{
		return "sportals";
	}

	@Override
	public String getUsage(ICommandSender sender)
	{
		return "commands.sportals.usage";
	}

	@Override
	public int getRequiredPermissionLevel()
	{
		return 4;
	}

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
}
