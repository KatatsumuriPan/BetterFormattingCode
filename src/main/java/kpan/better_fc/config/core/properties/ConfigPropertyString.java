package kpan.better_fc.config.core.properties;

import kpan.better_fc.config.core.gui.ModGuiConfig;
import kpan.better_fc.config.core.gui.ModGuiConfigEntries;
import kpan.better_fc.config.core.gui.ModGuiConfigEntries.IGuiConfigEntry;
import kpan.better_fc.config.core.gui.ModGuiConfigEntries.StringEntry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ConfigPropertyString extends AbstractConfigProperty {

	public static final String TYPE = "S";

	private final String defaultValue;
	private String value;
	public ConfigPropertyString(String name, String defaultValue, String comment, int order) {
		super(name, comment, order);
		this.defaultValue = defaultValue;
		value = defaultValue;
	}

	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
		dirty = true;
	}
	public String getDefaultValue() {
		return defaultValue;
	}
	@Override
	public boolean readValue(String value) {
		this.value = value;
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
		return value;
	}
	@Override
	public String getDefaultValueString() {
		return defaultValue;
	}
	@Override
	public boolean isDefault() {
		return value.equals(defaultValue);
	}
	@Override
	public void setToDefault() {
		value = defaultValue;
	}
	@Override
	public boolean isValidValue(String str) {
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IGuiConfigEntry toEntry(ModGuiConfig screen, ModGuiConfigEntries entryList) {
		return new StringEntry(screen, entryList, this);
	}
}
