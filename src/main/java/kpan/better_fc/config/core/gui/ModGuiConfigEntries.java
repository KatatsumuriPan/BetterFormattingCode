package kpan.better_fc.config.core.gui;

import kpan.better_fc.config.core.IConfigElement;
import kpan.better_fc.config.core.ModConfigCategory;
import kpan.better_fc.config.core.properties.AbstractConfigProperty;
import kpan.better_fc.config.core.properties.ConfigPropertyBool;
import kpan.better_fc.config.core.properties.ConfigPropertyDouble;
import kpan.better_fc.config.core.properties.ConfigPropertyEnum;
import kpan.better_fc.config.core.properties.ConfigPropertyFloat;
import kpan.better_fc.config.core.properties.ConfigPropertyInt;
import kpan.better_fc.config.core.properties.ConfigPropertyLong;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.config.GuiButtonExt;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.client.config.HoverChecker;
import org.apache.commons.lang3.ArrayUtils;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static net.minecraftforge.fml.client.config.GuiUtils.RESET_CHAR;
import static net.minecraftforge.fml.client.config.GuiUtils.UNDO_CHAR;

public class ModGuiConfigEntries extends GuiListExtended {
	public final ModGuiConfig owningScreen;
	public final Minecraft mc;
	public List<IGuiConfigEntry> listEntries;
	/**
	 * The max width of the label of all IConfigEntry objects.
	 */
	public int maxLabelTextWidth = 0;
	/**
	 * The max x boundary of all IConfigEntry objects.
	 */
	public int maxEntryRightBound = 0;
	/**
	 * The x position where the label should be drawn.
	 */
	public int labelX;
	/**
	 * The x position where the control should be drawn.
	 */
	public int controlX;
	/**
	 * The width of the control.
	 */
	public int controlWidth;
	/**
	 * The minimum x position where the Undo/Default buttons will start
	 */
	public int resetX;
	/**
	 * The x position of the scroll bar.
	 */
	public int scrollBarX;

	public ModGuiConfigEntries(ModGuiConfig parent, Minecraft mc) {
		super(mc, parent.width, parent.height, parent.titleLine2 != null ? 33 : 23, parent.height - 32, 20);
		owningScreen = parent;
		setShowSelectionBox(false);
		this.mc = mc;
		listEntries = new ArrayList<>();

		for (IConfigElement element : owningScreen.configElements) {
			if (element != null) {
				if (element instanceof AbstractConfigProperty property && element.showInGui()) // as opposed to being a child category entry
				{
					int length;

					// protects against language keys that are not defined in the .lang file
					if (!I18n.format(property.getLanguageKey()).equals(property.getLanguageKey()))
						length = mc.fontRenderer.getStringWidth(I18n.format(property.getLanguageKey()));
					else
						length = mc.fontRenderer.getStringWidth(property.getName());

					if (length > maxLabelTextWidth)
						maxLabelTextWidth = length;
				}
			}
		}

		int viewWidth = maxLabelTextWidth + 8 + (width / 2);
		labelX = (width / 2) - (viewWidth / 2);
		controlX = labelX + maxLabelTextWidth + 8;
		resetX = (width / 2) + (viewWidth / 2) - 45;
		controlWidth = resetX - controlX - 5;
		scrollBarX = width;

		for (IConfigElement e : owningScreen.configElements) {
			if (e.showInGui())
				listEntries.add(e.toEntry(owningScreen, this));
		}

	}

	protected void initGui() {
		width = owningScreen.width;
		height = owningScreen.height;

		maxLabelTextWidth = 0;
		for (IGuiConfigEntry entry : listEntries) {
			if (entry.getLabelWidth() > maxLabelTextWidth)
				maxLabelTextWidth = entry.getLabelWidth();
		}

		top = owningScreen.titleLine2 != null ? 33 : 23;
		bottom = owningScreen.height - 32;
		left = 0;
		right = width;
		int viewWidth = maxLabelTextWidth + 8 + (width / 2);
		labelX = (width / 2) - (viewWidth / 2);
		controlX = labelX + maxLabelTextWidth + 8;
		resetX = (width / 2) + (viewWidth / 2) - 45;

		maxEntryRightBound = 0;
		for (IGuiConfigEntry entry : listEntries) {
			if (entry.getEntryRightBound() > maxEntryRightBound)
				maxEntryRightBound = entry.getEntryRightBound();
		}

		scrollBarX = maxEntryRightBound + 5;
		controlWidth = maxEntryRightBound - controlX - 45;
	}

	@Override
	public int getSize() {
		return listEntries.size();
	}

	/**
	 * Gets the IGuiListEntry object for the given index
	 */
	@Override
	public IGuiConfigEntry getListEntry(int index) {
		return listEntries.get(index);
	}

	@Override
	public int getScrollBarX() {
		return scrollBarX;
	}

	/**
	 * Gets the width of the list
	 */
	@Override
	public int getListWidth() {
		return owningScreen.width;
	}

	/**
	 * This method is a pass-through for IConfigEntry objects that require keystrokes. Called from the parent GuiConfig screen.
	 */
	public void keyTyped(char eventChar, int eventKey) {
		for (IGuiConfigEntry entry : listEntries) {
			entry.keyTyped(eventChar, eventKey);
		}
	}

	/**
	 * This method is a pass-through for IConfigEntry objects that contain GuiTextField elements. Called from the parent GuiConfig
	 * screen.
	 */
	public void updateScreen() {
		for (IGuiConfigEntry entry : listEntries) {
			entry.updateCursorCounter();
		}
	}

	/**
	 * This method is a pass-through for IConfigEntry objects that contain GuiTextField elements. Called from the parent GuiConfig
	 * screen.
	 */
	public void mouseClickedPassThru(int mouseX, int mouseY, int mouseEvent) {
		for (IGuiConfigEntry entry : listEntries) {
			entry.mouseClicked(mouseX, mouseY, mouseEvent);
		}
	}

	/**
	 * This method is a pass-through for IConfigEntry objects that need to perform actions when the containing GUI is closed.
	 */
	public void onGuiClosed() {
		for (IGuiConfigEntry entry : listEntries) {
			entry.onGuiClosed();
		}
	}

	/**
	 * Saves all properties on this screen / child screens. This method returns true if any elements were changed that require
	 * a restart for proper handling.
	 */
	public boolean saveConfigElements() {
		boolean requiresRestart = false;
		for (IGuiConfigEntry entry : listEntries) {
			if (entry.saveConfigElement())
				requiresRestart = true;
		}

		return requiresRestart;
	}

	/**
	 * Returns true if all IConfigEntry objects on this screen are set to default. If includeChildren is true sub-category
	 * objects are checked as well.
	 */
	public boolean areAllEntriesDefault(boolean includeChildren) {
		for (IGuiConfigEntry entry : listEntries) {
			if ((includeChildren || !(entry instanceof CategoryEntry)) && !entry.isDefault())
				return false;
		}

		return true;
	}

	/**
	 * Sets all IConfigEntry objects on this screen to default. If includeChildren is true sub-category objects are set as
	 * well.
	 */
	public void setAllToDefault(boolean includeChildren) {
		for (IGuiConfigEntry entry : listEntries) {
			if ((includeChildren || !(entry instanceof CategoryEntry)))
				entry.setToDefault();
		}
	}

	/**
	 * Returns true if any IConfigEntry objects on this screen are changed. If includeChildren is true sub-category objects
	 * are checked as well.
	 */
	public boolean hasChangedEntry(boolean includeChildren) {
		for (IGuiConfigEntry entry : listEntries) {
			if ((includeChildren || !(entry instanceof CategoryEntry)) && entry.isChanged())
				return true;
		}

		return false;
	}

	/**
	 * Returns true if any IConfigEntry objects on this screen are enabled. If includeChildren is true sub-category objects
	 * are checked as well.
	 */
	public boolean areAnyEntriesEnabled(boolean includeChildren) {
		for (IGuiConfigEntry entry : listEntries) {
			if ((includeChildren || !(entry instanceof CategoryEntry)) && entry.enabled())
				return true;
		}

		return false;
	}

	/**
	 * Reverts changes to all IConfigEntry objects on this screen. If includeChildren is true sub-category objects are
	 * reverted as well.
	 */
	public void undoAllChanges(boolean includeChildren) {
		for (IGuiConfigEntry entry : listEntries) {
			if ((includeChildren || !(entry instanceof CategoryEntry)))
				entry.undoChanges();
		}
	}

	/**
	 * Calls the drawToolTip() method for all IConfigEntry objects on this screen. This is called from the parent GuiConfig screen
	 * after drawing all other elements.
	 */
	public void drawScreenPost(int mouseX, int mouseY, float partialTicks) {
		for (IGuiConfigEntry entry : listEntries) {
			entry.drawToolTip(mouseX, mouseY);
		}
	}

	public static class BooleanEntry extends ButtonEntry {
		protected final boolean beforeValue;
		protected boolean currentValue;

		public BooleanEntry(ModGuiConfig owningScreen, ModGuiConfigEntries owningEntryList, ConfigPropertyBool property) {
			super(owningScreen, owningEntryList, property);
			beforeValue = property.getValue();
			currentValue = beforeValue;
			btnValue.enabled = enabled();
			updateValueButtonText();
		}

		@Override
		public void updateValueButtonText() {
			btnValue.displayString = I18n.format(String.valueOf(currentValue));
			btnValue.packedFGColour = currentValue ? GuiUtils.getColorCode('2', true) : GuiUtils.getColorCode('4', true);
		}

		@Override
		public void valueButtonPressed(int slotIndex) {
			if (enabled())
				currentValue = !currentValue;
		}

		@Override
		public boolean isDefault() {
			return currentValue == ((ConfigPropertyBool) property).getDefaultValue();
		}

		@Override
		public void setToDefault() {
			if (enabled()) {
				currentValue = ((ConfigPropertyBool) property).getDefaultValue();
				updateValueButtonText();
			}
		}

		@Override
		public boolean isChanged() {
			return currentValue != beforeValue;
		}

		@Override
		public void undoChanges() {
			if (enabled()) {
				currentValue = beforeValue;
				updateValueButtonText();
			}
		}

		@Override
		public boolean saveConfigElement() {
			if (enabled() && isChanged()) {
				((ConfigPropertyBool) property).setValue(currentValue);
				return property.requiresMcRestart();
			}
			return false;
		}
	}

	public static abstract class CycleValueEntry extends ButtonEntry {
		protected final String[] valueTexts;
		protected final int beforeIndex;
		protected final int defaultIndex;
		protected int currentIndex;

		private CycleValueEntry(ModGuiConfig owningScreen, ModGuiConfigEntries owningEntryList, AbstractConfigProperty property, String[] valueTexts, int currentIndex, int defaultIndex) {
			super(owningScreen, owningEntryList, property);
			this.valueTexts = valueTexts;
			beforeIndex = currentIndex;
			this.defaultIndex = defaultIndex;
			this.currentIndex = beforeIndex;
			btnValue.enabled = enabled();
			updateValueButtonText();
		}

		@Override
		public void updateValueButtonText() {
			btnValue.displayString = I18n.format(valueTexts[currentIndex]);
		}

		@Override
		public void valueButtonPressed(int slotIndex) {
			if (enabled()) {
				if (++currentIndex >= valueTexts.length)
					currentIndex = 0;

				updateValueButtonText();
			}
		}

		@Override
		public boolean isDefault() {
			return currentIndex == defaultIndex;
		}

		@Override
		public void setToDefault() {
			if (enabled()) {
				currentIndex = defaultIndex;
				updateValueButtonText();
			}
		}

		@Override
		public boolean isChanged() {
			return currentIndex != beforeIndex;
		}

		@Override
		public void undoChanges() {
			if (enabled()) {
				currentIndex = beforeIndex;
				updateValueButtonText();
			}
		}

		@Override
		public abstract boolean saveConfigElement();
	}

	public static class EnumEntry extends CycleValueEntry {
		private final Class<? extends Enum<?>> enumClass;

		@SuppressWarnings("unchecked")
		public EnumEntry(ModGuiConfig owningScreen, ModGuiConfigEntries owningEntryList, ConfigPropertyEnum property, Enum<?> current, Enum<?> defaultValue) {
			super(owningScreen, owningEntryList, property, Arrays.stream(current.getClass().getEnumConstants()).map(Enum::toString).toArray(String[]::new), ArrayUtils.indexOf(current.getClass().getEnumConstants(), current), ArrayUtils.indexOf(current.getClass().getEnumConstants(), defaultValue));
			enumClass = (Class<? extends Enum<?>>) current.getClass();
		}

		@Override
		public boolean saveConfigElement() {
			if (enabled() && isChanged()) {
				((ConfigPropertyEnum) property).setValue(enumClass.getEnumConstants()[currentIndex]);
				return property.requiresMcRestart();
			}
			return false;
		}
	}

	/*
	public static class ChatColorEntry extends CycleValueEntry {
		ChatColorEntry(MyGuiConfig owningScreen, MyGuiConfigEntries owningEntryList, IConfigElement configElement) {
			super(owningScreen, owningEntryList, configElement);
			btnValue.enabled = enabled();
			updateValueButtonText();
		}

		@Override
		public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partial) {
			btnValue.packedFGColour = GuiUtils.getColorCode(getValidValueDisplay().charAt(0), true);
			super.drawEntry(slotIndex, x, y, listWidth, slotHeight, mouseX, mouseY, isSelected, partial);
		}

		@Override
		public void updateValueButtonText() {
			btnValue.displayString = I18n.format(getValidValueDisplay()) + " - " + I18n.format("fml.configgui.sampletext");
		}
	}

	public static class SelectValueEntry extends ButtonEntry {
		protected final String beforeValue;
		protected Object currentValue;
		protected Map<Object, String> selectableValues;

		public SelectValueEntry(MyGuiConfig owningScreen, MyGuiConfigEntries owningEntryList, IConfigElement configElement, Map<Object, String> selectableValues) {
			super(owningScreen, owningEntryList, configElement);
			beforeValue = configElement.get().toString();
			currentValue = configElement.get().toString();
			this.selectableValues = selectableValues;
			updateValueButtonText();
		}

		@Override
		public void updateValueButtonText() {
			btnValue.displayString = currentValue.toString();
		}

		@Override
		public void valueButtonPressed(int slotIndex) {
			mc.displayGuiScreen(new GuiSelectString(owningScreen, configElement, slotIndex, selectableValues, currentValue, enabled()));
		}

		public void setValueFromChildScreen(Object newValue) {
			if (enabled() && currentValue != null ? !currentValue.equals(newValue) : newValue != null) {
				currentValue = newValue;
				updateValueButtonText();
			}
		}

		@Override
		public boolean isDefault() {
			if (configElement.getDefault() != null)
				return configElement.getDefault().equals(currentValue);
			else
				return currentValue == null;
		}

		@Override
		public void setToDefault() {
			if (enabled()) {
				currentValue = configElement.getDefault().toString();
				updateValueButtonText();
			}
		}

		@Override
		public boolean isChanged() {
			if (beforeValue != null)
				return !beforeValue.equals(currentValue);
			else
				return currentValue == null;
		}

		@Override
		public void undoChanges() {
			if (enabled()) {
				currentValue = beforeValue;
				updateValueButtonText();
			}
		}

		@Override
		public boolean saveConfigElement() {
			if (enabled() && isChanged()) {
				configElement.set(currentValue);
				return configElement.requiresMcRestart();
			}
			return false;
		}

		@Override
		public String getCurrentValue() {
			return currentValue.toString();
		}

		@Override
		public String[] getCurrentValues() {
			return new String[]{getCurrentValue()};
		}
	}

	public static class ArrayEntry extends ButtonEntry {
		protected final Object[] beforeValues;
		protected Object[] currentValues;

		public ArrayEntry(MyGuiConfig owningScreen, MyGuiConfigEntries owningEntryList, IConfigElement configElement) {
			super(owningScreen, owningEntryList, configElement);
			beforeValues = configElement.getList();
			currentValues = configElement.getList();
			updateValueButtonText();
		}

		@Override
		public void updateValueButtonText() {
			btnValue.displayString = "";
			for (Object o : currentValues) {
				btnValue.displayString += ", [" + o + "]";
			}

			btnValue.displayString = btnValue.displayString.replaceFirst(", ", "");
		}

		@Override
		public void valueButtonPressed(int slotIndex) {
			mc.displayGuiScreen(new GuiEditArray(owningScreen, configElement, slotIndex, currentValues, enabled()));
		}

		public void setListFromChildScreen(Object[] newList) {
			if (enabled() && !Arrays.deepEquals(currentValues, newList)) {
				currentValues = newList;
				updateValueButtonText();
			}
		}

		@Override
		public boolean isDefault() {
			return Arrays.deepEquals(configElement.getDefaults(), currentValues);
		}

		@Override
		public void setToDefault() {
			if (enabled()) {
				currentValues = configElement.getDefaults();
				updateValueButtonText();
			}
		}

		@Override
		public boolean isChanged() {
			return !Arrays.deepEquals(beforeValues, currentValues);
		}

		@Override
		public void undoChanges() {
			if (enabled()) {
				currentValues = beforeValues;
				updateValueButtonText();
			}
		}

		@Override
		public boolean saveConfigElement() {
			if (enabled() && isChanged()) {
				configElement.set(currentValues);
				return configElement.requiresMcRestart();
			}
			return false;
		}

		@Override
		public Object getCurrentValue() {
			return btnValue.displayString;
		}

		@Override
		public Object[] getCurrentValues() {
			return currentValues;
		}
	}

	public static class NumberSliderEntry extends ButtonEntry {
		protected final double beforeValue;

		public NumberSliderEntry(MyGuiConfig owningScreen, MyGuiConfigEntries owningEntryList, IConfigElement configElement) {
			super(owningScreen, owningEntryList, configElement, new GuiSlider(0, owningEntryList.controlX, 0, owningEntryList.controlWidth, 18,
					"", "", Double.valueOf(configElement.getMinValue().toString()), Double.valueOf(configElement.getMaxValue().toString()),
					Double.valueOf(configElement.get().toString()), configElement.getType() == ConfigGuiType.DOUBLE, true));

			if (configElement.getType() == ConfigGuiType.INTEGER)
				beforeValue = Integer.valueOf(configElement.get().toString());
			else
				beforeValue = Double.valueOf(configElement.get().toString());
		}

		@Override
		public void updateValueButtonText() {
			((GuiSlider) btnValue).updateSlider();
		}

		@Override
		public void valueButtonPressed(int slotIndex) { }

		@Override
		public boolean isDefault() {
			if (configElement.getType() == ConfigGuiType.INTEGER)
				return ((GuiSlider) btnValue).getValueInt() == Integer.valueOf(configElement.getDefault().toString());
			else
				return ((GuiSlider) btnValue).getValue() == Double.valueOf(configElement.getDefault().toString());
		}

		@Override
		public void setToDefault() {
			if (enabled()) {
				((GuiSlider) btnValue).setValue(Double.valueOf(configElement.getDefault().toString()));
				((GuiSlider) btnValue).updateSlider();
			}
		}

		@Override
		public boolean isChanged() {
			if (configElement.getType() == ConfigGuiType.INTEGER)
				return ((GuiSlider) btnValue).getValueInt() != (int) Math.round(beforeValue);
			else
				return ((GuiSlider) btnValue).getValue() != beforeValue;
		}

		@Override
		public void undoChanges() {
			if (enabled()) {
				((GuiSlider) btnValue).setValue(beforeValue);
				((GuiSlider) btnValue).updateSlider();
			}
		}

		@Override
		public boolean saveConfigElement() {
			if (enabled() && isChanged()) {
				if (configElement.getType() == ConfigGuiType.INTEGER)
					configElement.set(((GuiSlider) btnValue).getValueInt());
				else
					configElement.set(((GuiSlider) btnValue).getValue());
				return configElement.requiresMcRestart();
			}
			return false;
		}

		@Override
		public Object getCurrentValue() {
			if (configElement.getType() == ConfigGuiType.INTEGER)
				return ((GuiSlider) btnValue).getValueInt();
			else
				return ((GuiSlider) btnValue).getValue();
		}

		@Override
		public Object[] getCurrentValues() {
			return new Object[]{getCurrentValue()};
		}
	}

	 */

	public static abstract class ButtonEntry extends PropertyEntry {
		protected final GuiButtonExt btnValue;

		public ButtonEntry(ModGuiConfig owningScreen, ModGuiConfigEntries owningEntryList, AbstractConfigProperty property) {
			this(owningScreen, owningEntryList, property, new GuiButtonExt(0, owningEntryList.controlX, 0, owningEntryList.controlWidth, 18, I18n.format(property.getValueString())));
		}

		public ButtonEntry(ModGuiConfig owningScreen, ModGuiConfigEntries owningEntryList, AbstractConfigProperty property, GuiButtonExt button) {
			super(owningScreen, owningEntryList, property);
			btnValue = button;
		}

		/**
		 * Updates the displayString of the value button.
		 */
		public abstract void updateValueButtonText();

		/**
		 * Called when the value button has been clicked.
		 */
		public abstract void valueButtonPressed(int slotIndex);

		@Override
		public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partial) {
			super.drawEntry(slotIndex, x, y, listWidth, slotHeight, mouseX, mouseY, isSelected, partial);
			btnValue.width = owningEntryList.controlWidth;
			btnValue.x = owningEntryList.controlX;
			btnValue.y = y;
			btnValue.enabled = enabled();
			btnValue.drawButton(mc, mouseX, mouseY, partial);
		}

		/**
		 * Returns true if the mouse has been pressed on this control
		 * Called when the mouse is clicked within this entry. Returning true means that something within this entry was
		 * clicked and the list should not be dragged.
		 */
		@Override
		public boolean mousePressed(int index, int x, int y, int mouseEvent, int relativeX, int relativeY) {
			if (btnValue.mousePressed(mc, x, y)) {
				btnValue.playPressSound(mc.getSoundHandler());
				valueButtonPressed(index);
				updateValueButtonText();
				return true;
			} else
				return super.mousePressed(index, x, y, mouseEvent, relativeX, relativeY);
		}

		/**
		 * Fired when the mouse button is released. Arguments: index, x, y, mouseEvent, relativeX, relativeY
		 */
		@Override
		public void mouseReleased(int index, int x, int y, int mouseEvent, int relativeX, int relativeY) {
			super.mouseReleased(index, x, y, mouseEvent, relativeX, relativeY);
			btnValue.mouseReleased(x, y);
		}

		@Override
		public void keyTyped(char eventChar, int eventKey) { }

		@Override
		public void updateCursorCounter() { }

		@Override
		public void mouseClicked(int x, int y, int mouseEvent) { }
	}

	public static class IntegerEntry extends InputEntry {
		protected final int beforeValueInt;

		public IntegerEntry(ModGuiConfig owningScreen, ModGuiConfigEntries owningEntryList, ConfigPropertyInt property) {
			super(owningScreen, owningEntryList, property);
			beforeValueInt = property.getValue();
		}

		@Override
		public void keyTyped(char eventChar, int eventKey) {
			if (enabled() || eventKey == Keyboard.KEY_LEFT || eventKey == Keyboard.KEY_RIGHT || eventKey == Keyboard.KEY_HOME || eventKey == Keyboard.KEY_END) {
				String validChars = "0123456789";
				String before = textFieldValue.getText();
				if (validChars.contains(String.valueOf(eventChar))
						|| (!before.startsWith("-") && textFieldValue.getCursorPosition() == 0 && eventChar == '-')
						|| eventKey == Keyboard.KEY_BACK || eventKey == Keyboard.KEY_DELETE
						|| eventKey == Keyboard.KEY_LEFT || eventKey == Keyboard.KEY_RIGHT || eventKey == Keyboard.KEY_HOME || eventKey == Keyboard.KEY_END)
					textFieldValue.textboxKeyTyped((enabled() ? eventChar : Keyboard.CHAR_NONE), eventKey);
				isValidValue = property.isValidValue(textFieldValue.getText().trim());
			}
		}

		@Override
		public boolean isChanged() {
			try {
				return beforeValueInt != Integer.parseInt(textFieldValue.getText().trim());
			} catch (Throwable e) {
				return true;
			}
		}
	}

	public static class LongEntry extends InputEntry {
		protected final long beforeValueInt;

		public LongEntry(ModGuiConfig owningScreen, ModGuiConfigEntries owningEntryList, ConfigPropertyLong property) {
			super(owningScreen, owningEntryList, property);
			beforeValueInt = property.getValue();
		}

		@Override
		public void keyTyped(char eventChar, int eventKey) {
			if (enabled() || eventKey == Keyboard.KEY_LEFT || eventKey == Keyboard.KEY_RIGHT || eventKey == Keyboard.KEY_HOME || eventKey == Keyboard.KEY_END) {
				String validChars = "0123456789";
				String before = textFieldValue.getText();
				if (validChars.contains(String.valueOf(eventChar))
						|| (!before.startsWith("-") && textFieldValue.getCursorPosition() == 0 && eventChar == '-')
						|| eventKey == Keyboard.KEY_BACK || eventKey == Keyboard.KEY_DELETE
						|| eventKey == Keyboard.KEY_LEFT || eventKey == Keyboard.KEY_RIGHT || eventKey == Keyboard.KEY_HOME || eventKey == Keyboard.KEY_END)
					textFieldValue.textboxKeyTyped((enabled() ? eventChar : Keyboard.CHAR_NONE), eventKey);
				isValidValue = property.isValidValue(textFieldValue.getText().trim());
			}
		}

		@Override
		public boolean isChanged() {
			try {
				return beforeValueInt != Long.parseLong(textFieldValue.getText().trim());
			} catch (Throwable e) {
				return true;
			}
		}
	}

	public static class FloatEntry extends InputEntry {
		protected final float beforeValue;

		public FloatEntry(ModGuiConfig owningScreen, ModGuiConfigEntries owningEntryList, ConfigPropertyFloat property) {
			super(owningScreen, owningEntryList, property);
			beforeValue = property.getValue();
		}

		@Override
		public void keyTyped(char eventChar, int eventKey) {
			if (enabled() || eventKey == Keyboard.KEY_LEFT || eventKey == Keyboard.KEY_RIGHT || eventKey == Keyboard.KEY_HOME || eventKey == Keyboard.KEY_END) {
				String validChars = "0123456789";
				String before = textFieldValue.getText();
				if (validChars.contains(String.valueOf(eventChar)) ||
						(!before.startsWith("-") && textFieldValue.getCursorPosition() == 0 && eventChar == '-')
						|| (!before.contains(".") && eventChar == '.')
						|| eventKey == Keyboard.KEY_BACK || eventKey == Keyboard.KEY_DELETE || eventKey == Keyboard.KEY_LEFT || eventKey == Keyboard.KEY_RIGHT
						|| eventKey == Keyboard.KEY_HOME || eventKey == Keyboard.KEY_END)
					textFieldValue.textboxKeyTyped((enabled() ? eventChar : Keyboard.CHAR_NONE), eventKey);

				if (!textFieldValue.getText().trim().isEmpty() && !textFieldValue.getText().trim().equals("-")) {
					isValidValue = property.isValidValue(textFieldValue.getText().trim());
				} else
					isValidValue = false;
			}
		}

		@Override
		public boolean isChanged() {
			try {
				return beforeValue != Float.parseFloat(textFieldValue.getText().trim());
			} catch (Throwable e) {
				return true;
			}
		}

	}

	public static class DoubleEntry extends InputEntry {
		protected final double beforeValue;

		public DoubleEntry(ModGuiConfig owningScreen, ModGuiConfigEntries owningEntryList, ConfigPropertyDouble property) {
			super(owningScreen, owningEntryList, property);
			beforeValue = property.getValue();
		}

		@Override
		public void keyTyped(char eventChar, int eventKey) {
			if (enabled() || eventKey == Keyboard.KEY_LEFT || eventKey == Keyboard.KEY_RIGHT || eventKey == Keyboard.KEY_HOME || eventKey == Keyboard.KEY_END) {
				String validChars = "0123456789";
				String before = textFieldValue.getText();
				if (validChars.contains(String.valueOf(eventChar)) ||
						(!before.startsWith("-") && textFieldValue.getCursorPosition() == 0 && eventChar == '-')
						|| (!before.contains(".") && eventChar == '.')
						|| eventKey == Keyboard.KEY_BACK || eventKey == Keyboard.KEY_DELETE || eventKey == Keyboard.KEY_LEFT || eventKey == Keyboard.KEY_RIGHT
						|| eventKey == Keyboard.KEY_HOME || eventKey == Keyboard.KEY_END)
					textFieldValue.textboxKeyTyped((enabled() ? eventChar : Keyboard.CHAR_NONE), eventKey);

				if (!textFieldValue.getText().trim().isEmpty() && !textFieldValue.getText().trim().equals("-")) {
					isValidValue = property.isValidValue(textFieldValue.getText().trim());
				} else
					isValidValue = false;
			}
		}

		@Override
		public boolean isChanged() {
			try {
				return beforeValue != Double.parseDouble(textFieldValue.getText().trim());
			} catch (Throwable e) {
				return true;
			}
		}

	}

	public static class CharEntry extends InputEntry {

		public CharEntry(ModGuiConfig owningScreen, ModGuiConfigEntries owningEntryList, AbstractConfigProperty property) {
			super(owningScreen, owningEntryList, property);
		}

		@Override
		public boolean isChanged() {
			return beforeValue != null ? !beforeValue.equals(textFieldValue.getText()) : textFieldValue.getText().trim().isEmpty();
		}

	}

	public static class StringEntry extends InputEntry {

		public StringEntry(ModGuiConfig owningScreen, ModGuiConfigEntries owningEntryList, AbstractConfigProperty property) {
			super(owningScreen, owningEntryList, property);
		}

		@Override
		public boolean isChanged() {
			return beforeValue != null ? !beforeValue.equals(textFieldValue.getText()) : textFieldValue.getText().trim().isEmpty();
		}

	}

	public static abstract class InputEntry extends PropertyEntry {
		protected final GuiTextField textFieldValue;
		protected final String beforeValue;
		protected final boolean isBeforeValueDefault;

		public InputEntry(ModGuiConfig owningScreen, ModGuiConfigEntries owningEntryList, AbstractConfigProperty property) {
			super(owningScreen, owningEntryList, property);
			beforeValue = property.getValueString();
			isBeforeValueDefault = property.isDefault();
			textFieldValue = new GuiTextField(10, mc.fontRenderer, owningEntryList.controlX + 1, 0, owningEntryList.controlWidth - 3, 16);
			textFieldValue.setMaxStringLength(10000);
			textFieldValue.setText(property.getValueString());
		}

		@Override
		public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partial) {
			super.drawEntry(slotIndex, x, y, listWidth, slotHeight, mouseX, mouseY, isSelected, partial);
			textFieldValue.x = owningEntryList.controlX + 2;
			textFieldValue.y = y + 1;
			textFieldValue.width = owningEntryList.controlWidth - 4;
			textFieldValue.setEnabled(enabled());
			textFieldValue.drawTextBox();
		}

		@Override
		public void keyTyped(char eventChar, int eventKey) {
			if (enabled() || eventKey == Keyboard.KEY_LEFT || eventKey == Keyboard.KEY_RIGHT || eventKey == Keyboard.KEY_HOME || eventKey == Keyboard.KEY_END) {
				textFieldValue.textboxKeyTyped((enabled() ? eventChar : Keyboard.CHAR_NONE), eventKey);
				isValidValue = property.isValidValue(textFieldValue.getText().trim());
			}
		}

		@Override
		public void updateCursorCounter() {
			textFieldValue.updateCursorCounter();
		}

		@Override
		public void mouseClicked(int x, int y, int mouseEvent) {
			textFieldValue.mouseClicked(x, y, mouseEvent);
		}

		@Override
		public boolean isDefault() {
			return property.isDefault();
		}

		@Override
		public void setToDefault() {
			if (enabled()) {
				textFieldValue.setText(property.getDefaultValueString());
				keyTyped((char) Keyboard.CHAR_NONE, Keyboard.KEY_HOME);
			}
		}

		@Override
		public void undoChanges() {
			if (enabled())
				textFieldValue.setText(beforeValue);
		}

		@Override
		public boolean saveConfigElement() {
			if (enabled() && isChanged()) {
				if (property.readValue(textFieldValue.getText())) {
					return property.requiresMcRestart();
				} else {
					property.setToDefault();
					return property.requiresMcRestart() && isBeforeValueDefault;
				}
			}
			return false;
		}

	}

	public static class CategoryEntry extends ListEntryBase {
		protected GuiScreen childScreen;
		protected final ModConfigCategory category;
		protected final GuiButtonExt btnSelectCategory;

		public CategoryEntry(ModGuiConfig owningScreen, ModGuiConfigEntries owningEntryList, ModConfigCategory category) {
			super(owningScreen, owningEntryList, getTranslatedName(category));
			this.category = category;

			childScreen = buildChildScreen();

			btnSelectCategory = new GuiButtonExt(0, 0, 0, 300, 18, I18n.format(name));
			tooltipHoverChecker = new HoverChecker(btnSelectCategory, 800);

			drawLabel = false;

			String comment = I18n.format(category.getLanguageKey() + ".tooltip").replace("\\n", "\n");
			if (!comment.equals(category.getLanguageKey() + ".tooltip"))
				Collections.addAll(toolTip, (TextFormatting.GREEN + name + "\n" + TextFormatting.YELLOW + comment).split("\n"));
			else if (category.getComment() != null && !category.getComment().trim().isEmpty())
				Collections.addAll(toolTip, (TextFormatting.GREEN + name + "\n" + TextFormatting.YELLOW + category.getComment()).split("\n"));
			else
				Collections.addAll(toolTip, (TextFormatting.GREEN + name + "\n" + TextFormatting.RED + "No tooltip defined.").split("\n"));

			if (category.requiresWorldRestart() || owningScreen.allRequireMcRestart)
				toolTip.add(TextFormatting.RED + "[" + I18n.format("fml.configgui.gameRestartTitle") + "]");
		}

		/**
		 * This method is called in the constructor and is used to set the childScreen field.
		 */
		protected GuiScreen buildChildScreen() {
			return new ModGuiConfig(owningScreen, category.getOrderedElements(), owningScreen.configID,
					owningScreen.allRequireWorldRestart || category.requiresWorldRestart(),
					owningScreen.allRequireMcRestart || category.requiresMcRestart(), owningScreen.title,
					((owningScreen.titleLine2 == null ? "" : owningScreen.titleLine2) + " > " + name));
		}

		@Override
		public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partial) {
			btnSelectCategory.x = listWidth / 2 - 150;
			btnSelectCategory.y = y;
			btnSelectCategory.enabled = enabled();
			btnSelectCategory.drawButton(mc, mouseX, mouseY, partial);

			super.drawEntry(slotIndex, x, y, listWidth, slotHeight, mouseX, mouseY, isSelected, partial);
		}

		@Override
		public void drawToolTip(int mouseX, int mouseY) {
			boolean canHover = mouseY < owningEntryList.bottom && mouseY > owningEntryList.top;

			if (tooltipHoverChecker.checkHover(mouseX, mouseY, canHover))
				owningScreen.drawToolTip(toolTip, mouseX, mouseY);

			super.drawToolTip(mouseX, mouseY);
		}

		/**
		 * Called when the mouse is clicked within this entry. Returning true means that something within this entry was
		 * clicked and the list should not be dragged.
		 */
		@Override
		public boolean mousePressed(int index, int x, int y, int mouseEvent, int relativeX, int relativeY) {
			if (btnSelectCategory.mousePressed(mc, x, y)) {
				btnSelectCategory.playPressSound(mc.getSoundHandler());
				Minecraft.getMinecraft().displayGuiScreen(childScreen);
				return true;
			} else
				return super.mousePressed(index, x, y, mouseEvent, relativeX, relativeY);
		}

		/**
		 * Fired when the mouse button is released. Arguments: index, x, y, mouseEvent, relativeX, relativeY
		 */
		@Override
		public void mouseReleased(int index, int x, int y, int mouseEvent, int relativeX, int relativeY) {
			btnSelectCategory.mouseReleased(x, y);
		}

		@Override
		public boolean isDefault() {
			if (childScreen instanceof ModGuiConfig && ((ModGuiConfig) childScreen).entryList != null)
				return ((ModGuiConfig) childScreen).entryList.areAllEntriesDefault(true);

			return true;
		}

		@Override
		public void setToDefault() {
			if (childScreen instanceof ModGuiConfig && ((ModGuiConfig) childScreen).entryList != null)
				((ModGuiConfig) childScreen).entryList.setAllToDefault(true);
		}

		@Override
		public void keyTyped(char eventChar, int eventKey) { }

		@Override
		public void updateCursorCounter() { }

		@Override
		public void mouseClicked(int x, int y, int mouseEvent) { }

		@Override
		public boolean saveConfigElement() {
			boolean requiresRestart = false;

			if (childScreen instanceof ModGuiConfig && ((ModGuiConfig) childScreen).entryList != null) {
				requiresRestart = category.requiresMcRestart() && ((ModGuiConfig) childScreen).entryList.hasChangedEntry(true);

				if (((ModGuiConfig) childScreen).entryList.saveConfigElements())
					requiresRestart = true;
			}

			return requiresRestart;
		}

		@Override
		public boolean isChanged() {
			if (childScreen instanceof ModGuiConfig && ((ModGuiConfig) childScreen).entryList != null)
				return ((ModGuiConfig) childScreen).entryList.hasChangedEntry(true);
			else
				return false;
		}

		@Override
		public void undoChanges() {
			if (childScreen instanceof ModGuiConfig && ((ModGuiConfig) childScreen).entryList != null)
				((ModGuiConfig) childScreen).entryList.undoAllChanges(true);
		}

		@Override
		public boolean enabled() {
			return true;
		}

		@Override
		public int getLabelWidth() {
			return 0;
		}

		@Override
		public int getEntryRightBound() {
			return owningEntryList.width / 2 + 155 + 22 + 18;
		}

		@Override
		public boolean requiresWorldRestart() {
			return category.requiresWorldRestart();
		}
		@Override
		public boolean requiresMcRestart() {
			return category.requiresMcRestart();
		}
		@Override
		public String getName() {
			return category.getName();
		}

		private static String getTranslatedName(ModConfigCategory category) {
			String trans = I18n.format(category.getLanguageKey());
			if (!trans.equals(category.getLanguageKey()))
				return trans;
			else
				return category.getName();
		}
	}

	public static abstract class PropertyEntry extends ListEntryBase {
		protected final AbstractConfigProperty property;

		public PropertyEntry(ModGuiConfig owningScreen, ModGuiConfigEntries owningEntryList, AbstractConfigProperty property) {
			super(owningScreen, owningEntryList, getTranslatedName(property));
			this.property = property;

			String comment = I18n.format(property.getLanguageKey() + ".tooltip").replace("\\n", "\n");
			if (!comment.equals(property.getLanguageKey() + ".tooltip"))
				Collections.addAll(toolTip, (TextFormatting.GREEN + name + "\n" + TextFormatting.YELLOW + comment).split("\n"));
			else if (property.getComment() != null && !property.getComment().trim().isEmpty())
				Collections.addAll(toolTip, (TextFormatting.GREEN + name + "\n" + TextFormatting.YELLOW + property.getComment()).split("\n"));
			else
				Collections.addAll(toolTip, (TextFormatting.GREEN + name + "\n" + TextFormatting.RED + "No tooltip defined.").split("\n"));

			//TODO:range

			if (property.requiresWorldRestart() || owningScreen.allRequireMcRestart)
				toolTip.add(TextFormatting.RED + "[" + I18n.format("fml.configgui.gameRestartTitle") + "]");
		}

		@Override
		public String getName() {
			return property.getName();
		}

		@Override
		public boolean enabled() {
			return owningScreen.isWorldRunning ? !owningScreen.allRequireWorldRestart && !property.requiresWorldRestart() : true;
		}

		private static String getTranslatedName(AbstractConfigProperty property) {
			String trans = I18n.format(property.getLanguageKey());
			if (!trans.equals(property.getLanguageKey()))
				return trans;
			else
				return property.getName();
		}

		@Override
		public boolean requiresMcRestart() {
			return property.requiresMcRestart();
		}
		@Override
		public boolean requiresWorldRestart() {
			return property.requiresWorldRestart();
		}
	}

	public static abstract class ListEntryBase implements IGuiConfigEntry {
		protected final ModGuiConfig owningScreen;
		protected final ModGuiConfigEntries owningEntryList;
		protected final Minecraft mc;
		protected final String name;
		protected final GuiButtonExt btnUndoChanges;
		protected final GuiButtonExt btnDefault;
		protected List<String> toolTip;
		protected List<String> undoToolTip;
		protected List<String> defaultToolTip;
		protected boolean isValidValue = true;
		protected HoverChecker tooltipHoverChecker;
		protected HoverChecker undoHoverChecker;
		protected HoverChecker defaultHoverChecker;
		protected boolean drawLabel;

		public ListEntryBase(ModGuiConfig owningScreen, ModGuiConfigEntries owningEntryList, String name) {
			this.owningScreen = owningScreen;
			this.owningEntryList = owningEntryList;
			mc = Minecraft.getMinecraft();
			this.name = name;
			btnUndoChanges = new GuiButtonExt(0, 0, 0, 18, 18, UNDO_CHAR);
			btnDefault = new GuiButtonExt(0, 0, 0, 18, 18, RESET_CHAR);

			undoHoverChecker = new HoverChecker(btnUndoChanges, 800);
			defaultHoverChecker = new HoverChecker(btnDefault, 800);
			undoToolTip = Arrays.asList(I18n.format("fml.configgui.tooltip.undoChanges"));
			defaultToolTip = Arrays.asList(I18n.format("fml.configgui.tooltip.resetToDefault"));
			toolTip = new ArrayList<>();

			drawLabel = true;

		}

		@Override
		public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partial) {
			boolean isChanged = isChanged();

			if (drawLabel) {
				String label = (!isValidValue ? TextFormatting.RED.toString() :
						(isChanged ? TextFormatting.WHITE.toString() : TextFormatting.GRAY.toString()))
						+ (isChanged ? TextFormatting.ITALIC.toString() : "") + name;
				mc.fontRenderer.drawString(
						label,
						owningEntryList.labelX,
						y + slotHeight / 2 - mc.fontRenderer.FONT_HEIGHT / 2,
						16777215);
			}

			btnUndoChanges.x = owningEntryList.scrollBarX - 44;
			btnUndoChanges.y = y;
			btnUndoChanges.enabled = enabled() && isChanged;
			btnUndoChanges.drawButton(mc, mouseX, mouseY, partial);

			btnDefault.x = owningEntryList.scrollBarX - 22;
			btnDefault.y = y;
			btnDefault.enabled = enabled() && !isDefault();
			btnDefault.drawButton(mc, mouseX, mouseY, partial);

			if (tooltipHoverChecker == null)
				tooltipHoverChecker = new HoverChecker(y, y + slotHeight, x, owningEntryList.controlX - 8, 800);
			else
				tooltipHoverChecker.updateBounds(y, y + slotHeight, x, owningEntryList.controlX - 8);
		}

		@Override
		public void drawToolTip(int mouseX, int mouseY) {
			boolean canHover = mouseY < owningEntryList.bottom && mouseY > owningEntryList.top;
			if (toolTip != null && tooltipHoverChecker != null) {
				if (tooltipHoverChecker.checkHover(mouseX, mouseY, canHover))
					owningScreen.drawToolTip(toolTip, mouseX, mouseY);
			}

			if (undoHoverChecker.checkHover(mouseX, mouseY, canHover))
				owningScreen.drawToolTip(undoToolTip, mouseX, mouseY);

			if (defaultHoverChecker.checkHover(mouseX, mouseY, canHover))
				owningScreen.drawToolTip(defaultToolTip, mouseX, mouseY);
		}

		/**
		 * Called when the mouse is clicked within this entry. Returning true means that something within this entry was
		 * clicked and the list should not be dragged.
		 */
		@Override
		public boolean mousePressed(int index, int x, int y, int mouseEvent, int relativeX, int relativeY) {
			if (btnDefault.mousePressed(mc, x, y)) {
				btnDefault.playPressSound(mc.getSoundHandler());
				setToDefault();
				return true;
			} else if (btnUndoChanges.mousePressed(mc, x, y)) {
				btnUndoChanges.playPressSound(mc.getSoundHandler());
				undoChanges();
				return true;
			}
			return false;
		}

		/**
		 * Fired when the mouse button is released. Arguments: index, x, y, mouseEvent, relativeX, relativeY
		 */
		@Override
		public void mouseReleased(int index, int x, int y, int mouseEvent, int relativeX, int relativeY) {
			btnDefault.mouseReleased(x, y);
		}

		@Override
		public void updatePosition(int p_178011_1_, int p_178011_2_, int p_178011_3_, float partial) { }

		@Override
		public int getLabelWidth() {
			return mc.fontRenderer.getStringWidth(name);
		}

		@Override
		public int getEntryRightBound() {
			return owningEntryList.resetX + 40;
		}

		@Override
		public void onGuiClosed() { }

	}

	public interface IGuiConfigEntry extends IGuiListEntry {

		boolean requiresWorldRestart();
		boolean requiresMcRestart();

		/**
		 * Gets the name of the ConfigElement owned by this entry.
		 */
		String getName();

		/**
		 * Is this list entry enabled?
		 *
		 * @return true if this entry's controls should be enabled, false otherwise.
		 */
		boolean enabled();

		/**
		 * Handles user keystrokes for any GuiTextField objects in this entry. Call {@link GuiTextField#textboxKeyTyped(char, int)} for any GuiTextField
		 * objects that should receive the input provided.
		 */
		void keyTyped(char eventChar, int eventKey);

		/**
		 * Call {@link GuiTextField#updateCursorCounter()} for any GuiTextField objects in this entry.
		 */
		void updateCursorCounter();

		/**
		 * Call {@link GuiTextField#mouseClicked(int, int, int)} for and GuiTextField objects in this entry.
		 */
		void mouseClicked(int x, int y, int mouseEvent);

		/**
		 * Is this entry's value equal to the default value? Generally true should be returned if this entry is not a property or category
		 * entry.
		 *
		 * @return true if this entry's value is equal to this entry's default value.
		 */
		boolean isDefault();

		/**
		 * Sets this entry's value to the default value.
		 */
		void setToDefault();

		/**
		 * Handles reverting any changes that have occurred to this entry.
		 */
		void undoChanges();

		/**
		 * Has the value of this entry changed?
		 *
		 * @return true if changes have been made to this entry's value, false otherwise.
		 */
		boolean isChanged();

		/**
		 * Handles saving any changes that have been made to this entry back to the underlying object. It is a good practice to check
		 * isChanged() before performing the save action. This method should return true if the element has changed AND REQUIRES A RESTART.
		 */
		boolean saveConfigElement();

		/**
		 * Handles drawing any tooltips that apply to this entry. This method is called after all other GUI elements have been drawn to the
		 * screen, so it could also be used to draw any GUI element that needs to be drawn after all entries have had drawEntry() called.
		 */
		void drawToolTip(int mouseX, int mouseY);

		/**
		 * Gets this entry's label width.
		 */
		int getLabelWidth();

		/**
		 * Gets this entry's right-hand x boundary. This value is used to control where the scroll bar is placed.
		 */
		int getEntryRightBound();

		/**
		 * This method is called when the parent GUI is closed. Most handlers won't need this; it is provided for special cases.
		 */
		void onGuiClosed();
	}
}
