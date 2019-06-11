package net.zarathul.simpleportals.commands;

import net.minecraft.command.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.DimensionManager;
import net.zarathul.simpleportals.common.Utils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class CommandTeleport extends CommandBase
{
	@Override
	public String getName()
	{
		return "tpd";
	}

	@Override
	public String getUsage(ICommandSender sender)
	{
		return "commands.tpd.usage";
	}

	@Override
	public int getRequiredPermissionLevel()
	{
		return 2;
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos)
	{
		switch (args.length)
		{
			case 1:
				return getListOfStringsMatchingLastWord(args, generateTabCompletionList(DimensionManager.getIDs(), server.getPlayerList().getOnlinePlayerNames()));

			case 2:
				return getListOfStringsMatchingLastWord(args, server.getPlayerList().getOnlinePlayerNames());

			case 3:
				return getListOfStringsMatchingLastWord(args, (targetPos != null) ? String.format("%d %d %d", targetPos.getX(), targetPos.getZ(), targetPos.getY() + 1) : "");

			default:
				return super.getTabCompletions(server, sender, args, targetPos);
		}
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
	{
		if (args.length < 1 || args.length > 5)
		{
			// tpd <dimensionId> [playerName] [<x> <z>] [y] or tpd [target playerName] <destination playerName>
			throw new WrongUsageException("commands.tpd.usage");
		}
		else
		{
			Entity targetPlayer = null;
			BlockPos destination = null;
			int dimension;

			if (Utils.isInteger(args[0]))
			{
				// tpd <dimensionId> [playerName] [<x> <z>] [y]

				int argumentOffset = 0;
				dimension = parseInt(args[0]);

				if (!DimensionManager.isDimensionRegistered(dimension))
				{
					throw new CommandException("commands.tpd.unknownDimension", dimension);
				}

				boolean playerNameSpecified = false;

				if (args.length >= 2)
				{
					playerNameSpecified = !Utils.isInteger(args[1]) || args.length == 5;
					targetPlayer = server.getPlayerList().getPlayerByUsername(args[1]);
				}

				// If 3 arguments are supplied and one of them is a player name, 'z' is missing.
				if ((args.length == 3 && playerNameSpecified) || (args.length == 2 && !playerNameSpecified))
				{
					throw new SyntaxErrorException("commands.tpd.zMissing");
				}

				if (targetPlayer == null)
				{
					if (playerNameSpecified)
					{
						throw new PlayerNotFoundException("commands.tpd.unknownPlayerName", args[1]);
					}
					else
					{
						// Teleport the command user if no player name was specified.
						targetPlayer = sender.getCommandSenderEntity();

						if (targetPlayer == null)
						{
							throw new PlayerNotFoundException("commands.tpd.unknownSender");
						}

						argumentOffset = 1;
					}
				}

				BlockPos start = sender.getPosition();
				double x = start.getX();
				double z = start.getZ();
				double y = start.getY();

				if (args.length >= 3)
				{
					x = parseDouble((double)start.getX(), args[2 - argumentOffset], true);
					z = parseDouble((double)start.getZ(), args[3 - argumentOffset], true);
				}

				if ((args.length == 4 && !playerNameSpecified) || args.length == 5)
				{
					y = parseDouble((double)start.getY(), args[4 - argumentOffset], true);
				}

				destination = new BlockPos(x, y, z);
			}
			else
			{
				// tpd [target playerName] <destination playerName>

				EntityPlayerMP destinationPlayer;

				if (args.length == 2)
				{
					targetPlayer = server.getPlayerList().getPlayerByUsername(args[0]);
					destinationPlayer = server.getPlayerList().getPlayerByUsername(args[1]);

					if (targetPlayer == null || destinationPlayer == null)
					{
						throw new PlayerNotFoundException("commands.tpd.unknownPlayerName", args[(targetPlayer == null) ? 0 : 1]);
					}
				}
				else
				{
					// Teleport the command user if no player name was specified.
					targetPlayer = sender.getCommandSenderEntity();

					if (targetPlayer == null)
					{
						throw new PlayerNotFoundException("commands.tpd.unknownSender");
					}

					destinationPlayer = server.getPlayerList().getPlayerByUsername(args[0]);

					if (destinationPlayer == null)
					{
						throw new PlayerNotFoundException("commands.tpd.unknownPlayerName", args[0]);
					}
				}

				destination = destinationPlayer.getPosition();
				dimension = destinationPlayer.dimension;
			}

			Utils.teleportTo(targetPlayer, dimension, destination, EnumFacing.NORTH);
			notifyCommandListener(sender, this, "commands.tpd.success", targetPlayer.getName(), destination, dimension);
		}
	}

	/**
	 * Accumulates all elements from the passed in arrays, converts them to String and puts them in a list.
	 *
	 * @param input
	 * The input arrays.
	 * @return
	 * The list containing the converted elements. Never returns <c>null</c> but the list may be empty.
	 */
	private List<String> generateTabCompletionList(Object[]... input)
	{
		List<String> completionStrings = new ArrayList<>();

		if (input != null)
		{
			for (Object[] array : input)
			{
				for (Object element : array)
				{
					completionStrings.add(element.toString());
				}
			}
		}

		return completionStrings;
	}
}
