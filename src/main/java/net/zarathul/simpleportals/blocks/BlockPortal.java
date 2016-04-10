package net.zarathul.simpleportals.blocks;

import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

import com.google.common.collect.Maps;

import net.minecraft.block.BlockBreakable;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.MathHelper;
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
		super(Material.portal, false);
		
		setUnlocalizedName(Registry.BLOCK_PORTAL_NAME);
		setDefaultState(this.blockState.getBaseState().withProperty(AXIS, EnumFacing.Axis.X));
		setHardness(-1.0F); // indestructible by normal means
		setLightLevel(0.75F);
		setStepSound(soundTypeGlass);
	}

	@Override
	protected BlockState createBlockState()
	{
		return new BlockState(this, new IProperty[] { AXIS });
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta)
	{
		EnumFacing.Axis axis;
		
		switch (meta)
		{
			case 0: axis = EnumFacing.Axis.X;
			case 1: axis = EnumFacing.Axis.Y;
			case 2:	axis = EnumFacing.Axis.Z;
			default: axis = EnumFacing.Axis.X;
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
	public AxisAlignedBB getCollisionBoundingBox(World world, BlockPos pos, IBlockState state)
	{
		return null;
	}
	
	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess world, BlockPos pos)
	{
		Axis axis = world.getBlockState(pos).getValue(AXIS);
		
		float minX = 0f, maxX = 0f, minY = 0f, maxY = 0f, minZ = 0f, maxZ = 0f;
		
		switch (axis)
		{
			case X:
				minX = 0f;
				maxX = 1f;
				minY = 0f;
				maxY = 1f;
				minZ = 0.375f;
				maxZ = 0.625f;
			break;
			
			case Y:
				minX = 0f;
				maxX = 1f;
				minY = 0.375f;
				maxY = 0.625f;
				minZ = 0f;
				maxZ = 1f;
			break;
			
			case Z:
				minX = 0.375f;
				maxX = 0.625f;
				minY = 0f;
				maxY = 1f;
				minZ = 0f;
				maxZ = 1f;
			break;
		}
		
		this.setBlockBounds(minX, minY, minZ, maxX, maxY, maxZ);
	}
	
	@Override
	public void onEntityCollidedWithBlock(World world, BlockPos pos, IBlockState state, Entity entity)
	{
		if (!world.isRemote && entity.ridingEntity == null && entity.riddenByEntity == null && !entity.isDead)
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
			
			// Check if portal has enough power for a port
			if (PortalRegistry.getPower(start) < Config.powerCost) return;
			
			portals = PortalRegistry.getPortalsWithAddress(start.getAddress());
			
			if (portals == null || portals.size() < 2) return;
			
			// Choose a random portal with the same address as the starting portal
			
			Portal destination = portals.stream()
					.filter(e -> !e.equals(start))
					.skip(RANDOM.nextInt(portals.size() - 1))
					.findFirst()
					.get();
			
			WorldServer server = MinecraftServer.getServer().worldServerForDimension(destination.getDimension());
			int entityHeight = MathHelper.ceiling_float_int(entity.height);
			
			BlockPos portTarget = destination.getPortDestination(server, entityHeight);
			
			if (portTarget == null) return;
			
			if (Config.powerCost == 0 || PortalRegistry.removePower(start, Config.powerCost))
			{
				// Get a facing pointing away from the destination portal. After porting, the portal 
				// will always be behind the entity. When porting to a horizontal portal the facing
				// is always south.
				EnumFacing entityFacing = (destination.getAxis() == Axis.Y)
						? EnumFacing.SOUTH
						: (destination.getAxis() == Axis.X)
						? (portTarget.getZ() > destination.getCorner1().getPos().getZ())
						? EnumFacing.SOUTH
						: EnumFacing.NORTH
						: (portTarget.getX() > destination.getCorner1().getPos().getX())
						? EnumFacing.EAST
						: EnumFacing.WEST;
				
				Utils.teleportTo(entity, destination.getDimension(), portTarget, entityFacing);
				// Put the entity on "cooldown" in order to prevent it from instantly porting again
				entityCooldowns.put(entity.getUniqueID(), world.getTotalWorldTime());
				PortalRegistry.updatePowerGauges(world, start);
			}
		}
	}
	
	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state)
	{
		if (!world.isRemote)
		{
			// Deactivate damaged portals.
			
			List<Portal> affectedPortals = PortalRegistry.getPortalsAt(pos, world.provider.getDimensionId());
			
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
	public boolean isFullCube()
	{
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Item getItem(World world, BlockPos pos)
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
	public EnumWorldBlockLayer getBlockLayer()
	{
		return EnumWorldBlockLayer.TRANSLUCENT;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(World worldIn, BlockPos pos, IBlockState state, Random rand)
	{
		if (Config.ambientSoundEnabled && rand.nextInt(100) == 0)
		{
			worldIn.playSound((double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D, "portal.portal", 0.5F, rand.nextFloat() * 0.4F + 0.8F, false);
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

				if (worldIn.getBlockState(pos.west()).getBlock() != this && worldIn.getBlockState(pos.east()).getBlock() != this)
				{
					d0 = (double)pos.getX() + 0.5D + 0.25D * (double)j;
					d3 = (double)(rand.nextFloat() * 2.0F * (float)j);
				}
				else
				{
					d2 = (double)pos.getZ() + 0.5D + 0.25D * (double)j;
					d5 = (double)(rand.nextFloat() * 2.0F * (float)j);
				}

				worldIn.spawnParticle(EnumParticleTypes.PORTAL, d0, d1, d2, d3, d4, d5, new int[0]);
			}
		}
	}
}