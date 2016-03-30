package net.zarathul.simpleportals.items;

import java.util.List;

import org.lwjgl.input.Keyboard;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.zarathul.simpleportals.SimplePortals;
import net.zarathul.simpleportals.common.Utils;
import net.zarathul.simpleportals.registration.Registry;

/**
 * Power gauge in item form.
 */
public class ItemPowerGauge extends ItemBlock
{
	private static final String toolTipKey = "item." + Registry.ITEM_POWER_GAUGE_NAME + ".toolTip";
	private static final String toolTipDetailsKey = "item." + Registry.ITEM_POWER_GAUGE_NAME + ".toolTipDetails";

	public ItemPowerGauge(Block block)
	{
		super(block);
		
		setMaxStackSize(64);
		setCreativeTab(SimplePortals.creativeTab);
		setUnlocalizedName(Registry.ITEM_POWER_GAUGE_NAME);
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
			list.add(StatCollector.translateToLocal(toolTipKey));
		}
	}
}