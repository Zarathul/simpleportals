package net.zarathul.simpleportals;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.*;
import net.zarathul.simpleportals.registration.Registry;

public class ClientProxy extends CommonProxy {
	@Override
	public void preInit(FMLPreInitializationEvent event) {
		Registry.addCreativeTab();

		super.preInit(event);

		SimplePortals.clientEventHub = new ClientEventHub();
		MinecraftForge.EVENT_BUS.register(SimplePortals.clientEventHub);
	}

	@Override
	public void init(FMLInitializationEvent event) {
		super.init(event);

		Registry.registerWithWaila();
	}

	@Override
	public void postInit(FMLPostInitializationEvent event) {
		super.postInit(event);
	}
	
	@Override
	public void registerItemRenderer(Item item, int meta, String id) {
		super.registerItemRenderer(item, meta, id);
		ModelLoader.setCustomModelResourceLocation(item, meta,
				new ModelResourceLocation(SimplePortals.MOD_ID + ":" + id, "inventory"));
	}
	
}
