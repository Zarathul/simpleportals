package net.zarathul.simpleportals.registration;

import java.util.Arrays;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import net.zarathul.simpleportals.SimplePortals;
import net.zarathul.simpleportals.blocks.BlockPortal;
import net.zarathul.simpleportals.blocks.BlockPortalFrame;
import net.zarathul.simpleportals.blocks.BlockPowerGauge;
import net.zarathul.simpleportals.configuration.Config;
import net.zarathul.simpleportals.configuration.Recipe;
import net.zarathul.simpleportals.items.ItemPortalActivator;
import net.zarathul.simpleportals.items.ItemPortalFrame;
import net.zarathul.simpleportals.items.ItemPowerGauge;

/**
 * Provides helper methods to register blocks, items, custom renderers etc.
 */
public final class Registry
{
	public static final String BLOCK_PORTAL_NAME = "blockPortal";

	public static final String BLOCK_PORTAL_FRAME_NAME = "blockPortalFrame";
	public static final String ITEM_PORTAL_FRAME_NAME = "itemPortalFrame";
	
	public static final String BLOCK_POWER_GAUGE_NAME = "blockPowerGauge";
	public static final String ITEM_POWER_GAUGE_NAME = "itemPowerGauge";

	public static final String ITEM_PORTAL_ACTIVATOR_NAME = "itemPortalActivator";

	private static final String ITEM_PORTAL_FRAME_MODEL_RESLOC = SimplePortals.MOD_ID + ":" + BLOCK_PORTAL_FRAME_NAME;
	private static final String ITEM_POWER_GAUGE_MODEL_RESLOC = SimplePortals.MOD_ID + ":" + BLOCK_POWER_GAUGE_NAME;
	private static final String ITEM_PORTAL_ACTIVATOR_RESLOC = SimplePortals.MOD_ID + ":" + ITEM_PORTAL_ACTIVATOR_NAME;

	/**
	 * Creates and registers all blocks added by the mod.
	 */
	public static void registerBlocks()
	{
		// BlockPortal
		SimplePortals.blockPortal = new BlockPortal();
		GameRegistry.register(SimplePortals.blockPortal);

		// BlockPortalFrame
		SimplePortals.blockPortalFrame = new BlockPortalFrame();
		GameRegistry.register(SimplePortals.blockPortalFrame);
		
		// BlockPowerGauge
		SimplePortals.blockPowerGauge = new BlockPowerGauge();
		GameRegistry.register(SimplePortals.blockPowerGauge);
	}

	/**
	 * Creates and registers all items added by the mod.
	 */
	public static void registerItems()
	{
		SimplePortals.itemPortalFrame = new ItemPortalFrame(SimplePortals.blockPortalFrame);
		GameRegistry.register(SimplePortals.itemPortalFrame);
		
		SimplePortals.itemPowerGauge = new ItemPowerGauge(SimplePortals.blockPowerGauge);
		GameRegistry.register(SimplePortals.itemPowerGauge);
		
		SimplePortals.itemPortalActivator = new ItemPortalActivator();
		GameRegistry.register(SimplePortals.itemPortalActivator);
	}
	
	/**
	 * Registers item models. Must be called after registerItems().
	 */
	public static void registerItemModels()
	{
		ModelLoader.setCustomModelResourceLocation(SimplePortals.itemPortalFrame, 0, new ModelResourceLocation(ITEM_PORTAL_FRAME_MODEL_RESLOC, "inventory"));
		ModelLoader.setCustomModelResourceLocation(SimplePortals.itemPowerGauge, 0, new ModelResourceLocation(ITEM_POWER_GAUGE_MODEL_RESLOC, "inventory"));
		ModelLoader.setCustomModelResourceLocation(SimplePortals.itemPortalActivator, 0, new ModelResourceLocation(ITEM_PORTAL_ACTIVATOR_RESLOC, "inventory"));
	}

	/**
	 * Registers with Waila, if installed.
	 */
	public static final void registerWithWaila()
	{
		FMLInterModComms.sendMessage("Waila", "register", "net.zarathul.simpleportals.waila.WailaRegistry.register");
	}

	/**
	 * Registers the mods recipes.
	 */
	public static final void registerRecipes()
	{
		ItemStack portalFrameRecipeResult = new ItemStack(SimplePortals.blockPortalFrame);
		ItemStack powerGaugeRecipeResult = new ItemStack(SimplePortals.blockPowerGauge);
		ItemStack portalActivatorRecipeResult = new ItemStack(SimplePortals.itemPortalActivator);

		registerRecipeWithAlternative(portalFrameRecipeResult, Config.portalFrameRecipe, Config.defaultPortalFrameRecipe);
		registerRecipeWithAlternative(powerGaugeRecipeResult, Config.powerGaugeRecipe, Config.defaultPowerGaugeRecipe);
		registerRecipeWithAlternative(portalActivatorRecipeResult, Config.portalActivatorRecipe, Config.defaultPortalActivatorRecipe);
	}

	/**
	 * Tries to register the recipe for the specified ItemStack. If registration fails
	 * the default recipe is used instead.
	 * 
	 * @param result
	 * The ItemStack crafted using the specified recipe.
	 * @param recipe
	 * The recipe.
	 * @param defaultRecipe
	 * The fall back recipe.
	 */
	private static final void registerRecipeWithAlternative(ItemStack result, Recipe recipe, Recipe defaultRecipe)
	{
		if (!registerRecipe(result, recipe))
		{
			SimplePortals.log.warn(String.format("Failed to register recipe for '%s'. Check your config file.", result.getItem().getRegistryName()));

			if (!registerRecipe(result, defaultRecipe))
			{
				SimplePortals.log.error(String.format("Failed to register default recipe for '%s'. This should never happen.", result.getItem().getRegistryName()));
			}
		}
	}

	/**
	 * Tries to register the recipe for the specified ItemStack.
	 * 
	 * @param result
	 * The ItemStack crafted using the specified recipe.
	 * @param recipe
	 * The recipe.
	 * @returns
	 * <code>true</code> if the registration succeeded, otherwise <code>false</code>.
	 */
	private static final boolean registerRecipe(ItemStack result, Recipe recipe)
	{
		Object[] registrationArgs;

		try
		{
			registrationArgs = recipe.getRegistrationArgs();

			if (registrationArgs != null && recipe.yield > 0)
			{
				result.stackSize = recipe.yield;

				if (recipe.isShapeless)
				{
					GameRegistry.addRecipe(new ShapelessOreRecipe(result, registrationArgs));
				}
				else
				{
					GameRegistry.addRecipe(new ShapedOreRecipe(result, false, registrationArgs));
				}

				return true;
			}
		}
		catch (Exception e)
		{
		}

		return false;
	}

	/**
	 * Adds a creative mode tab.
	 */
	@SideOnly(Side.CLIENT)
	public static final void addCreativeTab()
	{
		// Check if a a "Simple Mods" tab already exists, otherwise make one.
		SimplePortals.creativeTab = Arrays.stream(CreativeTabs.creativeTabArray)
			.filter(tab -> tab.getTabLabel().equals(SimplePortals.MOD_TAB_NAME))
			.findFirst()
			.orElseGet(() ->
				new CreativeTabs(SimplePortals.MOD_TAB_NAME)
				{
					@Override
					public String getTranslatedTabLabel()
					{
						return this.getTabLabel();
					}
					
					@Override
					public Item getTabIconItem()
					{
						return Item.getItemFromBlock(SimplePortals.blockPortalFrame);
					}
				}
			);
	}
}