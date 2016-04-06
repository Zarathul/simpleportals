package net.zarathul.simpleportals.common;

import java.util.ArrayList;

import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.S07PacketRespawn;
import net.minecraft.network.play.server.S1DPacketEntityEffect;
import net.minecraft.network.play.server.S1FPacketSetExperience;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.StatCollector;
import net.minecraft.world.WorldServer;

/**
 * General utility class.
 */
public final class Utils
{
	/**
	 * A predicate that returns <code>true</code> if passed string is neither <code>null</code> nor empty.
	 */
	private static final Predicate<String> stringNotNullOrEmpty = new Predicate<String>()
	{
		@Override
		public boolean apply(String item)
		{
			return !Strings.isNullOrEmpty(item);
		}
	};

	/**
	 * Checks a list of strings for <code>null</code> and empty elements.
	 * 
	 * @param items
	 * The list of strings to check.
	 * @return
	 * <code>true</code> if the list neither contains <code>null</code> elements nor empty strings, otherwise <code>false</code>.
	 */
	public static final boolean notNullorEmpty(Iterable<String> items)
	{
		return Iterables.all(items, stringNotNullOrEmpty);
	}

	/**
	 * Gets the localized formatted strings for the specified key and formatting arguments.
	 * 
	 * @param key
	 * The base key without an index (e.g. "myKey" gets "myKey0", "myKey1" ... etc.).
	 * @param args
	 * Formatting arguments.
	 * @return
	 */
	public static final ArrayList<String> multiLineTranslateToLocal(String key, Object... args)
	{
		ArrayList<String> lines = new ArrayList<String>();

		if (key != null)
		{
			int x = 0;
			String currentKey = key + x;

			while (StatCollector.canTranslate(currentKey))
			{
				lines.add(StatCollector.translateToLocalFormatted(currentKey, args));
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
	public static final int getAxisValue(BlockPos pos, Axis axis)
	{
		if (pos != null || axis != null)
		{
			if (axis == Axis.X) return pos.getX();
			if (axis == Axis.Y) return pos.getY();
			if (axis == Axis.Z) return pos.getZ();
		}
		
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
	 * One of the {@link EnumFacing} values or <code>null</code> if one of the arguments was <code>null</code>.
	 */
	public static EnumFacing getRelativeDirection(BlockPos from, BlockPos to)
	{
		if (from == null || to == null) return null;
		
		BlockPos directionVec = to.subtract(from);
		
		return EnumFacing.getFacingFromVector(directionVec.getX(), directionVec.getY(), directionVec.getZ());
	}
	
	/**
	 * Teleport an entity to the specified position in the specified dimension
	 * facing the specified direction.
	 * 
	 * @param entity
	 * The entity to teleport. Can be any entity (item, mob, player).
	 * @param dimension
	 * The dimension to port to.
	 * @param destination
	 * The position to port to.
	 * @param facing
	 * The direction the entity should face after porting.
	 * @return
	 */
	public static final void teleportTo(Entity entity, int dimension, BlockPos destination, EnumFacing facing)
	{
		if (entity == null || destination == null || entity.ridingEntity != null || entity.riddenByEntity != null) return;
		
		EntityPlayerMP player = (entity instanceof EntityPlayerMP) ? (EntityPlayerMP)entity : null;
		boolean interdimensional = (entity.dimension != dimension);
		
		if (player != null)
		{
			if (interdimensional)
			{
				teleportPlayerToDimension(player, dimension, destination, getYaw(facing));
			}
			else
			{
				player.playerNetServerHandler.setPlayerLocation(destination.getX() + 0.5d, destination.getY(), destination.getZ() + 0.5d, getYaw(facing), player.rotationPitch);
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
				entity.setVelocity(0, 0, 0);
				entity.setLocationAndAngles(
						destination.getX() + 0.5d,
						destination.getY(),
						destination.getZ() + 0.5d,
						getYaw(facing),
						entity.rotationPitch);
			}
		}
	}
	
	/**
	 * Teleport a player entity to the specified position in the specified dimension
	 * facing the specified direction.
	 * (Combination of {@link EntityPlayerMP#travelToDimension(int)} and
	 * {@link ServerConfigurationManager#transferPlayerToDimension(EntityPlayerMP, int)})
	 * 
	 * @param player
	 * The player to teleport.
	 * @param dimension
	 * The dimension to port to.
	 * @param destination
	 * The position to port to.
	 * @param yaw
	 * The rotation yaw the entity should have after porting.
	 */
	private static void teleportPlayerToDimension(EntityPlayerMP player, int dimension, BlockPos destination, float yaw)
	{
		int startDimension = player.dimension;
		MinecraftServer server = MinecraftServer.getServer();
		ServerConfigurationManager serverManager = server.getConfigurationManager();
		WorldServer startWorld = server.worldServerForDimension(startDimension);
		WorldServer destinationWorld = server.worldServerForDimension(dimension);

		player.dimension = dimension;
		player.playerNetServerHandler.sendPacket(new S07PacketRespawn(
				dimension,
				destinationWorld.getDifficulty(),
				destinationWorld.getWorldInfo().getTerrainType(),
				player.theItemInWorldManager.getGameType()));

		startWorld.removePlayerEntityDangerously(player);
		player.isDead = false;

		player.setLocationAndAngles(destination.getX() + 0.5d, destination.getY(), destination.getZ() + 0.5d, yaw, player.rotationPitch);
		destinationWorld.spawnEntityInWorld(player);
		destinationWorld.updateEntityWithOptionalForce(player, false);
		player.setWorld(destinationWorld);

		serverManager.preparePlayer(player, startWorld);
		player.playerNetServerHandler.setPlayerLocation(destination.getX() + 0.5d, destination.getY(), destination.getZ() + 0.5d, yaw, player.rotationPitch);
		player.theItemInWorldManager.setWorld(destinationWorld);
		serverManager.updateTimeAndWeatherForPlayer(player, destinationWorld);
		serverManager.syncPlayerInventory(player);

		// Reapply potion effects

		for (PotionEffect potioneffect : player.getActivePotionEffects())
		{
			player.playerNetServerHandler.sendPacket(new S1DPacketEntityEffect(player.getEntityId(), potioneffect));
		}

		// Resend player XP otherwise the XP bar won't show up until XP is either gained or lost 
		player.playerNetServerHandler.sendPacket(new S1FPacketSetExperience(player.experience, player.experienceTotal, player.experienceLevel));

		startWorld.resetUpdateEntityTick();
		destinationWorld.resetUpdateEntityTick();

		net.minecraftforge.fml.common.FMLCommonHandler.instance().firePlayerChangedDimensionEvent(player, startDimension, dimension);
	}
	
	/**
	 * Teleport a non-player entity to the specified position in the specified dimension
	 * facing the specified direction.
	 * (Combination of {@link Entity#travelToDimension(int)} and
	 * {@link ServerConfigurationManager#transferEntityToWorld(Entity, int, WorldServer, WorldServer)}).
	 * 
	 * @param entity
	 * The entity to teleport. Can be any entity (item, mob, player).
	 * @param dimension
	 * The dimension to port to.
	 * @param destination
	 * The position to port to.
	 * @param yaw
	 * The rotation yaw the entity should have after porting.
	 */
	private static void teleportNonPlayerEntityToDimension(Entity entity, int dimension, BlockPos destination, float yaw)
	{
		MinecraftServer server = MinecraftServer.getServer();
		WorldServer startWorld = server.worldServerForDimension(entity.dimension);
		WorldServer destinationWorld = server.worldServerForDimension(dimension);
		
		entity.dimension = dimension;
		startWorld.removeEntity(entity);
		entity.isDead = false;
		
		if (entity.isEntityAlive())
		{
			entity.setLocationAndAngles(destination.getX() + 0.5d, destination.getY(), destination.getZ() + 0.5d, yaw, entity.rotationPitch);
	        destinationWorld.spawnEntityInWorld(entity);
	        destinationWorld.updateEntityWithOptionalForce(entity, false);
		}
		
		entity.setWorld(destinationWorld);
		
		// Why duplicate the entity and delete the one we just went through the trouble of porting?
		// - Vanilla does it, and without it there are significantly more error and missing items.
		Entity portedEntity = EntityList.createEntityByName(EntityList.getEntityString(entity), destinationWorld);
		
		if (portedEntity != null)
		{
			portedEntity.copyDataFromOld(entity);
			// setVelocity is marked to be client side only for some reason, so set velocity manually
			portedEntity.motionX = 0;
			portedEntity.motionY = 0;
			portedEntity.motionZ = 0;
			portedEntity.setLocationAndAngles(
					destination.getX() + 0.5d,
					destination.getY(),
					destination.getZ() + 0.5d,
					yaw,
					entity.rotationPitch);
			
			destinationWorld.spawnEntityInWorld(portedEntity);
		}
		
		entity.isDead = true;
		startWorld.resetUpdateEntityTick();
		destinationWorld.resetUpdateEntityTick();
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
	public static float getYaw(EnumFacing facing)
	{
		if (facing == null) return 0;
		
		float yaw;
		
		switch (facing)
		{
			case EAST:
				yaw = 270.0f;
				break;
			
			case WEST:
				yaw = 90.0f;
				break;
				
			case NORTH:
				yaw = 180.0f;
				break;
			
			default:
				yaw = 0.0f;
				break;
		}
		
		return yaw;
	}
}