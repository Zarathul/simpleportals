package net.zarathul.simpleportals.items;

import java.util.List;

import org.lwjgl.input.Keyboard;

import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.zarathul.simpleportals.SimplePortals;
import net.zarathul.simpleportals.common.Utils;
import net.zarathul.simpleportals.registration.Registry;

/**
 * Power gauge in item form.
 */
public class ItemPowerGauge extends ItemBlock {
	private static final String toolTipKey = "item." + Registry.ITEM_POWER_GAUGE_NAME + ".toolTip";
	private static final String toolTipDetailsKey = "item." + Registry.ITEM_POWER_GAUGE_NAME + ".toolTipDetails";

	public ItemPowerGauge(Block block) {
		super(block);

		setMaxStackSize(64);
		setCreativeTab(SimplePortals.creativeTab);
		setRegistryName(Registry.ITEM_POWER_GAUGE_NAME);
		setUnlocalizedName(Registry.ITEM_POWER_GAUGE_NAME);
	}

	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> list, ITooltipFlag flagIn) {
		if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
			list.addAll(Utils.multiLineTranslateToLocal(toolTipDetailsKey, 1));
		} else {
			list.add(I18n.translateToLocal(toolTipKey));
		}
	}

}