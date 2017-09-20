package net.zarathul.simpleportals.items;

import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.zarathul.simpleportals.SimplePortals;
import net.zarathul.simpleportals.common.Utils;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Power gauge in item form.
 */
public class ItemPowerGauge extends ItemBlock
{
	private static final String toolTipKey = "item." + SimplePortals.ITEM_POWER_GAUGE_NAME + ".toolTip";
	private static final String toolTipDetailsKey = "item." + SimplePortals.ITEM_POWER_GAUGE_NAME + ".toolTipDetails";

	public ItemPowerGauge(Block block)
	{
		super(block);
		
		setMaxStackSize(64);
		setCreativeTab(SimplePortals.creativeTab);
		setRegistryName(SimplePortals.ITEM_POWER_GAUGE_NAME);
		setUnlocalizedName(SimplePortals.ITEM_POWER_GAUGE_NAME);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn)
	{
		if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT))
		{
			tooltip.addAll(Utils.multiLineTranslateToLocal(toolTipDetailsKey, 1));
		}
		else
		{
			tooltip.add(I18n.translateToLocal(toolTipKey));
		}
	}
}