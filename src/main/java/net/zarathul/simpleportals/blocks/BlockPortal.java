package net.zarathul.simpleportals.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BreakableBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.zarathul.simpleportals.SimplePortals;
import net.zarathul.simpleportals.common.TeleportTask;
import net.zarathul.simpleportals.common.Utils;
import net.zarathul.simpleportals.configuration.Config;
import net.zarathul.simpleportals.registration.Portal;
import net.zarathul.simpleportals.registration.PortalRegistry;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Represents the actual portals in the center of the portal multiblock.
 */
public class BlockPortal extends BreakableBlock
{
	private static final VoxelShape X_AABB = Block.makeCuboidShape(6.0D, 0.0D, 0.0D, 10.0D, 16.0D, 16.0D);
	private static final VoxelShape Y_AABB = Block.makeCuboidShape(0.0D, 6.0D, 0.0D, 16.0D, 10.0D, 16.0D);
	private static final VoxelShape Z_AABB = Block.makeCuboidShape(0.0D, 0.0D, 6.0D, 16.0D, 16.0D, 10.0D);

	public static final EnumProperty<Axis> AXIS = EnumProperty.create(
		"axis",
		Axis.class,
		Axis.X, Axis.Y, Axis.Z);

	public BlockPortal()
	{
		super(Block.Properties.create(Material.PORTAL)
			.doesNotBlockMovement()
			.noDrops()
			.hardnessAndResistance(-1.0F) // indestructible by normal means
			.lightValue(11)
			.sound(SoundType.GLASS));
		
		setRegistryName(SimplePortals.BLOCK_PORTAL_NAME);
	}

	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> stateBuilder)
	{
		stateBuilder.add(AXIS);
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader blockReader, BlockPos pos, ISelectionContext selection)
	{
		Axis portalAxis = state.get(AXIS);

		switch (portalAxis)
		{
			case Y: return Y_AABB;
			case Z: return Z_AABB;
			case X:
			default:
				return X_AABB;
		}
	}

	@Override
	public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity)
	{
		if (!world.isRemote && entity.isAlive() && !entity.isPassenger() && !entity.isBeingRidden() && entity.isNonBoss() &&
			VoxelShapes.compare(VoxelShapes.create(entity.getBoundingBox().offset((double)(-pos.getX()), (double)(-pos.getY()), (double)(-pos.getZ()))), state.getShape(world, pos), IBooleanFunction.AND))
		{
			// For players a configurable cooldown is used instead of the value provided by getPortalCooldown(), because
			// that value is very small. A small value is fine for vanilla teleportation mechanics but can cause issues
			// for this mod.
			int cooldown = (entity instanceof ServerPlayerEntity) ? Config.playerTeleportationCooldown.get() : entity.getPortalCooldown();
			// Check if entity is on teleportation cooldown
			if (entity.timeUntilPortal > 0) return;

			List<Portal> portals = PortalRegistry.getPortalsAt(pos, entity.dimension);
			
			if (portals == null || portals.size() < 1) return;
			
			Portal start = portals.get(0);
			
			// Handle power source entering the portal
			
			if (entity instanceof ItemEntity && Config.powerCost.get() > 0 && Config.powerCapacity.get() > 0)
			{
				ItemStack item = ((ItemEntity)entity).getItem();
				
				if ((PortalRegistry.getPower(start) < Config.powerCapacity.get()) && item.getItem().getTags().contains(Config.powerSource))
				{
					int surplus = PortalRegistry.addPower(start, item.getCount());

					PortalRegistry.updatePowerGauges(world, start);
					
					if (surplus > 0)
					{
						item.setCount(surplus);
					}
					else
					{
						entity.remove();
					}

					return;
				}
			}
			
			// Bypass the power cost for players in creative mode
			boolean bypassPowerCost = (entity instanceof ServerPlayerEntity && ((ServerPlayerEntity)entity).isCreative());
			
			// Check if portal has enough power for a port
			if (!bypassPowerCost && PortalRegistry.getPower(start) < Config.powerCost.get()) return;
			
			portals = PortalRegistry.getPortalsWithAddress(start.getAddress());
			
			if (portals == null || portals.size() < 2) return;
			
			// Get a shuffled list of possible destination portals (portals with the same address)
			List<Portal> destinations = portals.stream()
				.filter(e -> !e.equals(start))
				.collect(Collectors.toList());
			
			Collections.shuffle(destinations);
			
			if (destinations.size() > 0 && (bypassPowerCost || Config.powerCost.get() == 0 || PortalRegistry.removePower(start, Config.powerCost.get())))
			{
				MinecraftServer mcServer = entity.getServer();
				if (mcServer == null) return;

				int entityHeight = MathHelper.ceil(entity.getHeight());
				ServerWorld serverWorld;
				DimensionType dimension;
				BlockPos destinationPos = null;
				Portal destinationPortal = null;

				// Pick the first not blocked destination portal
				for (Portal portal : destinations)
				{
					dimension = portal.getDimension();
					if (dimension == null) continue;

					serverWorld = mcServer.getWorld(dimension);
					destinationPos = portal.getPortDestination(serverWorld, entityHeight);
					
					if (destinationPos != null)
					{
						destinationPortal = portal;
						break;
					}
				}
				
				if (destinationPos != null)
				{
					// Get a facing pointing away from the destination portal. After porting, the portal 
					// will always be behind the entity. When porting to a horizontal portal the initial
					// facing is not changed.
					Direction entityFacing = (destinationPortal.getAxis() == Axis.Y)
						? entity.getHorizontalFacing()
						: (destinationPortal.getAxis() == Axis.Z)
						? (destinationPos.getZ() > destinationPortal.getCorner1().getPos().getZ())
						? Direction.SOUTH
						: Direction.NORTH
						: (destinationPos.getX() > destinationPortal.getCorner1().getPos().getX())
						? Direction.EAST
						: Direction.WEST;
					
					if (entity instanceof ServerPlayerEntity)
					{
						try
						{
							SimplePortals.TELEPORT_QUEUE.put(new TeleportTask(
									(ServerPlayerEntity)entity,
									destinationPortal.getDimension(),
									destinationPos,
									entityFacing));
						}
						catch (InterruptedException ex)
						{
							SimplePortals.log.error("Failed to enqueue teleportation task for player '{}' to dimension ' {}'.",
													((ServerPlayerEntity)entity).getName(),
													(destinationPortal.getDimension().getRegistryName() != null)
													? destinationPortal.getDimension().getRegistryName()
													: "UNKNOWN");
						}
					}
					else
					{
						Utils.teleportTo(entity, destinationPortal.getDimension(), destinationPos, entityFacing);
						PortalRegistry.updatePowerGauges(world, start);
					}
				}
			}

			// Put the entity on "cooldown" in order to prevent it from instantly porting again.
			entity.timeUntilPortal = cooldown;
		}
	}

	@Override
	public void onReplaced(BlockState oldState, World world, BlockPos pos, BlockState newState, boolean isMoving)
	{
		if (!world.isRemote)
		{
			// Deactivate damaged portals.
			
			List<Portal> affectedPortals = PortalRegistry.getPortalsAt(pos, world.getDimension().getType());
			
			if (affectedPortals == null || affectedPortals.size() < 1) return;
			
			Portal firstPortal = affectedPortals.get(0);
			
			if (firstPortal.isDamaged(world))
			{
				PortalRegistry.deactivatePortal(world, pos);
			}
		}
	}

	@Override
	public ItemStack getItem(IBlockReader reader, BlockPos pos, BlockState state)
	{
		return ItemStack.EMPTY;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public BlockRenderLayer getRenderLayer()
	{
		return BlockRenderLayer.TRANSLUCENT;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void animateTick(BlockState state, World world, BlockPos pos, Random rand)
	{
		if (Config.ambientSoundEnabled.get() && rand.nextInt(100) == 0)
		{
			world.playSound((double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D, SoundEvents.BLOCK_PORTAL_AMBIENT, SoundCategory.BLOCKS, 0.5F, rand.nextFloat() * 0.4F + 0.8F, false);
		}

		if (Config.particlesEnabled.get())
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

				world.addParticle(ParticleTypes.PORTAL, d0, d1, d2, d3, d4, d5);
			}
		}
	}
}