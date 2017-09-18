package net.zarathul.simpleportals.blocks;

import net.minecraft.block.material.*;

/**
 * The material for {@link BlockPortalFrame}s.
 */
public class PortalFrameMaterial extends Material {
	public static final Material portalFrameMaterial = new PortalFrameMaterial();

	public PortalFrameMaterial() {
		super(MapColor.BLACK);
	}

	@Override
	public boolean isLiquid() {
		return false;
	}

	@Override
	public boolean isSolid() {
		return true;
	}

	@Override
	public boolean blocksMovement() {
		return true;
	}

	@Override
	public boolean getCanBurn() {
		return false;
	}

	@Override
	public boolean isReplaceable() {
		return false;
	}

	@Override
	public boolean isOpaque() {
		return true;
	}

	@Override
	public boolean isToolNotRequired() {
		return false;
	}

	@Override
	public EnumPushReaction getMobilityFlag() {
		return EnumPushReaction.BLOCK;
	}
}