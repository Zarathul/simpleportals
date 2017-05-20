package net.zarathul.simpleportals.items;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Keyboard;

import net.minecraft.block.BlockDispenser;
import net.minecraft.block.state.IBlockState;
import net.minecraft.dispenser.BehaviorDefaultDispenseItem;
import net.minecraft.dispenser.IBehaviorDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.zarathul.simpleportals.SimplePortals;
import net.zarathul.simpleportals.blocks.BlockPortalFrame;
import net.zarathul.simpleportals.common.Utils;
import net.zarathul.simpleportals.registration.PortalRegistry;
import net.zarathul.simpleportals.registration.Registry;

/**
 * The item used to activate portals.
 */
public class ItemPortalActivator extends Item
{
	private static final String toolTipKey = "item." + Registry.ITEM_PORTAL_ACTIVATOR_NAME + ".toolTip";
	private static final String toolTipDetailsKey = "item." + Registry.ITEM_PORTAL_ACTIVATOR_NAME + ".toolTipDetails";
	
	public ItemPortalActivator()
	{
		super();

		setMaxStackSize(1);
		setCreativeTab(SimplePortals.creativeTab);
		setRegistryName(Registry.ITEM_PORTAL_ACTIVATOR_NAME);
		setUnlocalizedName(Registry.ITEM_PORTAL_ACTIVATOR_NAME);
		
		BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(this, dispenserBehavior);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack items, EntityPlayer player, List list, boolean advancedItemTooltipsEnabled)
	{
		if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT))
		{
			list.addAll(Utils.multiLineTranslateToLocal(toolTipDetailsKey, 1));
		}
		else
		{
			list.add(I18n.translateToLocal(toolTipKey));
		}
	}
	
	@Override
	public boolean doesSneakBypassUse(ItemStack stack, IBlockAccess world, BlockPos pos, EntityPlayer player)
	{
		return true;
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand,
			 EnumFacing side, float hitX, float hitY, float hitZ)
	{
		if (world.getBlockState(pos).getBlock() instanceof BlockPortalFrame)
		{
			player.swingArm(hand);
		}
		
		return super.onItemUse(player, world, pos, hand, side, hitX, hitY, hitZ);
	}
	
	/**
	 * Custom dispenser behavior that allows dispensers to activate portals with a contained
	 * portal activator.
	 */
	private final static IBehaviorDispenseItem dispenserBehavior = new IBehaviorDispenseItem()
	{
		private final BehaviorDefaultDispenseItem defaultBehavior = new BehaviorDefaultDispenseItem();
		
		@Override
		public ItemStack dispense(IBlockSource source, ItemStack stack)
		{
			if (ItemStack.areItemsEqual(stack, new ItemStack(SimplePortals.itemPortalActivator)))
			{
				World world = source.getWorld();
				IBlockState dispenser = world.getBlockState(source.getBlockPos());
				
				// Start searching for portal frame blocks in the direction the dispenser is facing.
				EnumFacing dispenserFacing = dispenser.getValue(BlockDispenser.FACING);
				BlockPos searchStartPos = source.getBlockPos().offset(dispenserFacing);
				
				if (world.isAirBlock(searchStartPos))
				{
					// Search along the other two axis besides the one the dispenser is facing in.
					// E.g. dispenser faces south: Search one block south of the dispenser, up, down,
					// east and west.
					List<EnumFacing> searchDirections = new ArrayList<EnumFacing>();
					Axis dispenserAxis = dispenserFacing.getAxis();
					
					for (Axis axis : Axis.values())
					{
						if (axis != dispenserAxis)
						{
							searchDirections.add(EnumFacing.getFacingFromAxis(AxisDirection.POSITIVE, axis));
							searchDirections.add(EnumFacing.getFacingFromAxis(AxisDirection.NEGATIVE, axis));
						}
					}
					
					BlockPos currentPos;
					
					for (EnumFacing facing : searchDirections)
					{
						currentPos = searchStartPos.offset(facing);
						
						if (world.getBlockState(currentPos).getBlock() instanceof BlockPortalFrame)
						{
							if (PortalRegistry.activatePortal(world, currentPos, facing.getOpposite()))
							{
								return stack;
							}
						}
					}
				}
			}
			
			return defaultBehavior.dispense(source, stack);
		}
	};
}