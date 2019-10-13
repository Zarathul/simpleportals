package net.zarathul.simpleportals.theoneprobe;

import mcjty.theoneprobe.api.*;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;
import net.zarathul.simpleportals.SimplePortals;
import net.zarathul.simpleportals.common.Utils;
import net.zarathul.simpleportals.configuration.Config;
import net.zarathul.simpleportals.registration.Portal;
import net.zarathul.simpleportals.registration.PortalRegistry;

import java.util.Collection;
import java.util.List;

/**
 * Provides TheOneProbe tooltip information for portals.
 */
public class PortalInfoProvider implements IProbeInfoProvider
{
	// I18N keys
	private static final String PORTAL_INFO = "interop.top.";
	private static final String POWER_CAPACITY = PORTAL_INFO + "power_capacity";
	private static final String POWER_SOURCES = PORTAL_INFO + "power_sources";
	private static final String INVALID_POWER_SOURCE = PORTAL_INFO + "invalid_power_source";
	private static final String ADDRESS = PORTAL_INFO + "address";
	private static final String REDSTONE_POWER = PORTAL_INFO + "redstone_power";

	private static final IForgeRegistry<Block> BLOCK_REGISTRY = GameRegistry.findRegistry(Block.class);

	@Override
	public String getID()
	{
		return SimplePortals.MOD_ID + ":PortalInfoProvider";
	}

	@Override
	public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, PlayerEntity player, World world, BlockState blockState, IProbeHitData data)
	{
		// Note: Text translations will only work in singleplayer. On a dedicated server everything will be english only unfortunately.

		List<Portal> portals = PortalRegistry.getPortalsAt(data.getPos(), world.getDimension().getType().getId());
		if (portals == null) return;

		for (Portal portal : portals)
		{
			// Add power capacity info

			if (Config.powerCost.get() > 0)
			{
				int power = PortalRegistry.getPower(portal);
				int percentage = (Config.powerCapacity.get() > 0)
								 ? MathHelper.clamp((int) ((long) power * 100 / Config.powerCapacity.get()), 0, 100)
								 : 100;

				probeInfo.text(Utils.translate(POWER_CAPACITY, power, Config.powerCapacity.get(), percentage));
				probeInfo.progress(power, Config.powerCapacity.get(), probeInfo.defaultProgressStyle().showText(false));

				if (mode == ProbeMode.EXTENDED)
				{
					// Add a list of items that are considered valid power sources for the portal (4 max)

					probeInfo.text(Utils.translate(POWER_SOURCES));
					IProbeInfo powerSourceInfo =  probeInfo.horizontal();
					Tag<Item> powerTag = ItemTags.getCollection().get(Config.powerSource);

					if (powerTag != null)
					{
						Collection<Item> itemsWithPowerTag = powerTag.getAllElements();
						int powerItemCount = 0;

						for (Item powerSource : itemsWithPowerTag)
						{
							powerSourceInfo.item(new ItemStack(powerSource));
							powerItemCount++;

							if (powerItemCount == 4) break;
						}
					}
					else
					{
						powerSourceInfo.text(Utils.translate(INVALID_POWER_SOURCE, Config.powerSource));
					}
				}
			}

			if (mode == ProbeMode.EXTENDED)
			{
				// Add the address as block icons

				probeInfo.text(Utils.translate(ADDRESS));
				IProbeInfo addressInfo = probeInfo.horizontal();

				String address = portal.getAddress().toString();
				String[] addressComponents = address.split(",");

				for (String component : addressComponents)
				{
					// Extract the block name and count from the address
					String trimmedComponent = component.trim();
					int blockCount = Integer.parseInt(trimmedComponent.substring(0, 1));
					String blockName = trimmedComponent.substring(2);

					// Get an ItemStack corresponding to the extracted data
					ItemStack addressItem = ItemStack.EMPTY;
					Block addressBlock = BLOCK_REGISTRY.getValue(new ResourceLocation(blockName));

					if (addressBlock != null)
					{
						Item blockItem = Item.getItemFromBlock(addressBlock);

						if (blockItem != Items.AIR)
						{
							// Note: There used to be code here to deal with sub-items based on meta data.
							// Since wool, logs and all the other blocks are all separate blocks now we don't
							// care about sub-items anymore I guess ?.
							addressItem = new ItemStack(blockItem, 1);
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
							.text(Utils.translate(REDSTONE_POWER, gaugeLevel));

				}
			}
			else if (mode == ProbeMode.DEBUG)
			{
				// Add the address in plain text in debug mode

				probeInfo.text(Utils.translate(ADDRESS));

				String address = portal.getAddress().toString();
				String[] addressComponents = address.split(",");

				for (String component : addressComponents) probeInfo.vertical().text(component.trim());
			}
		}
	}
}
