package net.zarathul.simpleportals.items;

import net.minecraft.item.Item;
import net.zarathul.simpleportals.registration.Registry;


/**
 * Serves no purpose other than providing the icon for the creative tab.
 */
public class CreativeTabLogoItem extends Item
{
	public CreativeTabLogoItem()
	{
		super();
		
		setCreativeTab(null);
		setRegistryName(Registry.CREATIVE_TAB_LOGO_ITEM_NAME);
		setUnlocalizedName(Registry.CREATIVE_TAB_LOGO_ITEM_NAME);
	}

	@Override
	public boolean isFull3D()
	{
		return false;
	}
}