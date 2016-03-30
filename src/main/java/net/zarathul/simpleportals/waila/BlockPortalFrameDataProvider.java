package net.zarathul.simpleportals.waila;

import java.util.List;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.zarathul.simpleportals.configuration.Config;
import net.zarathul.simpleportals.registration.Portal;
import net.zarathul.simpleportals.registration.PortalRegistry;

/**
 * Provides Waila with tooltip information for portals when looking at portal frames.
 */
public final class BlockPortalFrameDataProvider implements IWailaDataProvider
{
	public static final BlockPortalFrameDataProvider instance = new BlockPortalFrameDataProvider();

	private BlockPortalFrameDataProvider()
	{
	}

	@Override
	public ItemStack getWailaStack(IWailaDataAccessor accessor, IWailaConfigHandler config)
	{
		return null;
	}

	@Override
	public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound tag, World world, BlockPos pos)
	{
		return null;
	}

	@Override
	public List<String> getWailaHead(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config)
	{
		return currenttip;
	}

	@Override
	public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config)
	{
		if (config.getConfig(WailaRegistry.WAILA_POWER_CAPACITY_KEY) || config.getConfig(WailaRegistry.WAILA_ADDRESS_KEY))
		{
			World world = accessor.getWorld();
			
			List<Portal> portals = PortalRegistry.getPortalsAt(accessor.getPosition(), world.provider.getDimensionId());
			
			if (portals == null) return currenttip;
			
			if (config.getConfig(WailaRegistry.WAILA_POWER_CAPACITY_KEY) && Config.powerCost > 0)
			{
				int power;
				int percentage;
				
				for (Portal portal : portals)
				{
					power = PortalRegistry.getPower(portal);
					percentage = (Config.powerCapacity > 0) ? MathHelper.clamp_int((int) ((long) power * 100 / Config.powerCapacity), 0, 100) : 100;
					
					currenttip.add(StatCollector.translateToLocalFormatted(WailaRegistry.WAILA_TOOLTIP_POWER_CAPACITY, power, Config.powerCapacity, percentage));
				}
			}
			
			if (config.getConfig(WailaRegistry.WAILA_ADDRESS_KEY))
			{
				String address;
				String[] addressComponents;
				
				for (Portal portal : portals)
				{
					currenttip.add(StatCollector.translateToLocal(WailaRegistry.WAILA_TOOLTIP_ADDRESS));
					
					address = portal.getAddress().toString();
					addressComponents = address.split(",");
					
					for (String component : addressComponents)
					{
						currenttip.add(StatCollector.translateToLocalFormatted(WailaRegistry.WAILA_TOOLTIP_ADDRESS_COMPONENT, component.trim()));
					}
				}
			}
		}
		
		return currenttip;
	}

	@Override
	public List<String> getWailaTail(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config)
	{
		return currenttip;
	}
}