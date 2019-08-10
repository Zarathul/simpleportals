package net.zarathul.simpleportals.theoneprobe;

/*

import mcjty.theoneprobe.api.*;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;
import net.zarathul.simpleportals.SimplePortals;
import net.zarathul.simpleportals.configuration.Config;
import net.zarathul.simpleportals.registration.Portal;
import net.zarathul.simpleportals.registration.PortalRegistry;

import java.util.List;

/**
 * Provides TheOneProbe tooltip information for portals.
 */
/*
public class PortalInfoProvider implements IProbeInfoProvider
{
	// I18N keys
	private static final String PORTAL_INFO = "portalInfo.";
	private static final String POWER_CAPACITY = PORTAL_INFO + "powerCapacity";
	private static final String POWER_SOURCES = PORTAL_INFO + "powerSources";
	private static final String ADDRESS = PORTAL_INFO + "address";
	private static final String REDSTONE_POWER = PORTAL_INFO + "redstonePower";

	private static final IForgeRegistry<Block> BLOCK_REGISTRY = GameRegistry.findRegistry(Block.class);

	@Override
	public String getID()
	{
		return SimplePortals.MOD_ID + ":PortalInfoProvider";
	}

	@Override
	public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data)
	{
		List<Portal> portals = PortalRegistry.getPortalsAt(data.getPos(), world.provider.getDimension());

		if (portals == null) return;

		for (Portal portal : portals)
		{
			// Add power capacity info

			if (Config.powerCost > 0)
			{
				int power = PortalRegistry.getPower(portal);
				int percentage = (Config.powerCapacity > 0)
					? MathHelper.clamp((int) ((long) power * 100 / Config.powerCapacity), 0, 100)
					: 100;

				probeInfo.text(I18n.translateToLocalFormatted(POWER_CAPACITY, power, Config.powerCapacity, percentage));
				probeInfo.progress(power, Config.powerCapacity, probeInfo.defaultProgressStyle().showText(false));

				if (mode == ProbeMode.EXTENDED)
				{
					// Add a list of items that are considered valid power sources for the portal

					probeInfo.text(I18n.translateToLocal(POWER_SOURCES));
					IProbeInfo powerSourceInfo =  probeInfo.horizontal();

					for (ItemStack powerSource : Config.powerSources)
					{
						powerSourceInfo.item(powerSource);
					}
				}
			}

			if (mode == ProbeMode.EXTENDED)
			{
				// Add the address as block icons

				probeInfo.text(I18n.translateToLocal(ADDRESS));
				IProbeInfo addressInfo = probeInfo.horizontal();

				String address = portal.getAddress().toString();
				String[] addressComponents = address.split(",");

				for (String component : addressComponents)
				{
					// Extract the block name, meta and count from the address
					String trimmedComponent = component.trim();
					int blockCount = Integer.parseInt(trimmedComponent.substring(0, 1));
					int metaIndex = trimmedComponent.indexOf('#');
					String blockName = trimmedComponent.substring(2, metaIndex);
					int meta = Integer.parseInt(trimmedComponent.substring(metaIndex + 1));

					// Get an ItemStack corresponding to the extracted data
					ItemStack addressItem = ItemStack.EMPTY;
					Block addressBlock = BLOCK_REGISTRY.getValue(new ResourceLocation(blockName));

					if (addressBlock != null)
					{
						Item blockItem = Item.getItemFromBlock(addressBlock);

						if (blockItem != Items.AIR)
						{
							// Using Block.damageDropped() here is important. If the meta value is used directly,
							// the icons for blocks with sub-items may not render correctly. This happens for example
							// with logs, because the meta value contains orientation data that is not supported by
							// the item renderer. Logs are always rendered Y-Axis aligned in item form.
							addressItem = new ItemStack(blockItem, 1, addressBlock.damageDropped(addressBlock.getStateFromMeta(meta)));
						}
					}

					if (!addressItem.isEmpty())
					{
						// Add the icon for the ItemStack as many times as it occurs in the address
						for (int x = 0; x < blockCount; x++ ) addressInfo.item(addressItem);
					}
					else
					{
						// If no ItemStack could be found for the current address component, show the raw text instead
						addressInfo.text(component);
						addressInfo = probeInfo.horizontal();
					}
				}

				// Add redstone power level if the inspected block is a power gauge

				Block block = blockState.getBlock();

				if (block == SimplePortals.blockPowerGauge)
				{
					int gaugeLevel = SimplePortals.blockPowerGauge.getComparatorInputOverride(blockState, world, data.getPos());

					probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER))
						.item(new ItemStack(Items.REDSTONE))
						.text(I18n.translateToLocalFormatted(REDSTONE_POWER, gaugeLevel));

				}
			}
			else if (mode == ProbeMode.DEBUG)
			{
				// Add the address in plain text in debug mode

				probeInfo.text(I18n.translateToLocal(ADDRESS));

				String address = portal.getAddress().toString();
				String[] addressComponents = address.split(",");

				for (String component : addressComponents) probeInfo.vertical().text(component.trim());
			}
		}
	}
}
*/