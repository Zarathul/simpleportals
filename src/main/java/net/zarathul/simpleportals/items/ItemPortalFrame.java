package net.zarathul.simpleportals.items;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.zarathul.simpleportals.SimplePortals;
import net.zarathul.simpleportals.common.Utils;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Portal frame in item form.
 */
public class ItemPortalFrame extends BlockItem
{
	private static final String toolTipKey = "item." + SimplePortals.ITEM_PORTAL_FRAME_NAME + ".toolTip";
	private static final String toolTipDetailsKey = "item." + SimplePortals.ITEM_PORTAL_FRAME_NAME + ".toolTipDetails";

	public ItemPortalFrame(Block block)
	{
		super(block, new Item.Properties().maxStackSize(64).group(SimplePortals.creativeTab));

		setRegistryName(SimplePortals.ITEM_PORTAL_FRAME_NAME);
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
}