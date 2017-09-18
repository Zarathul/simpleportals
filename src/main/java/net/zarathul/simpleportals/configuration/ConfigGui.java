package net.zarathul.simpleportals.configuration;

import java.util.List;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.zarathul.simpleportals.SimplePortals;

/**
 * The in-game config UI.
 */
public class ConfigGui extends GuiConfig {
	public ConfigGui(GuiScreen parentScreen) {
		super(parentScreen, getConfigElements(), SimplePortals.MOD_ID, false, false,
				GuiConfig.getAbridgedConfigPath(Config.getConfig().getConfigFile().getPath()),
				SimplePortals.MOD_READABLE_NAME);
	}

	private static List<IConfigElement> getConfigElements() {
		return new ConfigElement(Config.getConfig().getCategory(Configuration.CATEGORY_GENERAL)).getChildElements();
	}
}
