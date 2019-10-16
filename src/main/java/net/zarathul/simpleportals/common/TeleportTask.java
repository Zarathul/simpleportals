package net.zarathul.simpleportals.common;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionType;

public class TeleportTask
{
	public int creationTickCount;
	public ServerPlayerEntity player;
	public DimensionType dimension;
	public BlockPos pos;
	public Direction facing;

	public TeleportTask(int creationTickCount, ServerPlayerEntity player, DimensionType dimension, BlockPos pos, Direction facing)
	{
		this.creationTickCount = creationTickCount;
		this.player = player;
		this.dimension = dimension;
		this.pos = pos;
		this.facing = facing;
	}
}
