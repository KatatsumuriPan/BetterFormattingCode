package kpan.better_fc.config.core.properties;

import kpan.better_fc.config.core.gui.ModGuiConfig;
import kpan.better_fc.config.core.gui.ModGuiConfigEntries;
import kpan.better_fc.config.core.gui.ModGuiConfigEntries.IGuiConfigEntry;
import kpan.better_fc.config.core.gui.ModGuiConfigEntries.IntegerEntry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ConfigPropertyInt extends AbstractConfigProperty {

	public static final String TYPE = "I";

	private final int defaultValue;
	private final int minValue;
	private final int maxValue;
	private int value;
	private boolean hasSlidingControl = false;
	public ConfigPropertyInt(String name, int defaultValue, int minValue, int maxValue, String comment, int order) {
		super(name, comment, order);
		this.defaultValue = defaultValue;
		this.minValue = minValue;
		this.maxValue = maxValue;
		value = defaultValue;
	}

	public int getValue() {
		return value;
	}
	public void setValue(int value) {
		this.value = value;
		dirty = true;
	}
	public int getMinValue() {
		return minValue;
	}
	public int getMaxValue() {
		return maxValue;
	}

	@Override
	public boolean readValue(String value) {
		try {
			int i = Integer.parseInt(value);
			if (i < minValue || i > maxValue)
				return false;
			this.value = Integer.parseInt(value);
			dirty = true;
			return true;
		} catch (NumberFormatException ignore) {
			return false;
		}
	}
	@Override
	public String getAdditionalComment() {
		return "[range: " + minValue + " ~ " + maxValue + ", default: " + defaultValue + "]";
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
		try {
			int value = Integer.parseInt(str);
			return value >= minValue && value <= maxValue;
		} catch (NumberFormatException ignore) {
			return false;
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IGuiConfigEntry toEntry(ModGuiConfig screen, ModGuiConfigEntries entryList) {
		return new IntegerEntry(screen, entryList, this);
	}

	public boolean hasSlidingControl() {
		return hasSlidingControl;
	}
	public void setHasSlidingControl(boolean hasSlidingControl) {
		this.hasSlidingControl = hasSlidingControl;
	}
}
