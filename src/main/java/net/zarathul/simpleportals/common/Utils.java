package net.zarathul.simpleportals.common;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.server.SPlayEntityEffectPacket;
import net.minecraft.network.play.server.SSetExperiencePacket;
import net.minecraft.potion.EffectInstance;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.translation.LanguageMap;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;

import java.util.ArrayList;

/**
 * General utility class.
 */
public final class Utils
{
	private static final LanguageMap I18N = LanguageMap.getInstance();

	/**
	 * Gets the localized formatted string for the specified key.
	 *
	 * @param key
	 * The key for the localized string.
	 * @param parameters
	 * Formatting arguments.
	 * @return
	 * The localized formatted string.
	 */
	public static String translate(String key, Object... parameters)
	{
		return String.format(I18N.translateKey(key), parameters);
	}

	/**
	 * Gets the localized formatted strings for the specified key and formatting arguments.
	 *
	 * @param key
	 * The base key without an index (e.g. "myKey" gets "myKey0", "myKey1" ... etc.).
	 * @param args
	 * Formatting arguments.
	 * @return
	 * A list of localized strings for the specified key, or an empty list if the key was not found.
	 */
	public static ArrayList<StringTextComponent> multiLineTranslate(String key, Object... args)
	{
		ArrayList<StringTextComponent> lines = new ArrayList<>();

		if (key != null)
		{
			int x = 0;
			String currentKey = key + x;

			while (I18N.exists(currentKey))
			{
				lines.add(new StringTextComponent(String.format(I18N.translateKey(currentKey), args)));
				currentKey = key + ++x;
			}
		}

		return lines;
	}

	/**
	 * Gets the coordinate component of a BlockPos for the specified axis.
	 *
	 * @param pos
	 * The coordinate to choose the component from.
	 * @param axis
	 * The axis representing the coordinate component to choose.
	 * @return
	 * <code>0</code> if either pos or axis are <code>null</code>, otherwise the chosen coordinate component.
	 */
	public static int getAxisValue(BlockPos pos, Axis axis)
	{
		if (pos == null || axis == null) return 0;

		if (axis == Axis.X) return pos.getX();
		if (axis == Axis.Y)	return pos.getY();
		if (axis == Axis.Z)	return pos.getZ();

		return 0;
	}

	/**
	 * Gets the relative direction from one {@link BlockPos} to another.
	 *
	 * @param from
	 * The starting point.
	 * @param to
	 * The end point.
	 * @return
	 * One of the {@link Direction} values or <code>null</code> if one of the arguments was <code>null</code>.
	 */
	public static Direction getRelativeDirection(BlockPos from, BlockPos to)
	{
		if (from == null || to == null) return null;

		BlockPos directionVec = to.subtract(from);

		return Direction.getFacingFromVector(directionVec.getX(), directionVec.getY(), directionVec.getZ());
	}

	/**
	 * Gets the axis that is orthogonal to, and on the same plane as the specified one.
	 *
	 * @param axis
	 * The starting axis.
	 * @return
	 * One of the {@link Axis} values or <code>null</code> if the specified axis was <code>null</code> or
	 * there is no other axis on the same plane.
	 */
	public static Axis getOrthogonalTo(Axis axis)
	{
		if (axis == null || axis == Axis.Y) return null;

		return (axis == Axis.X) ? Axis.Z : Axis.X;
	}

	/**
	 * Teleport an entity to the specified position in the specified dimensionId
	 * facing the specified direction.
	 *
	 * @param entity
	 * The entity to teleport. Can be any entity (item, mob, player).
	 * @param dimensionId
	 * The id of the dimension to port to.
	 * @param destination
	 * The position to port to.
	 * @param facing
	 * The direction the entity should face after porting.
	 */
	public static void teleportTo(Entity entity, int dimensionId, BlockPos destination, Direction facing)
	{
		if (entity == null || destination == null || entity.isBeingRidden() || entity.isOnePlayerRiding() || !entity.isNonBoss()) return;

		DimensionType dimension = DimensionType.getById(dimensionId);
		if (dimension == null) return;

		ServerPlayerEntity player = (entity instanceof ServerPlayerEntity) ? (ServerPlayerEntity) entity : null;
		boolean interdimensional = (entity.dimension.getId() != dimensionId);

		if (player != null)
		{
			// Note: This field is normally not accessible (see accesstransformer.cfg in META-INF folder).
			// Setting this flag circumvents at least a part of the shitty speed hack checks in
			// net.minecraft.network.play.ServerPlayNetHandler#processPlayer() that cause nothing but trouble.
			player.invulnerableDimensionChange = true;

			ServerWorld destinationWorld = player.getServer().getWorld(dimension);
			player.teleport(destinationWorld, destination.getX() + 0.5d, destination.getY(), destination.getZ() + 0.5d, getYaw(facing), player.rotationPitch);

			if (interdimensional)
			{
				// FIXME: Remove whenever ServerPlayerEntity.teleport() actually does this.
				// Reapply potion effects
				for (EffectInstance potionEffect : player.getActivePotionEffects())
				{
					player.connection.sendPacket(new SPlayEntityEffectPacket(player.getEntityId(), potionEffect));
				}

				// Resend player XP otherwise the XP bar won't show up until XP is either gained or lost
				player.connection.sendPacket(new SSetExperiencePacket(player.experience, player.experienceTotal, player.experienceLevel));
			}
		}
		else
		{
			if (interdimensional)
			{
				teleportNonPlayerEntityToDimension(entity, dimension, destination, getYaw(facing));
			}
			else
			{
				entity.setLocationAndAngles(destination.getX() + 0.5d,
											destination.getY(),
											destination.getZ() + 0.5d,
											getYaw(facing),
											entity.rotationPitch);
			}
		}
	}

	/**
	 * Teleport a non-player entity to the specified position in the specified dimension
	 * facing the specified direction.
	 * ({@link Entity#changeDimension(DimensionType)} without the hardcoded dimension specific vanilla code)
	 *
	 * @param entity
	 * The entity to teleport. Can be any entity except players (e.g. item, mob).
	 * @param dimension
	 * The dimension to port to.
	 * @param destination
	 * The position to port to.
	 * @param yaw
	 * The rotation yaw the entity should have after porting.
	 */
	private static void teleportNonPlayerEntityToDimension(Entity entity, DimensionType dimension, BlockPos destination, float yaw)
	{
		if (!entity.world.isRemote && entity.isAlive())
		{
			MinecraftServer server = entity.getServer();
			ServerWorld startWorld = server.getWorld(entity.dimension);
			ServerWorld destinationWorld = server.getWorld(dimension);

			entity.world.getProfiler().startSection("changeDimension");

			entity.dimension = dimension;
			entity.detach();

			entity.world.getProfiler().startSection("reposition");
			entity.world.getProfiler().endStartSection("reloading");

			Entity portedEntity = entity.getType().create(destinationWorld);

			if (portedEntity != null)
			{
				portedEntity.copyDataFromOld(entity);
				portedEntity.moveToBlockPosAndAngles(destination, yaw, portedEntity.rotationPitch);
				destinationWorld.func_217460_e(portedEntity);
			}

			entity.remove(false);
			entity.world.getProfiler().endSection();
			startWorld.resetUpdateEntityTick();
			destinationWorld.resetUpdateEntityTick();
			entity.world.getProfiler().endSection();
		}
	}

	/**
	 * Converts the specified facing to a degree value.
	 *
	 * @param facing
	 * The facing to convert.
	 * @return
	 * <code>0</code> if facing is <code>null</code>, otherwise a value between <code>0</code> and <code>270</code> that
	 * is a multiple of <code>90</code>.
	 */
	public static float getYaw(Direction facing)
	{
		if (facing == null) return 0;

		float yaw;

		switch (facing)
		{
			case EAST:  yaw = 270.0f; break;
			case WEST:  yaw =  90.0f; break;
			case NORTH:	yaw = 180.0f; break;
			default:	yaw =   0.0f; break;
		}

		return yaw;
	}

	/**
	 * Determines if a string represents an integer (may be negative).
	 *
	 * @param numberString
	 * The string to check.
	 * @return
	 * <code>true</code> if numberString can be converted to an integer, otherwise <code>false</code>.
	 */
	public static boolean isInteger(String numberString)
	{
		boolean success = true;

		try
		{
			int value = Integer.parseInt(numberString);
		}
		catch (NumberFormatException ex)
		{
			success = false;
		}

		return success;
	}
}
