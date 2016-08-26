package net.zarathul.simpleportals.blocks;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

import com.google.common.collect.Maps;

import net.minecraft.block.BlockBreakable;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import net.zarathul.simpleportals.common.Utils;
import net.zarathul.simpleportals.configuration.Config;
import net.zarathul.simpleportals.registration.Portal;
import net.zarathul.simpleportals.registration.PortalRegistry;
import net.zarathul.simpleportals.registration.Registry;

/**
 * Represents the actual portals in the center of the portal multiblock.
 */
public class BlockPortal extends BlockBreakable
{
	public static final PropertyEnum<EnumFacing.Axis> AXIS = PropertyEnum.<EnumFacing.Axis>create(
			"axis",
			EnumFacing.Axis.class,
			new EnumFacing.Axis[] { EnumFacing.Axis.X, EnumFacing.Axis.Y, EnumFacing.Axis.Z });
	
	private static final HashMap<UUID, Long> entityCooldowns = Maps.newHashMap();
	private static long lastCooldownUpdate = 0;
	private static final int COOLDOWN = 60;
	private static final int COOLDOWN_UPDATE_INTERVAL = 200;
	
	public BlockPortal()
	{
		super(Material.PORTAL, false);
		
		setRegistryName(Registry.BLOCK_PORTAL_NAME);
		setUnlocalizedName(Registry.BLOCK_PORTAL_NAME);
		setDefaultState(this.blockState.getBaseState().withProperty(AXIS, EnumFacing.Axis.X));
		setHardness(-1.0F); // indestructible by normal means
		setLightLevel(0.75F);
		setSoundType(SoundType.GLASS);
	}

	@Override
	protected BlockStateContainer createBlockState()
	{
		return new BlockStateContainer(this, new IProperty[] { AXIS });
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta)
	{
		EnumFacing.Axis axis;
		
		switch (meta)
		{
			case 0: axis = EnumFacing.Axis.X; break;
			case 1: axis = EnumFacing.Axis.Y; break;
			case 2:	axis = EnumFacing.Axis.Z; break;
			default: axis = EnumFacing.Axis.X; break;
		}
		
		return this.getDefaultState().withProperty(AXIS, axis);
	}

	@Override
	public int getMetaFromState(IBlockState state)
	{
		switch (state.getValue(AXIS))
		{
			case X: return 0;
			case Y: return 1;
			case Z:	return 2;
			default: return 0;
		}
	}
	
	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, World world, BlockPos pos)
	{
		return NULL_AABB;
	}
	
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos)
	{
		Axis axis = state.getValue(AXIS);
		
		double minX = 0, maxX = 0, minY = 0, maxY = 0, minZ = 0, maxZ = 0;
		
		switch (axis)
		{
			case X:
				minX = 0.375;
				maxX = 0.625;
				minY = 0;
				maxY = 1;
				minZ = 0;
				maxZ = 1;
			break;
			
			case Y:
				minX = 0;
				maxX = 1;
				minY = 0.375;
				maxY = 0.625;
				minZ = 0;
				maxZ = 1;
			break;
			
			case Z:
				minX = 0;
				maxX = 1;
				minY = 0;
				maxY = 1;
				minZ = 0.375;
				maxZ = 0.625;
			break;
		}
		
		return new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
	}
	
	@Override
	public void onEntityCollidedWithBlock(World world, BlockPos pos, IBlockState state, Entity entity)
	{
		if (!world.isRemote && !entity.isRiding() && !entity.isBeingRidden() && entity.isNonBoss() && !entity.isDead)
		{
			// Check if entity is on teleportation cooldown
			if (entityCooldowns.containsKey(entity.getUniqueID()))
			{
				if (world.getTotalWorldTime() - entityCooldowns.get(entity.getUniqueID()) < COOLDOWN) return;
				
				entityCooldowns.remove(entity.getUniqueID());
				
				if (world.getTotalWorldTime() - lastCooldownUpdate >= COOLDOWN_UPDATE_INTERVAL)
				{
					if (entityCooldowns.size() > 0)
					{
						// Remove expired cooldowns
						List<UUID> expiredCooldowns = entityCooldowns.keySet().stream()
							.filter(id -> world.getTotalWorldTime() - entityCooldowns.get(id) >= COOLDOWN)
							.collect(Collectors.toList());
						
						for (UUID id : expiredCooldowns) { entityCooldowns.remove(id); }
					}
					
					lastCooldownUpdate = world.getTotalWorldTime();
				}
			}
			
			List<Portal> portals = PortalRegistry.getPortalsAt(pos, entity.dimension);
			
			if (portals == null || portals.size() < 1) return;
			
			Portal start = portals.get(0);
			
			// Handle power source entering the portal
			
			if (entity instanceof EntityItem && Config.powerCost > 0 && Config.powerCapacity > 0)
			{
				ItemStack item = ((EntityItem)entity).getEntityItem();
				
				if (PortalRegistry.getPower(start) < Config.powerCapacity && OreDictionary.containsMatch(false, Config.powerSources, item))
				{
					int surplus = PortalRegistry.addPower(start, item.stackSize);
					
					PortalRegistry.updatePowerGauges(world, start);
					
					if (surplus > 0)
					{
						item.stackSize = surplus;
					}
					else
					{
						entity.setDead();
					}
					
					return;
				}
			}
			
			// Bypass the power cost for players in creative mode
			boolean bypassPowerCost = (entity instanceof EntityPlayerMP && ((EntityPlayerMP)entity).capabilities.isCreativeMode);
			
			// Check if portal has enough power for a port
			if (!bypassPowerCost && PortalRegistry.getPower(start) < Config.powerCost) return;
			
			portals = PortalRegistry.getPortalsWithAddress(start.getAddress());
			
			if (portals == null || portals.size() < 2) return;
			
			// Get a shuffled list of possible destination portals (portals with the same address)
			List<Portal> destinations = portals.stream()
					.filter(e -> !e.equals(start))
					.collect(Collectors.toList());
			
			Collections.shuffle(destinations);
			
			if (destinations.size() > 0 && (bypassPowerCost || Config.powerCost == 0 || PortalRegistry.removePower(start, Config.powerCost)))
			{
				WorldServer server;
				BlockPos portTarget = null;
				Portal destination = null;
				MinecraftServer mcServer = entity.getServer();
				int entityHeight = MathHelper.ceiling_float_int(entity.height);
				
				// Pick the first not blocked destination portal
				for (Portal portal : destinations)
				{
					server = mcServer.worldServerForDimension(portal.getDimension());
					portTarget = portal.getPortDestination(server, entityHeight);
					
					if (portTarget != null)
					{
						destination = portal;
						break;
					}
				}
				
				if (portTarget != null)
				{
					// Get a facing pointing away from the destination portal. After porting, the portal 
					// will always be behind the entity. When porting to a horizontal portal the initial
					// facing is not changed.
					EnumFacing entityFacing = (destination.getAxis() == Axis.Y)
							? entity.getHorizontalFacing()
							: (destination.getAxis() == Axis.Z)
							? (portTarget.getZ() > destination.getCorner1().getPos().getZ())
							? EnumFacing.SOUTH
							: EnumFacing.NORTH
							: (portTarget.getX() > destination.getCorner1().getPos().getX())
							? EnumFacing.EAST
							: EnumFacing.WEST;
					
					Utils.teleportTo(entity, destination.getDimension(), portTarget, entityFacing);
					PortalRegistry.updatePowerGauges(world, start);
				}
			}
			
			// Put the entity on "cooldown" in order to prevent it from instantly porting again
			entityCooldowns.put(entity.getUniqueID(), world.getTotalWorldTime());
		}
	}
	
	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state)
	{
		if (!world.isRemote)
		{
			// Deactivate damaged portals.
			
			List<Portal> affectedPortals = PortalRegistry.getPortalsAt(pos, world.provider.getDimension());
			
			if (affectedPortals == null || affectedPortals.size() < 1) return;
			
			Portal firstPortal = affectedPortals.get(0);
			
			if (firstPortal.isDamaged(world))
			{
				PortalRegistry.deactivatePortal(world, pos);
			}
		}
	}
	
	@Override
	public boolean requiresUpdates()
	{
		return false;
	}

	@Override
	public boolean isFullCube(IBlockState state)
	{
		return false;
	}

	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos,
			EntityPlayer player)
	{
		return null;
	}

	@Override
	public int quantityDropped(Random random)
	{
		return 0;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public BlockRenderLayer getBlockLayer()
	{
		return BlockRenderLayer.TRANSLUCENT;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random rand)
	{
		if (Config.ambientSoundEnabled && rand.nextInt(100) == 0)
		{
			world.playSound((double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D, SoundEvents.BLOCK_PORTAL_AMBIENT, SoundCategory.BLOCKS, 0.5F, rand.nextFloat() * 0.4F + 0.8F, false);
		}

		if (Config.particlesEnabled)
		{
			for (int i = 0; i < 4; ++i)
			{
				double d0 = (double)((float)pos.getX() + rand.nextFloat());
				double d1 = (double)((float)pos.getY() + rand.nextFloat());
				double d2 = (double)((float)pos.getZ() + rand.nextFloat());
				double d3 = ((double)rand.nextFloat() - 0.5D) * 0.5D;
				double d4 = ((double)rand.nextFloat() - 0.5D) * 0.5D;
				double d5 = ((double)rand.nextFloat() - 0.5D) * 0.5D;
				int j = rand.nextInt(2) * 2 - 1;

				if (world.getBlockState(pos.west()).getBlock() != this && world.getBlockState(pos.east()).getBlock() != this)
				{
					d0 = (double)pos.getX() + 0.5D + 0.25D * (double)j;
					d3 = (double)(rand.nextFloat() * 2.0F * (float)j);
				}
				else
				{
					d2 = (double)pos.getZ() + 0.5D + 0.25D * (double)j;
					d5 = (double)(rand.nextFloat() * 2.0F * (float)j);
				}

				world.spawnParticle(EnumParticleTypes.PORTAL, d0, d1, d2, d3, d4, d5, new int[0]);
			}
		}
	}
}