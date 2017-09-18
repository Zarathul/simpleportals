package net.zarathul.simpleportals.registration;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.INBTSerializable;

/**
 * Represents a corner of a portal.
 */
public class Corner implements INBTSerializable<NBTTagCompound> {
	private BlockPos pos;
	private EnumFacing facingA;
	private EnumFacing facingB;

	public Corner() {
	}

	public Corner(BlockPos pos, EnumFacing facingA, EnumFacing facingB) {
		this.pos = pos;
		this.facingA = facingA;
		this.facingB = facingB;
	}

	/**
	 * Gets the corners position.
	 * 
	 * @return The BlockPos representing the corners position in the world.
	 */
	public BlockPos getPos() {
		return pos;
	}

	/**
	 * The first direction where portal frames are located.
	 * 
	 * @return One of the {@link EnumFacing} values.
	 */
	public EnumFacing getFacingA() {
		return facingA;
	}

	/**
	 * The second direction where portal frames are located.
	 * 
	 * @return One of the {@link EnumFacing} values.
	 */
	public EnumFacing getFacingB() {
		return facingB;
	}

	/**
	 * Gets the position of the block enclosed by the corner.
	 * 
	 * @return A BlockPos representing the inside of the corner.
	 */
	public BlockPos getInnerCornerPos() {
		return (pos != null && facingA != null && facingB != null) ? pos.offset(facingA).offset(facingB) : null;
	}

	@Override
	public NBTTagCompound serializeNBT() {
		NBTTagCompound tag = new NBTTagCompound();
		tag.setLong("pos", pos.toLong());
		tag.setString("facingA", facingA.name());
		tag.setString("facingB", facingB.name());

		return tag;
	}

	@Override
	public void deserializeNBT(NBTTagCompound nbt) {
		if (nbt == null)
			return;

		pos = BlockPos.fromLong(nbt.getLong("pos"));
		facingA = EnumFacing.byName(nbt.getString("facingA"));
		facingB = EnumFacing.byName(nbt.getString("facingB"));
	}

	@Override
	public String toString() {
		return pos + " : " + facingA + " / " + facingB;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((facingA == null) ? 0 : facingA.hashCode());
		result = prime * result + ((facingB == null) ? 0 : facingB.hashCode());
		result = prime * result + ((pos == null) ? 0 : pos.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;

		Corner other = (Corner) obj;

		if (pos == null) {
			if (other.pos != null)
				return false;
		} else if (!pos.equals(other.pos))
			return false;

		return ((facingA == other.facingA && facingB == other.facingB)
				|| (facingA == other.facingB && facingB == other.facingA));
	}
}