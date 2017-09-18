package net.zarathul.simpleportals.blocks;

import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.zarathul.simpleportals.configuration.Config;
import net.zarathul.simpleportals.registration.*;

/**
 * Represents a frame of the portal multiblock that supplies comparators with a
 * redstone signal. The signal strength is based on the amount of power stored
 * inside the portal.
 */
public class BlockPowerGauge extends BlockPortalFrame {
	public BlockPowerGauge() {
		super(Registry.BLOCK_POWER_GAUGE_NAME);
	}

	@Override
	public boolean hasComparatorInputOverride(IBlockState state) {
		return true;
	}

	@Override
	public int getComparatorInputOverride(IBlockState state, World world, BlockPos pos) {
		List<Portal> portals = PortalRegistry.getPortalsAt(pos, world.provider.getDimension());

		if (portals != null && portals.size() > 0) {
			int signalSum = 0;

			for (Portal portal : portals) {
				signalSum += getSignalStrength(portal);
			}

			int combinedSignal = MathHelper.floor(signalSum / (float) portals.size());

			return combinedSignal;
		}

		return 0;
	}

	/**
	 * Calculates the comparator signal strength for the specified portal.
	 * 
	 * @param portal
	 *            The portal to calculate the signal strength for.
	 * @return <code>0</code> if <i>portal</i> was <code>null</code> or the power
	 *         system is disabled, otherwise a value between <code>0</code> and
	 *         <code>15</code>.
	 */
	private int getSignalStrength(Portal portal) {
		if (portal != null && Config.powerCost > 0 && Config.powerCapacity > 0) {
			int maxUses = MathHelper.floor(Config.powerCapacity / (float) Config.powerCost);

			if (maxUses > 0) {
				int power = PortalRegistry.getPower(portal);
				int uses = MathHelper.floor(power / (float) Config.powerCost);

				int signalStrength = MathHelper.floor((uses / (float) maxUses) * 14.0f) + ((uses > 0) ? 1 : 0);

				return Math.min(signalStrength, 15);
			}
		}

		return 0;
	}
}
