package net.zarathul.simpleportals.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.zarathul.simpleportals.SimplePortals;
import net.zarathul.simpleportals.configuration.Config;
import net.zarathul.simpleportals.registration.Portal;
import net.zarathul.simpleportals.registration.PortalRegistry;

import java.util.List;

/**
 * Represents a frame of the portal multiblock that supplies comparators with a redstone
 * signal. The signal strength is based on the amount of power stored inside the portal.
 */
public class BlockPowerGauge extends BlockPortalFrame
{
	public BlockPowerGauge()
	{
		super(SimplePortals.BLOCK_POWER_GAUGE_NAME);
	}

	@Override
	public boolean hasComparatorInputOverride(BlockState state)
	{
		return true;
	}
	
	@Override
	public int getComparatorInputOverride(BlockState state, World world, BlockPos pos)
	{
		if (!world.isRemote)
		{
			List<Portal> portals = PortalRegistry.getPortalsAt(pos, world.getDimension().getType());

			if (portals != null && portals.size() > 0)
			{
				int signalSum = 0;

				for (Portal portal : portals)
				{
					signalSum += getSignalStrength(portal);
				}

				return MathHelper.floor(signalSum / (float)portals.size());		// combined signal strength
			}
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
		if (portal != null && Config.powerCost.get() > 0 && Config.powerCapacity.get() > 0)
		{
			int maxUses = MathHelper.floor(Config.powerCapacity.get() / (float)Config.powerCost.get());
			
			if (maxUses > 0)
			{
				int power = PortalRegistry.getPower(portal);
				int uses = MathHelper.floor(power / (float)Config.powerCost.get());
				
				int signalStrength = MathHelper.floor((uses / (float)maxUses) * 14.0f) + ((uses > 0) ? 1 : 0);
				
				return Math.min(signalStrength, 15);
			}
		}
		
		return 0;
	}
}
