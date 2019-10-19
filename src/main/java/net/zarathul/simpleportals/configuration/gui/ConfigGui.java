package net.zarathul.simpleportals.configuration.gui;

import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.button.CheckboxButton;
import net.minecraft.client.gui.widget.button.ImageButton;
import net.minecraft.client.gui.widget.list.AbstractOptionList;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.StringUtils;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.client.config.GuiButtonExt;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public class ConfigGui extends Screen
{
	private Screen parent;
	private ForgeConfigSpec[] configSpecs;
	private ModOptionList optionList;

	private final int PADDING = 5;

	public ConfigGui(ITextComponent title, Screen parent, ForgeConfigSpec[] configSpecs)
	{
		super(title);

		this.parent = parent;
		this.configSpecs = configSpecs;
	}

	@Override
	public void init(Minecraft mc, int width, int height)
	{
		super.init(mc, width, height);

		int titleHeight = mc.fontRenderer.getWordWrappedHeight(title.getString(), width - 2 * PADDING);
		int paddedTitleHeight = titleHeight + PADDING * 2;

		addButton(width - 120 - 2 * PADDING, 0, 60, paddedTitleHeight, "Back", button -> mc.displayGuiScreen(parent));
		addButton(width - 60 - PADDING, 0, 60, paddedTitleHeight, "Done", button -> {
			for (ForgeConfigSpec spec : configSpecs)
			{
				spec.save();
			}

			mc.displayGuiScreen(parent);
		});

		int optionListTop = titleHeight + 2 * PADDING;
		this.optionList = new ModOptionList(configSpecs, minecraft, width, height - optionListTop, optionListTop, height, 26);
		this.children.add(optionList);
	}

	private void addButton(int x, int y, int width, int height, String label, Button.IPressable pressHandler)
	{
		Button button = new GuiButtonExt(x, y, width, height, label, pressHandler);

		children.add(button);
		buttons.add(button);
	}

	@Override
	public void render(int mouseX, int mouseY, float partialTicks)
	{
		this.renderBackground();
		this.optionList.render(mouseX, mouseX, partialTicks);
		super.render(mouseX, mouseY, partialTicks);
		minecraft.fontRenderer.drawStringWithShadow(title.getFormattedText(), PADDING, PADDING, 16777215);
	}

	@Override
	public void tick()
	{
		super.tick();
		optionList.tick();
	}

	public class ModOptionList extends AbstractOptionList<ModOptionList.Entry>
	{
		private final int LEFT_RIGHT_BORDER = 30;

		public ModOptionList(ForgeConfigSpec[] configSpecs, Minecraft mc, int width, int height, int top, int bottom, int itemHeight)
		{
			super(mc, width, height, top, bottom, itemHeight);

			for (ForgeConfigSpec spec : configSpecs)
			{
				UnmodifiableConfig configValues = spec.getValues();
				generateEntries(spec, configValues, "");
			}
		}

		public void tick()
		{
			for (IGuiEventListener child : this.children())
			{
				((Entry)child).tick();
			}
		}

		@Override
		public int getRowWidth()
		{
			return width - LEFT_RIGHT_BORDER * 2;
		}

		@Override
		protected int getScrollbarPosition()
		{
			return width - LEFT_RIGHT_BORDER;
		}

		@Override
		public boolean mouseClicked(double x, double y, int button)
		{
			if (super.mouseClicked(x, y, button))
			{
				IGuiEventListener focusedChild = getFocused();

				for (IGuiEventListener child : this.children())
				{
					if (child != focusedChild) ((Entry)child).clearFocus();
				}

				return true;
			}

			return false;
		}

		private void generateEntries(UnmodifiableConfig spec, UnmodifiableConfig values, String path)
		{
			String currentPath;

			for (UnmodifiableConfig.Entry entry : spec.entrySet())
			{
				currentPath = (path.length() > 0) ? path + "." + entry.getKey() : entry.getKey();

				if (entry.getValue() instanceof com.electronwill.nightconfig.core.Config)
				{
					addEntry(new CategoryEntry(entry.getKey()));
					generateEntries(spec.get(entry.getKey()), values, currentPath);
				}
				else if (entry.getValue() instanceof ForgeConfigSpec.ValueSpec)
				{
					ForgeConfigSpec.ConfigValue<?> value = values.get(currentPath);
					ForgeConfigSpec.ValueSpec valueSpec = entry.getValue();

					addEntry(new OptionEntry(valueSpec, value.get()));
				}
			}
		}

		public abstract class Entry extends AbstractOptionList.Entry<ConfigGui.ModOptionList.Entry>
		{
			public abstract void clearFocus();
			public abstract void tick();
		}

		public class CategoryEntry extends Entry
		{
			private final String text;
			private final int width;

			public CategoryEntry(String text)
			{
				this.text = text;
				this.width = minecraft.fontRenderer.getStringWidth(text);
			}

			@Override
			public void render(int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isHot, float partialTicks)
			{
				minecraft.fontRenderer.drawStringWithShadow(this.text, minecraft.currentScreen.width / 2 - this.width / 2, top + height - 9 - 1, 16777215);
			}

			@Override
			public List<? extends IGuiEventListener> children()
			{
				return Collections.emptyList();
			}

			@Override
			public boolean changeFocus(boolean forward)
			{
				return false;
			}

			@Override
			public void clearFocus()
			{
			}

			@Override
			public void tick()
			{
			}
		}

		public class OptionEntry extends Entry
		{
			private ForgeConfigSpec.ValueSpec valueSpec;
			private Object value;
			private TextFieldWidget editBox;
			private CheckboxButton checkBox;
			private ImageButton needsWorldRestartButton;
			private ValidationStatusButton validatedButton;
			private List<IGuiEventListener> children;

			// Sets the state of the ValidationStatusButton button based on the input in the TextFieldWidget.
			private final Predicate<String> textInputValidator = text -> {
				if (StringUtils.isNullOrEmpty(text)) return true;

				if (value instanceof Integer)
				{
					try
					{
						int parsedValue = Integer.parseInt(text);
						this.validatedButton.setValid(valueSpec.test(parsedValue));
					}
					catch (NumberFormatException ex)
					{
						this.validatedButton.setInvalid();
					}
				}
				else if (value instanceof Long)
				{
					try
					{
						long parsedValue = Long.parseLong(text);
						this.validatedButton.setValid(valueSpec.test(parsedValue));
					}
					catch (NumberFormatException ex)
					{
						this.validatedButton.setInvalid();
					}
				}
				else if (value instanceof Double)
				{
					try
					{
						double parsedValue = Double.parseDouble(text);
						this.validatedButton.setValid(valueSpec.test(parsedValue));
					}
					catch (NumberFormatException ex)
					{
						this.validatedButton.setInvalid();
					}
				}
				else if (value instanceof String)
				{
					this.validatedButton.setValid(valueSpec.test(text));
				}

				// TODO: write value back to config

				return true;
			};

			public OptionEntry(ForgeConfigSpec.ValueSpec valueSpec, Object value)
			{
				this.valueSpec = valueSpec;
				this.value = value;

				this.validatedButton = new ValidationStatusButton(0, 0, button -> {
					if (this.editBox != null)
					{
						this.editBox.setText(this.valueSpec.getDefault().toString());
						this.editBox.setFocused2(false);
					}
				});

				this.needsWorldRestartButton = new ImageButton(0, 0, 20, 20, 0, 106, 20, Button.WIDGETS_LOCATION, 256, 256, (button -> {;}), "Needs world restart!");
				this.needsWorldRestartButton.active = false;
				// TODO: enable this code
				//this.needsWorldRestartButton.visible = valueSpec.needsWorldRestart();

				if (value instanceof Boolean)
				{
					this.checkBox = new CheckboxButton(0, 0, 20, 20, "", (boolean)value);

					this.children = ImmutableList.of(this.validatedButton, this.needsWorldRestartButton, this.checkBox);
				}
				else
				{
					this.editBox = new TextFieldWidget(minecraft.fontRenderer, 0, 0, 100, itemHeight - PADDING, "");
					this.editBox.setTextColor(16777215);
					this.editBox.setText(value.toString());
					this.editBox.setMaxStringLength(256);
					this.editBox.setCanLoseFocus(true);
					this.editBox.setValidator(textInputValidator);

					this.children = ImmutableList.of(this.validatedButton, this.needsWorldRestartButton, this.editBox);
				}
			}

			@Override
			public void render(int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isHot, float partialTicks)
			{
				//this.validatedButton.x = this.editBox.x + this.editBox.getWidth() + PADDING;
				this.validatedButton.x = getScrollbarPosition() - this.validatedButton.getWidth() - this.needsWorldRestartButton.getWidth() - 2 * PADDING;
				this.validatedButton.y = top + ((itemHeight - this.validatedButton.getHeight()) / 2) - 1;
				this.validatedButton.render(mouseX, mouseY, partialTicks);

				// This needs to be here because the TextFieldWidget changes the GL state and never sets it back,
				// nor does the ImageButton set the correct values to render properly. Without this call, the
				// ImageButtons are just black after the first TextFieldWidget is rendered.
				// Update: No longer needed because the ValidationStatusButton sets up the state correctly and is rendered
				// BEFORE this ImageButton. DON'T delete this comment to avoid confusion in the future.
				//GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0f);

				//this.needsWorldRestartButton.x = this.validatedButton.x + this.validatedButton.getWidth() + PADDING;
				this.needsWorldRestartButton.x = getScrollbarPosition() - this.needsWorldRestartButton.getWidth() - PADDING;
				this.needsWorldRestartButton.y = top + ((itemHeight - this.needsWorldRestartButton.getHeight()) / 2) - 1;
				this.needsWorldRestartButton.render(mouseX, mouseY, partialTicks);

				if (this.editBox != null)
				{
					this.editBox.x = left + (width / 2) + PADDING;
					this.editBox.y = top;
					this.editBox.setWidth((width / 2) - this.validatedButton.getWidth() - this.needsWorldRestartButton.getWidth() - 4 * PADDING - 6);
					this.editBox.render(mouseX, mouseY, partialTicks);
				}
				else if (this.checkBox != null)
				{
					this.checkBox.x = left + (width / 2) + PADDING;
					this.checkBox.y = top;
					this.checkBox.render(mouseX, mouseY, partialTicks);
				}

				String description = I18n.format(valueSpec.getTranslationKey());
				int descriptionWidth = minecraft.fontRenderer.getStringWidth(description);
				int descriptionLeft = left + (width / 2) - descriptionWidth - PADDING;
				int descriptionTop = top + (itemHeight / 2) - PADDING - minecraft.fontRenderer.FONT_HEIGHT / 2 + 2;
				minecraft.fontRenderer.drawStringWithShadow(description,
															descriptionLeft,
															descriptionTop,
															16777215);
			}

			@Override
			public List<? extends IGuiEventListener> children()
			{
				return this.children;
			}

			@Override
			public void clearFocus()
			{
				if (this.editBox != null)
				{
					this.editBox.setFocused2(false);
				}
			}

			@Override
			public void tick()
			{
				if (this.editBox != null)
				{
					this.editBox.tick();
				}
			}
		}
	}
}

