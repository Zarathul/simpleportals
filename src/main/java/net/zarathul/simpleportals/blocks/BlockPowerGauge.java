package net.zarathul.simpleportals.blocks;

import java.util.List;

import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.zarathul.simpleportals.SimplePortals;
import net.zarathul.simpleportals.configuration.Config;
import net.zarathul.simpleportals.registration.Portal;
import net.zarathul.simpleportals.registration.PortalRegistry;
import net.zarathul.simpleportals.registration.Registry;

/**
 * Represents a frame of the portal multiblock that supplies comparators with a redstone
 * signal. The signal strength is based on the amount of power stored inside the portal.
 */
public class BlockPowerGauge extends BlockPortalFrame
{
	public BlockPowerGauge()
	{
		setUnlocalizedName(Registry.BLOCK_POWER_GAUGE_NAME);
		setCreativeTab(SimplePortals.creativeTab);
	}
	
	@Override
	public boolean hasComparatorInputOverride()
	{
		return true;
	}

	@Override
	public int getComparatorInputOverride(World world, BlockPos pos)
	{
		List<Portal> portals = PortalRegistry.getPortalsAt(pos, world.provider.getDimensionId());
		
		if (portals != null && portals.size() > 0)
		{
			int signalSum = 0;
			
			for (Portal portal : portals)
			{
				signalSum += getSignalStrength(portal);
			}
			
			int combinedSignal = MathHelper.floor_float(signalSum / (float)portals.size());
			
			return combinedSignal;
		}
		
		return 0;
	}
	
	/**
	 * Calculates the comparator signal strength for the specified portal.
	 * 
	 * @param portal
	 * The portal to calculate the signal strength for.
	 * @return
	 * <code>0</code> if <i>portal</i> was <code>null</code> or the power system is disabled,
	 * otherwise a value between <code>0</code> and <code>15</code>.
	 */
	private int getSignalStrength(Portal portal)
	{
		if (portal != null && Config.powerCost > 0 && Config.powerCapacity > 0)
		{
			int maxUses = MathHelper.floor_float(Config.powerCapacity / (float)Config.powerCost);
			
			if (maxUses > 0)
			{
				int power = PortalRegistry.getPower(portal);
				int uses = MathHelper.floor_float(power / (float)Config.powerCost);
				
				int signalStrength = MathHelper.floor_float((uses / (float)maxUses) * 14.0f) + ((uses > 0) ? 1 : 0);
				
				return Math.min(signalStrength, 15);
			}
		}
		
		return 0;
	}
}
