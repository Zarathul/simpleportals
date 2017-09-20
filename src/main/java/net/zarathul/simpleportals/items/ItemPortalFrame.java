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
 * Portal frame in item form.
 */
public class ItemPortalFrame extends ItemBlock
{
	private static final String toolTipKey = "item." + SimplePortals.ITEM_PORTAL_FRAME_NAME + ".toolTip";
	private static final String toolTipDetailsKey = "item." + SimplePortals.ITEM_PORTAL_FRAME_NAME + ".toolTipDetails";

	public ItemPortalFrame(Block block)
	{
		super(block);
		
		setMaxStackSize(64);
		setCreativeTab(SimplePortals.creativeTab);
		setRegistryName(SimplePortals.ITEM_PORTAL_FRAME_NAME);
		setUnlocalizedName(SimplePortals.ITEM_PORTAL_FRAME_NAME);
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