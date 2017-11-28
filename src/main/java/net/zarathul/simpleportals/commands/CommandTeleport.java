package net.zarathul.simpleportals.commands;

import net.minecraft.command.*;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.DimensionManager;
import net.zarathul.simpleportals.common.Utils;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.Arrays;
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
				return getListOfStringsMatchingLastWord(args, Arrays.asList(DimensionManager.getIDs()));

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
			// tpd <dimensionId> [playerName] [<x> <z>] [y]
			throw new WrongUsageException("commands.tpd.usage");
		}
		else
		{
			int argumentOffset = 0;
			int dimensionId = parseInt(args[0]);

			if (!DimensionManager.isDimensionRegistered(dimensionId))
			{
				throw new CommandException("commands.tpd.unknownDimension", dimensionId);
			}

			boolean playerNameSpecified = false;
			Entity player = null;

			if (args.length >= 2)
			{
				playerNameSpecified = !StringUtils.isNumeric(args[1]);
				player = server.getPlayerList().getPlayerByUsername(args[1]);
			}

			// If 3 arguments are supplied and one of them is a player name, 'z' is missing.
			if ((args.length == 3 && playerNameSpecified) || (args.length == 2 && !playerNameSpecified))
			{
				throw new SyntaxErrorException("commands.tpd.zMissing");
			}

			if (player == null)
			{
				if (playerNameSpecified)
				{
					throw new PlayerNotFoundException("commands.tpd.unknownPlayerName", args[1]);
				}
				else
				{
					// Teleport the command user if no player name was specified.
					player = sender.getCommandSenderEntity();

					if (player == null)
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

			BlockPos destination = new BlockPos(x, y, z);

			if (!server.getWorld(dimensionId).getWorldBorder().contains(destination))
			{
				throw new CommandException("commands.tpd.destinationOutsideWorldBorder");
			}
			else
			{
				Utils.teleportTo(player, dimensionId, destination, EnumFacing.NORTH);
				notifyCommandListener(sender, this, "commands.tpd.success", player.getName(), destination, dimensionId);
			}
		}
	}
}
