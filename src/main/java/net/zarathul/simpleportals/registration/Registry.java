package net.zarathul.simpleportals.registration;

import java.util.Arrays;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;
import net.zarathul.simpleportals.SimplePortals;
import net.zarathul.simpleportals.blocks.*;
import net.zarathul.simpleportals.items.*;

/**
 * Provides helper methods to register blocks, items, custom renderers etc.
 */
public final class Registry {
	public static final String BLOCK_PORTAL_NAME = "blockPortal";

	public static final String BLOCK_PORTAL_FRAME_NAME = "blockPortalFrame";
	public static final String ITEM_PORTAL_FRAME_NAME = "itemPortalFrame";

	public static final String BLOCK_POWER_GAUGE_NAME = "blockPowerGauge";
	public static final String ITEM_POWER_GAUGE_NAME = "itemPowerGauge";

	public static final String ITEM_PORTAL_ACTIVATOR_NAME = "itemPortalActivator";

	/**
	 * Creates and registers all blocks added by the mod.
	 */
	@SubscribeEvent
	public void registerBlocks(RegistryEvent.Register<Block> event) {
		IForgeRegistry<Block> reg = event.getRegistry();
		// BlockPortal
		SimplePortals.blockPortal = new BlockPortal();
		reg.register(SimplePortals.blockPortal);

		// BlockPortalFrame
		SimplePortals.blockPortalFrame = new BlockPortalFrame();
		reg.register(SimplePortals.blockPortalFrame);

		// BlockPowerGauge
		SimplePortals.blockPowerGauge = new BlockPowerGauge();
		reg.register(SimplePortals.blockPowerGauge);
	}

	/**
	 * Creates and registers all items added by the mod.
	 */
	@SubscribeEvent
	public void registerItems(RegistryEvent.Register<Item> event) {
		IForgeRegistry<Item> reg = event.getRegistry();
		SimplePortals.itemPortalFrame = new ItemPortalFrame(SimplePortals.blockPortalFrame);
		SimplePortals.proxy.registerItemRenderer(SimplePortals.itemPortalFrame, 0, BLOCK_PORTAL_FRAME_NAME);
		reg.register(SimplePortals.itemPortalFrame);

		SimplePortals.itemPowerGauge = new ItemPowerGauge(SimplePortals.blockPowerGauge);
		SimplePortals.proxy.registerItemRenderer(SimplePortals.itemPowerGauge, 0, BLOCK_POWER_GAUGE_NAME);
		reg.register(SimplePortals.itemPowerGauge);

		SimplePortals.itemPortalActivator = new ItemPortalActivator();
		SimplePortals.proxy.registerItemRenderer(SimplePortals.itemPortalActivator, 0, ITEM_PORTAL_ACTIVATOR_NAME);
		reg.register(SimplePortals.itemPortalActivator);
	}

	/**
	 * Registers with Waila, if installed.
	 */
	public static final void registerWithWaila() {
		FMLInterModComms.sendMessage("waila", "register", "net.zarathul.simpleportals.waila.WailaRegistry.register");
	}

	/**
	 * Adds a creative mode tab.
	 */
	@SideOnly(Side.CLIENT)
	public static final void addCreativeTab() {
		// Check if a a "Simple Mods" tab already exists, otherwise make one.
		SimplePortals.creativeTab = Arrays.stream(CreativeTabs.CREATIVE_TAB_ARRAY)
				.filter(tab -> tab.getTabLabel().equals(SimplePortals.MOD_TAB_NAME)).findFirst()
				.orElseGet(() -> new CreativeTabs(SimplePortals.MOD_TAB_NAME) {
					private ItemStack iconStack;

					@Override
					public String getTranslatedTabLabel() {
						return this.getTabLabel();
					}

					@Override
					public ItemStack getTabIconItem() {
						if (iconStack == null)
							iconStack = new ItemStack(SimplePortals.itemPortalFrame);

						return iconStack;
					}
				});
	}
}