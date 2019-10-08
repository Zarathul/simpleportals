package net.zarathul.simpleportals.items;

import net.minecraft.block.BlockState;
import net.minecraft.block.DispenserBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.dispenser.IDispenseItemBehavior;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.zarathul.simpleportals.SimplePortals;
import net.zarathul.simpleportals.blocks.BlockPortalFrame;
import net.zarathul.simpleportals.common.Utils;
import net.zarathul.simpleportals.registration.PortalRegistry;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * The item used to activate portals.
 */
public class ItemPortalActivator extends Item
{
	private static final String toolTipKey = "item." + SimplePortals.ITEM_PORTAL_ACTIVATOR_NAME + ".toolTip";
	private static final String toolTipDetailsKey = "item." + SimplePortals.ITEM_PORTAL_ACTIVATOR_NAME + ".toolTipDetails";
	
	public ItemPortalActivator()
	{
		super(new Item.Properties().maxStackSize(1).group(SimplePortals.creativeTab));

		setRegistryName(SimplePortals.ITEM_PORTAL_ACTIVATOR_NAME);
		DispenserBlock.registerDispenseBehavior(this, dispenserBehavior);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn)
	{
		KeyBinding SneakKey = Minecraft.getInstance().gameSettings.keyBindSneak;

		if (SneakKey.isKeyDown())
		{
			tooltip.addAll(Utils.multiLineTranslateToLocal(toolTipDetailsKey, 1));
		}
		else
		{
			tooltip.add(new StringTextComponent(I18n.format(toolTipKey, null)));
		}
	}

	@Override
	public boolean doesSneakBypassUse(ItemStack stack, IWorldReader world, BlockPos pos, PlayerEntity player)
	{
		return true;
	}

	@Override
	public ActionResultType onItemUse(ItemUseContext context)
	{
		if (context.getWorld().getBlockState(context.getPos()).getBlock() instanceof BlockPortalFrame)
		{
			context.getPlayer().swingArm(context.getHand());
		}

		return super.onItemUse(context);
	}

	/**
	 * Custom dispenser behavior that allows dispensers to activate portals with a contained
	 * portal activator.
	 */
	private final static IDispenseItemBehavior dispenserBehavior = new IDispenseItemBehavior()
	{
		private final DefaultDispenseItemBehavior defaultBehavior = new DefaultDispenseItemBehavior();
		
		@Override
		public ItemStack dispense(IBlockSource source, ItemStack stack)
		{
			if (ItemStack.areItemsEqual(stack, new ItemStack(SimplePortals.itemPortalActivator)))
			{
				World world = source.getWorld();
				BlockState dispenser = world.getBlockState(source.getBlockPos());
				
				// Start searching for portal frame blocks in the direction the dispenser is facing.
				Direction dispenserFacing = dispenser.get(DispenserBlock.FACING);
				BlockPos searchStartPos = source.getBlockPos().offset(dispenserFacing);
				
				if (world.isAirBlock(searchStartPos))
				{
					// Search along the other two axis besides the one the dispenser is facing in.
					// E.g. dispenser faces south: Search one block south of the dispenser, up, down,
					// east and west.
					List<Direction> searchDirections = new ArrayList<>();
					Axis dispenserAxis = dispenserFacing.getAxis();
					
					for (Axis axis : Axis.values())
					{
						if (axis != dispenserAxis)
						{
							searchDirections.add(Direction.getFacingFromAxis(AxisDirection.POSITIVE, axis));
							searchDirections.add(Direction.getFacingFromAxis(AxisDirection.NEGATIVE, axis));
						}
					}
					
					BlockPos currentPos;
					
					for (Direction facing : searchDirections)
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