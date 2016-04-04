package net.zarathul.simpleportals.blocks;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.zarathul.simpleportals.SimplePortals;
import net.zarathul.simpleportals.registration.Portal;
import net.zarathul.simpleportals.registration.PortalRegistry;
import net.zarathul.simpleportals.registration.Registry;

/**
 * Represents the frame of the portal mutliblock.
 */
public class BlockPortalFrame extends Block
{
	public BlockPortalFrame()
	{
		super(PortalFrameMaterial.portalFrameMaterial);
		
		setUnlocalizedName(Registry.BLOCK_PORTAL_FRAME_NAME);
		setCreativeTab(SimplePortals.creativeTab);
		setHardness(50.0f);
		setResistance(200.0f);
		setStepSound(soundTypePiston);
		setHarvestLevel("pickaxe", 3);
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player,
			EnumFacing side, float hitX, float hitY, float hitZ)
	{
		if (!world.isRemote)
		{
			ItemStack usedStack = player.getCurrentEquippedItem();
			
			if (usedStack != null)
			{
				Item usedItem = usedStack.getItem();
				
				if (usedItem == SimplePortals.itemPortalActivator)
				{
					if (player.isSneaking())
					{
						PortalRegistry.deactivatePortal(world, pos);
						
						world.setBlockToAir(pos);
						dropBlockAsItem(world, pos, this.getDefaultState(), 0);
					}
					else
					{
						PortalRegistry.activatePortal(world, pos, side);
					}
					
					return true;
				}
			}
		}
		
		return super.onBlockActivated(world, pos, state, player, side, hitX, hitY, hitZ);
	}

	@Override
	public void onNeighborBlockChange(World world, BlockPos pos, IBlockState state, Block neighborBlock)
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
}