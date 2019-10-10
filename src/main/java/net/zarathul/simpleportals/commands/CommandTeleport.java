package net.zarathul.simpleportals.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.*;
import net.minecraft.command.arguments.BlockPosArgument;
import net.minecraft.command.arguments.DimensionArgument;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.command.arguments.EntitySelectorParser;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.dimension.DimensionType;
import net.zarathul.simpleportals.common.Utils;

import java.util.ArrayList;
import java.util.List;

public class CommandTeleport
{
	private enum TeleportMode
	{
		ToPlayer,
		ToPosition
	}

	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		dispatcher.register(
			Commands.literal("tpd").requires((commandSource) -> {
				return commandSource.hasPermissionLevel(2);
			})
			.executes(context -> {
				SendTranslatedMessage(context.getSource(), "commands.tpd.info");
				return 1;
			})
			.then(
				Commands.argument("dimension", DimensionArgument.getDimension())
					.executes(context -> {
						return tp(context.getSource(), TeleportMode.ToPosition, DimensionArgument.func_212592_a(context, "dimension"), null, null, null);
					})
					.then(
						Commands.argument("position", BlockPosArgument.blockPos())
							.executes(context -> {
								return tp(context.getSource(), TeleportMode.ToPosition, DimensionArgument.func_212592_a(context, "dimension"), BlockPosArgument.getBlockPos(context, "position"), null, null);
							})
							.then(
								Commands.argument("player", EntityArgument.player())		// tpd <dimension> [<x> <y> <z>] [player]
									.executes(context -> {
										return tp(context.getSource(), TeleportMode.ToPosition, DimensionArgument.func_212592_a(context, "dimension"), BlockPosArgument.getBlockPos(context, "position"), null, EntityArgument.getPlayer(context, "player"));
									})
							)
					)
			)
			.then(
				Commands.argument("targetPlayer", EntityArgument.player())
					.executes(context -> {
						return tp(context.getSource(), TeleportMode.ToPlayer, null, null, EntityArgument.getPlayer(context, "targetPlayer"), null);
					})
					.then(
						Commands.argument("player", EntityArgument.player())		// tpd <targetPlayer> [player]
							.executes(context -> {
								return tp(context.getSource(), TeleportMode.ToPlayer, null, null, EntityArgument.getPlayer(context, "targetPlayer"), EntityArgument.getPlayer(context, "player"));
							})
					)
			)
		);
	}

	private static int tp(CommandSource source, TeleportMode mode, DimensionType dimension, BlockPos destination, ServerPlayerEntity targetPlayer, ServerPlayerEntity player)
	{
		if (player == null)
		{
			try
			{
				player = source.asPlayer();
			}
			catch (CommandSyntaxException ex)
			{
				throw new CommandException(new TranslationTextComponent("commands.errors.unknown_sender"));
			}
		}

		switch (mode)
		{
			case ToPosition:
				if (destination == null) destination = player.getPosition();
				break;

			case ToPlayer:
				destination = targetPlayer.getPosition();
				dimension = targetPlayer.dimension;

				break;
		}

		Utils.teleportTo(player, dimension.getId(), destination, Direction.NORTH);
		SendTranslatedMessage(source, "commands.tpd.success", player.getName(), destination.getX(), destination.getY(), destination.getZ(), dimension.getRegistryName().toString());

		return 1;
	}

	private static void SendTranslatedMessage(CommandSource source, String message, Object... args)
	{
		source.sendFeedback(new TranslationTextComponent(message, args), true);
	}
}
