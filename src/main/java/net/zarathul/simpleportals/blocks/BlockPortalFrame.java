package net.zarathul.simpleportals.blocks;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.zarathul.simpleportals.SimplePortals;
import net.zarathul.simpleportals.registration.*;

/**
 * Represents the frame of the portal mutliblock.
 */
public class BlockPortalFrame extends Block {
	public BlockPortalFrame() {
		this(Registry.BLOCK_PORTAL_FRAME_NAME);
	}

	public BlockPortalFrame(String registryName) {
		super(PortalFrameMaterial.portalFrameMaterial);

		setRegistryName(registryName);
		setUnlocalizedName(registryName);
		setCreativeTab(SimplePortals.creativeTab);
		setHardness(50.0f);
		setResistance(200.0f);
		setSoundType(SoundType.STONE);
		setHarvestLevel("pickaxe", 3);
	}

	@Override
	public boolean requiresUpdates() {
		return false;
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand,
			EnumFacing side, float hitX, float hitY, float hitZ) {
		if (!world.isRemote) {
			ItemStack heldStack = player.getHeldItem(hand);

			if (heldStack != null) {
				Item usedItem = heldStack.getItem();

				if (usedItem == SimplePortals.itemPortalActivator) {
					if (player.isSneaking()) {
						world.setBlockToAir(pos);
						dropBlockAsItem(world, pos, this.getDefaultState(), 0);
					} else if (!PortalRegistry.isPortalAt(pos, player.dimension)) {
						PortalRegistry.activatePortal(world, pos, side);
					}
				}
			}
		}

		return super.onBlockActivated(world, pos, state, player, hand, side, hitX, hitY, hitZ);
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		if (!world.isRemote) {
			// Deactivate damaged portals.

			List<Portal> affectedPortals = PortalRegistry.getPortalsAt(pos, world.provider.getDimension());

			if (affectedPortals == null || affectedPortals.size() < 1)
				return;

			Portal firstPortal = affectedPortals.get(0);

			if (firstPortal.isDamaged(world)) {
				PortalRegistry.deactivatePortal(world, pos);
			}
		}
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos sourcePos) {
		if (!world.isRemote) {
			if (neighborBlock instanceof BlockPortalFrame || neighborBlock == SimplePortals.blockPortal)
				return;

			// Deactivate all portals that share this frame block if an address block was
			// removed or changed.

			List<Portal> affectedPortals = PortalRegistry.getPortalsAt(pos, world.provider.getDimension());

			if (affectedPortals == null || affectedPortals.size() < 1)
				return;

			Portal firstPortal = affectedPortals.get(0);

			if (firstPortal.hasAddressChanged(world)) {
				PortalRegistry.deactivatePortal(world, pos);
			}
		}

		super.neighborChanged(state, world, pos, neighborBlock, sourcePos);
	}
}