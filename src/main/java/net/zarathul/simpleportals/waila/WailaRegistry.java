package net.zarathul.simpleportals.waila;

import mcp.mobius.waila.api.IWailaRegistrar;
import net.minecraft.util.StatCollector;
import net.zarathul.simpleportals.SimplePortals;
import net.zarathul.simpleportals.blocks.BlockPortalFrame;

/**
 * Hosts the registry callback for Waila.
 */
public final class WailaRegistry
{
	private static final String WAILA_POWER_CAPACITY = "powerCapacity";
	private static final String WAILA_WAILA_ADDRESS = "address";
	private static final String WAILA_WAILA_ADDRESS_COMPONENT = "addressComponent";

	public static final String WAILA_POWER_CAPACITY_KEY = SimplePortals.MOD_ID + WAILA_POWER_CAPACITY;
	public static final String WAILA_ADDRESS_KEY = SimplePortals.MOD_ID + WAILA_WAILA_ADDRESS;

	private static final String WAILA = "waila.";
	private static final String WAILA_POWER_CAPACITY_LOCA = WAILA + WAILA_POWER_CAPACITY;
	private static final String WAILA_ADDRESS_LOCA = WAILA + WAILA_WAILA_ADDRESS;

	public static final String WAILA_TOOLTIP = WAILA + "toolTip.";
	public static final String WAILA_TOOLTIP_POWER_CAPACITY = WAILA_TOOLTIP + WAILA_POWER_CAPACITY;
	public static final String WAILA_TOOLTIP_ADDRESS = WAILA_TOOLTIP + WAILA_WAILA_ADDRESS;
	public static final String WAILA_TOOLTIP_ADDRESS_COMPONENT = WAILA_TOOLTIP + WAILA_WAILA_ADDRESS_COMPONENT;

	/**
	 * Registers config options and tooltip providers for Waila. (Only called by Waila, don't call this method directly).
	 * 
	 * @param registrar
	 * The registration interface provided by Waila.
	 */
	public static final void register(IWailaRegistrar registrar)
	{
		registrar.addConfig(SimplePortals.MOD_READABLE_NAME, WAILA_POWER_CAPACITY_KEY, StatCollector.translateToLocal(WAILA_POWER_CAPACITY_LOCA));
		registrar.addConfig(SimplePortals.MOD_READABLE_NAME, WAILA_ADDRESS_KEY, StatCollector.translateToLocal(WAILA_ADDRESS_LOCA));

		registrar.registerBodyProvider(BlockPortalFrameDataProvider.instance, BlockPortalFrame.class);
	}
}
