package net.zarathul.simpleportals.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;
import net.zarathul.simpleportals.SimplePortals;
import net.zarathul.simpleportals.registration.Portal;
import net.zarathul.simpleportals.registration.PortalRegistry;

import java.util.List;

/**
 * Represents the frame of the portal mutliblock.
 */
public class BlockPortalFrame extends Block
{
	public BlockPortalFrame()
	{
		this(SimplePortals.BLOCK_PORTAL_FRAME_NAME);
	}
	
	public BlockPortalFrame(String registryName)
	{
		super(Block.Properties.create(Material.ROCK, MaterialColor.BLACK)
				.hardnessAndResistance(50.0f, 200.0f)
				.sound(SoundType.STONE)
				.harvestTool(ToolType.PICKAXE)
				.harvestLevel(3));

		setRegistryName(registryName);
	}

	@Override
	public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit)
	{
		if (!world.isRemote)
		{
			ItemStack heldStack = player.getHeldItem(hand);
			Item usedItem = heldStack.getItem();

			if (usedItem == SimplePortals.itemPortalActivator)
			{
				// Another case of shitty renaming. Hey let's rename isSneaking() to isShiftKeyDown() because nobody
				// would ever rebind buttons or would they?  -sigh-
				if (player.isShiftKeyDown())
				{
					world.destroyBlock(pos, true);
				}
				else if (!PortalRegistry.isPortalAt(pos, player.dimension))
				{
					PortalRegistry.activatePortal(world, pos, hit.getFace());
				}
			}
		}

		return super.onBlockActivated(state, world, pos, player, hand, hit);
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

		super.onReplaced(oldState, world, pos, newState, isMoving);
	}

	@Override
	public void neighborChanged(BlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean isMoving)
	{
		if (!world.isRemote	&&
			!neighborBlock.isAir(neighborBlock.getDefaultState()) &&	// I'd like to supply a proper BlockState here but the new block has already been placed, so there's no way.
			neighborBlock != SimplePortals.blockPortalFrame	&&
			neighborBlock != SimplePortals.blockPowerGauge &&
			neighborBlock != SimplePortals.blockPortal)
		{
			// Deactivate all portals that share this frame block if an address block was removed or changed.

			List<Portal> affectedPortals = PortalRegistry.getPortalsAt(pos, world.getDimension().getType());

			if (affectedPortals == null || affectedPortals.size() < 1) return;

			Portal firstPortal = affectedPortals.get(0);

			if (firstPortal.hasAddressChanged(world))
			{
				PortalRegistry.deactivatePortal(world, pos);
			}
		}

		super.neighborChanged(state, world, pos, neighborBlock, neighborPos, isMoving);
	}
}