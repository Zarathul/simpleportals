package net.zarathul.simpleportals.registration;

import com.google.common.base.Strings;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * Represents the address of a portal.<br>
 * The address consists of 4 blockIds as provided by
 * {@link PortalRegistry#getAddressBlockId(net.minecraft.block.state.IBlockState)}.
 * Multiple blocks with the same name/meta are allowed.
 */
public class Address implements INBTSerializable<NBTTagCompound>
{
	private static final int LENGTH = 4;
	
	private String readableName;
	
	private Map<String, Integer> blockCounts;

	public Address()
	{
		blockCounts = new TreeMap<>();
	}
	
	public Address(String ... blockIds)
	{
		this();
		initBlockCounts(blockIds);
	}
	
	/**
	 * Gets the number of times the specified blockId
	 * is contained in this address.
	 * 
	 * @param blockId
	 * The blockId.
	 * @return
	 * A value between <code>0</code> and <code>4</code>.
	 */
	public int getBlockCount(String blockId)
	{
		if (!Strings.isNullOrEmpty(blockId))
		{
			if (blockCounts.containsKey(blockId)) return blockCounts.get(blockId);
		}
		
		return 0;
	}
	
	@Override
	public NBTTagCompound serializeNBT()
	{
		NBTTagCompound mainTag = new NBTTagCompound();
		NBTTagCompound countTag;
		
		int i = 0;
		
		for (Entry<String, Integer> blockCount : blockCounts.entrySet())
		{
			countTag = new NBTTagCompound();
			countTag.setString("id", blockCount.getKey());
			countTag.setInteger("count", blockCount.getValue());
			
			mainTag.setTag(String.valueOf(i++), countTag);
		}
		
		return mainTag;
	}
	
	@Override
	public void deserializeNBT(NBTTagCompound nbt)
	{
		if (nbt == null) return;
		
		int i = 0;
		String key;
		NBTTagCompound countTag;
		
		while (nbt.hasKey(key = String.valueOf(i++)))
		{
			countTag = nbt.getCompoundTag(key);
			blockCounts.put(countTag.getString("id"), countTag.getInteger("count"));
		}
		
		readableName = generateReadableName();
	}
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((blockCounts == null) ? 0 : blockCounts.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		
		Address other = (Address) obj;
		
		// Because the block counts are stored in a TreeMap the order and therefore the readable name
		// should always be identical.
		return readableName.equals(other.toString());
	}
	
	@Override
	public String toString()
	{
		return readableName;
	}
	
	/**
	 * Counts the specified block ids and fills the block count
	 * map accordingly. Also generates the readable name.
	 * 
	 * @param blockIds
	 * The block ids to generate the address from.
	 */
	private void initBlockCounts(String[] blockIds)
	{
		if (blockIds != null && blockIds.length >= LENGTH)
		{
			int oldCount;
			String currentId; 
			
			for (int i = 0; i < LENGTH; i++)
			{
				currentId = blockIds[i];
				
				if (!Strings.isNullOrEmpty(currentId))
				{
					if (!blockCounts.containsKey(currentId))
					{
						blockCounts.put(currentId, 1);
					}
					else
					{
						oldCount = blockCounts.get(currentId);
						blockCounts.put(currentId, ++oldCount);
					}
				}
			}
			
			readableName = generateReadableName();
		}
	}
	
	/**
	 * Generates a readable representation of the address.
	 * 
	 * @return
	 * A string of the format <code>blockIdxblockCount</code> for every
	 * block id, delimited by "<code>,</code>".
	 */
	private String generateReadableName()
	{
		if (blockCounts == null) return null;
		
		StringBuilder nameBuilder = new StringBuilder();
		
		for (Entry<String, Integer> blockCount : blockCounts.entrySet())
		{
			nameBuilder.append(blockCount.getValue());
			nameBuilder.append('x');
			nameBuilder.append(blockCount.getKey());
			nameBuilder.append(", ");
		}
		
		nameBuilder.delete(nameBuilder.length() - 2, nameBuilder.length());
		
		return nameBuilder.toString();
	}
}
