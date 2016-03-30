package net.zarathul.simpleportals.common;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraft.world.storage.MapStorage;
import net.zarathul.simpleportals.SimplePortals;
import net.zarathul.simpleportals.registration.PortalRegistry;

/**
 * Responsible for saving/loading {@link PortalRegistry} data.
 */
public class PortalWorldSaveData extends WorldSavedData
{
	private static final String DATA_NAME = SimplePortals.MOD_ID;
	
	public PortalWorldSaveData(String name)
	{
		super(name);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		PortalRegistry.readFromNBT(nbt);
	}
	
	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		PortalRegistry.writeToNBT(nbt);
	}
	
	public static PortalWorldSaveData get(World world)
	{
		if (world == null) return null;
		
		MapStorage storage = world.getMapStorage();
		WorldSavedData instance = storage.loadData(PortalWorldSaveData.class, DATA_NAME);
		
		if (instance == null)
		{
			instance = new PortalWorldSaveData(DATA_NAME);
			storage.setData(DATA_NAME, instance);
		}
		
		return (PortalWorldSaveData)instance;
	}
}
