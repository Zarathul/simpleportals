package net.zarathul.simpleportals.registration;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.world.World;
import net.minecraftforge.common.util.INBTSerializable;
import net.zarathul.simpleportals.common.Utils;

/**
 * Represents a portal.<br>
 * Note: Corner1 and Corner4 must be diagonal to each other,
 * same for Corner2 and Corner3.
 */
public class Portal implements INBTSerializable<NBTTagCompound>
{
	private int dimension;
	private Address address;
	private Axis axis;
	private Corner corner1;
	private Corner corner2;
	private Corner corner3;
	private Corner corner4;
	
	public Portal()
	{
	}
	
	public Portal(
			int dimension, Address address, Axis axis,
			Corner corner1, Corner corner2,
			Corner corner3, Corner corner4)
	{
		this.dimension = dimension;
		this.address = address;
		this.axis = axis;
		this.corner1 = corner1;
		this.corner2 = corner2;
		this.corner3 = corner3;
		this.corner4 = corner4;
	}
	
	/**
	 * Gets the dimension the portal is located in.
	 * 
	 * @return
	 * An <code>int</code> representing the dimension id.
	 */
	public int getDimension()
	{
		return dimension;
	}
	
	/**
	 * Gets the portals address.
	 * 
	 * @return
	 * The portals {@link Address} or <code>null</code>.
	 */
	public Address getAddress()
	{
		return address;
	}
	
	/**
	 * Gets the axis the portal is aligned to.
	 * 
	 * @return
	 * One of the {@link Axis} values or <code>null</code>.
	 */
	public Axis getAxis()
	{
		return axis;
	}
	
	/**
	 * Gets the portals first corner.
	 * 
	 * @return
	 * A {@link Corner} or <code>null</code>.
	 */
	public Corner getCorner1()
	{
		return corner1;
	}
	
	/**
	 * Gets the portals second corner.
	 * 
	 * @return
	 * A {@link Corner} or <code>null</code>.
	 */
	public Corner getCorner2()
	{
		return corner2;
	}
	
	/**
	 * Gets the portals third corner.
	 * 
	 * @return
	 * A {@link Corner} or <code>null</code>.
	 */
	public Corner getCorner3()
	{
		return corner3;
	}
	
	/**
	 * Gets the portals fourth corner.
	 * 
	 * @return
	 * A {@link Corner} or <code>null</code>.
	 */
	public Corner getCorner4()
	{
		return corner4;
	}
	
	/**
	 * Gets the positions of all blocks making up the portal.
	 * 
	 * @return
	 * An {@link Iterable} of {@link BlockPos}.
	 */
	public Iterable<BlockPos> getAllPositions()
	{
		return BlockPos.getAllInBox(corner1.getPos(), corner4.getPos());
	}
	
	/**
	 * Gets the positions of the actual portal blocks inside the portal frame.
	 * 
	 * @return
	 * An {@link Iterable} of {@link BlockPos}.
	 */
	public Iterable<BlockPos> getPortalPositions()
	{
		return BlockPos.getAllInBox(corner1.getInnerCornerPos(), corner4.getInnerCornerPos());
	}
	
	/**
	 * Gets the positions of all blocks making up the portals frame.
	 * 
	 * @return
	 * An {@link Iterable} of {@link BlockPos}.
	 */
	public Iterable<BlockPos> getFramePositions()
	{
		List<BlockPos> all = new ArrayList<BlockPos>();
		
		for (BlockPos pos : getAllPositions()) all.add(pos);
		
		List<BlockPos> portals = new ArrayList<BlockPos>();
		
		for (BlockPos pos : getPortalPositions()) portals.add(pos);
		
		List<BlockPos> frame = all.stream().filter(e -> !portals.contains(e)).collect(Collectors.toList());
		
		return new Iterable<BlockPos>()
		{
			@Override
			public Iterator<BlockPos> iterator()
			{
				return frame.iterator();
			}
		};
	}
	
	/**
	 * Gets a possible spawn location for an entity of the specified height.
	 * 
	 * @param world
	 * The {@link World} the portal is located in.
	 * @param entityHeight
	 * The height of the entity the spawn point should be searched for.
	 * @return
	 * A {@link BlockPos} representing a possible spawn location or <code>null</code>.
	 */
	public BlockPos getPortDestination(World world, int entityHeight)
	{
		if (world == null || entityHeight < 1) return null;
		
		// Horizontal portal.
		
		if (axis == Axis.Y)
		{
			Iterable<BlockPos> framePositions = getFramePositions();
			
			BlockPos spawnLocation = null;
			
			// Check for valid spawn positions on top of the frame blocks.
			
			for (BlockPos framePos : framePositions)
			{
				spawnLocation = framePos.up();
				if (canEntitySpawnAt(world, spawnLocation, entityHeight)) return spawnLocation;
			}
			
			// Check for valid spawn positions below the portal blocks starting at the center.
			
			BlockPos portal1 = corner1.getInnerCornerPos();
			BlockPos portal2 = corner4.getInnerCornerPos();
			Axis[] portalAxis = new Axis[] { Axis.X, Axis.Z };
			int[] mins = new int[2];
			int[] maxs = new int[2];
			
			for (int i = 0; i < portalAxis.length; i++)
			{
				if (Utils.getAxisValue(portal1, portalAxis[i]) < Utils.getAxisValue(portal2, portalAxis[i]))
				{
					mins[i] = Utils.getAxisValue(portal1, portalAxis[i]);
					maxs[i] = Utils.getAxisValue(portal2, portalAxis[i]);
				}
				else
				{
					mins[i] = Utils.getAxisValue(portal2, portalAxis[i]);
					maxs[i] = Utils.getAxisValue(portal1, portalAxis[i]);
				}
			}
			
			int minX = mins[0];
			int maxX = maxs[0];
			int minZ = mins[1];
			int maxZ = maxs[1];
			int y = portal1.getY() - entityHeight;
			int width = Math.abs(maxX - minX) + 1;
			int height = Math.abs(maxZ - minZ) + 1;
			int halfWidth = Math.floorDiv(width, 2);
			int halfHeight = Math.floorDiv(height, 2);
			
			EnumFacing[] zAxisFacings = new EnumFacing[] { EnumFacing.SOUTH, EnumFacing.NORTH };
			BlockPos center = new BlockPos(minX + halfWidth, y, minZ + halfHeight);
			BlockPos currentPos;
			
			for (int z = 0; z <= halfHeight; z++)
			{
				for (EnumFacing zFacing : zAxisFacings)
				{
					for (int x = 0; x <= halfWidth; x++)
					{
						currentPos = center.east(x).offset(zFacing, z);
						
						if (currentPos.getX() <= maxX && currentPos.getZ() <= maxZ)
						{
							if (canEntitySpawnAt(world, currentPos, entityHeight)) return currentPos;
						}
						
						currentPos = center.west(x).offset(zFacing, z);
						
						if (currentPos.getX() >= minX && currentPos.getZ() >= minZ)
						{
							if (canEntitySpawnAt(world, currentPos, entityHeight)) return currentPos;
						}
					}
				}
			}
			
			return null;
		}
		
		// Vertical portal.
		
		BlockPos portal1 = corner1.getInnerCornerPos();
		BlockPos portal2 = corner4.getInnerCornerPos();
		int width = 0, height = 0, lowBound = 0, highBound = 0;
		
		width = Math.abs(Utils.getAxisValue(portal1, axis) - Utils.getAxisValue(portal2, axis)) + 1;
		height = Math.abs(portal1.getY() - portal2.getY()) + 1;
		
		if (Utils.getAxisValue(portal1, axis) < Utils.getAxisValue(portal2, axis))
		{
			lowBound = Utils.getAxisValue(portal1, axis);
			highBound = Utils.getAxisValue(portal2, axis);
		}
		else
		{
			lowBound = Utils.getAxisValue(portal2, axis);
			highBound = Utils.getAxisValue(portal1, axis);
		}
		
		int halfWidth = Math.floorDiv(width, 2);
		int middle = lowBound + halfWidth;
		int startHeight = (portal1.getY() < portal2.getY()) ? portal1.getY() : portal2.getY();
		
		// EnumFacing.func_181076_a gets the facing on the specified axis in the specified direction.
		// e.g. Axis.Z and AxisDirection.POSITIVE returns EnumFacing.SOUTH.
		EnumFacing searchDirPositive = EnumFacing.func_181076_a(AxisDirection.POSITIVE, axis);
		EnumFacing searchDirNegative = EnumFacing.func_181076_a(AxisDirection.NEGATIVE, axis);
		
		BlockPos feetPos = null;
		BlockPos searchStartPos1 = (axis == Axis.X)
				? new BlockPos(middle, startHeight, portal1.south(1).getZ())
				: new BlockPos(portal1.east(1).getX(), startHeight, middle);
		
		BlockPos searchStartPos2 = (axis == Axis.X)
				? new BlockPos(middle, startHeight, portal1.north(1).getZ())
				: new BlockPos(portal1.west(1).getX(), startHeight, middle);
		
		BlockPos[] searchStartPositions = new BlockPos[] { searchStartPos1, searchStartPos2 };
		
		BlockPos currentFeetPos;
		
		// Find the lowest position where the entity can spawn at at either side.
		// Search order is south before north and east before west.
		
		for (int y = 0; y <= height - entityHeight; y++)
		{
			for (BlockPos startPos : searchStartPositions)
			{
				currentFeetPos = startPos.up(y);
				
				for (int x = 0; x <= halfWidth; x++)
				{
					feetPos = currentFeetPos.offset(searchDirPositive, x);
					
					if (Utils.getAxisValue(feetPos, axis) <= highBound)
					{
						if (canEntitySpawnAt(world, feetPos, entityHeight)) return feetPos;
					}
					
					feetPos = currentFeetPos.offset(searchDirNegative, x);
					
					if (Utils.getAxisValue(feetPos, axis) >= lowBound)
					{
						if (canEntitySpawnAt(world, feetPos, entityHeight)) return feetPos;
					}
				}
			}
		}
		
		return null;
	}
	
	@Override
	public NBTTagCompound serializeNBT()
	{
		NBTTagCompound tag = new NBTTagCompound();
		tag.setInteger("dimension", dimension);
		tag.setTag("address", address.serializeNBT());
		tag.setString("axis", axis.name());
		tag.setTag("corner1", corner1.serializeNBT());
		tag.setTag("corner2", corner2.serializeNBT());
		tag.setTag("corner3", corner3.serializeNBT());
		tag.setTag("corner4", corner4.serializeNBT());
		
		return tag;
	}
	
	@Override
	public void deserializeNBT(NBTTagCompound nbt)
	{
		if (nbt == null) return;
		
		dimension = nbt.getInteger("dimension");
		
		address = new Address();
		address.deserializeNBT((NBTTagCompound)nbt.getTag("address"));
		
		axis = Axis.byName(nbt.getString("axis"));
		
		corner1 = new Corner();
		corner1.deserializeNBT((NBTTagCompound)nbt.getTag("corner1"));
		
		corner2 = new Corner();
		corner2.deserializeNBT((NBTTagCompound)nbt.getTag("corner2"));
		
		corner3 = new Corner();
		corner3.deserializeNBT((NBTTagCompound)nbt.getTag("corner3"));
		
		corner4 = new Corner();
		corner4.deserializeNBT((NBTTagCompound)nbt.getTag("corner4"));
	}
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + dimension;
		result = prime * result + ((address == null) ? 0 : address.hashCode());
		result = prime * result + ((axis == null) ? 0 : axis.hashCode());
		result = prime * result + ((corner1 == null) ? 0 : corner1.hashCode());
		result = prime * result + ((corner2 == null) ? 0 : corner2.hashCode());
		result = prime * result + ((corner3 == null) ? 0 : corner3.hashCode());
		result = prime * result + ((corner4 == null) ? 0 : corner4.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		
		Portal other = (Portal) obj;
		
		if (dimension != other.dimension) return false;
		
		if (axis != other.axis) return false;
		
		if (address == null)
		{
			if (other.address != null)
				return false;
		}
		else if (!address.equals(other.address)) return false;
		
		if (corner1 == null)
		{
			if (other.corner1 != null) return false;
		}
		else if (!corner1.equals(other.corner1)) return false;
		
		if (corner2 == null)
		{
			if (other.corner2 != null) return false;
		}
		else if (!corner2.equals(other.corner2)) return false;
		
		if (corner3 == null)
		{
			if (other.corner3 != null) return false;
		}
		else if (!corner3.equals(other.corner3)) return false;
		
		if (corner4 == null)
		{
			if (other.corner4 != null) return false;
		}
		else if (!corner4.equals(other.corner4)) return false;
		
		return true;
	}
	
	/**
	 * Check if an entity of the specified height can spawn at the specified 
	 * position.
	 * 
	 * @param world
	 * The {@link World} to check in.
	 * @param pos
	 * The position of the lowest point (feet) of the entity.
	 * @param entityHeight
	 * The entities height.
	 * @return
	 * <code>true</code> if the entity can spawn at the location, otherwise <code>false</code>.
	 */
	private boolean canEntitySpawnAt(World world, BlockPos pos, int entityHeight)
	{
		if (world == null || pos == null || entityHeight < 1) return false;
		
		for (int i = 0; i < entityHeight; i ++)
		{
			if (!world.isAirBlock(pos.up(i))) return false;
		}
		
		return true;
	}
}