package kpan.better_fc.config.core.properties;

import kpan.better_fc.config.core.gui.ModGuiConfig;
import kpan.better_fc.config.core.gui.ModGuiConfigEntries;
import kpan.better_fc.config.core.gui.ModGuiConfigEntries.CharEntry;
import kpan.better_fc.config.core.gui.ModGuiConfigEntries.IGuiConfigEntry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ConfigPropertyChar extends AbstractConfigProperty {

	public static final String TYPE = "C";

	private final char defaultValue;
	private char value;
	private boolean hasSlidingControl = false;
	public ConfigPropertyChar(String name, char defaultValue, String comment, int order) {
		super(name, comment, order);
		this.defaultValue = defaultValue;
		value = defaultValue;
	}

	public char getValue() {
		return value;
	}
	public void setValue(char value) {
		this.value = value;
		dirty = true;
	}

	@Override
	public boolean readValue(String value) {
		if (value.length() != 1)
			return false;
		this.value = value.charAt(0);
		dirty = true;
		return true;
	}
	@Override
	public String getAdditionalComment() {
		return "";
	}
	@Override
	public String getTypeString() { return TYPE; }
	@Override
	public String getValueString() {
		return value + "";
	}
	@Override
	public String getDefaultValueString() {
		return defaultValue + "";
	}
	@Override
	public boolean isDefault() {
		return value == defaultValue;
	}
	@Override
	public void setToDefault() {
		value = defaultValue;
	}
	@Override
	public boolean isValidValue(String str) {
		return str.length() == 1;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IGuiConfigEntry toEntry(ModGuiConfig screen, ModGuiConfigEntries entryList) {
		return new CharEntry(screen, entryList, this);
	}

}
