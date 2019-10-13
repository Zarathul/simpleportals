package net.zarathul.simpleportals.items;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.zarathul.simpleportals.SimplePortals;
import net.zarathul.simpleportals.common.Utils;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Power gauge in item form.
 */
public class ItemPowerGauge extends BlockItem
{
	private static final String toolTipKey = "item." + SimplePortals.MOD_ID + "." + SimplePortals.ITEM_POWER_GAUGE_NAME + ".tooltip";
	private static final String toolTipDetailsKey = "item." + SimplePortals.MOD_ID + "." + SimplePortals.ITEM_POWER_GAUGE_NAME + ".tooltip_details";

	public ItemPowerGauge(Block block)
	{
		super(block, new Item.Properties().maxStackSize(64).group(SimplePortals.creativeTab));
		
		setRegistryName(SimplePortals.ITEM_POWER_GAUGE_NAME);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag)
	{
		long windowHandle = Minecraft.getInstance().mainWindow.getHandle();
		int leftShiftState = GLFW.glfwGetKey(windowHandle, GLFW.GLFW_KEY_LEFT_SHIFT);
		int rightShiftState = GLFW.glfwGetKey(windowHandle, GLFW.GLFW_KEY_RIGHT_SHIFT);

		// This does not work for some reason.
		//KeyBinding SneakKey = Minecraft.getInstance().gameSettings.keyBindSneak;
		//if (SneakKey.isKeyDown())

		if (leftShiftState == GLFW.GLFW_PRESS || rightShiftState == GLFW.GLFW_PRESS)
		{
			tooltip.addAll(Utils.multiLineTranslate(toolTipDetailsKey, 1));
		}
		else
		{
			tooltip.add(new TranslationTextComponent(toolTipKey));
		}
	}
}