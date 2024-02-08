package kpan.better_fc.config.core.properties;

import kpan.better_fc.config.core.gui.ModGuiConfig;
import kpan.better_fc.config.core.gui.ModGuiConfigEntries;
import kpan.better_fc.config.core.gui.ModGuiConfigEntries.DoubleEntry;
import kpan.better_fc.config.core.gui.ModGuiConfigEntries.IGuiConfigEntry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ConfigPropertyDouble extends AbstractConfigProperty {

	public static final String TYPE = "D";

	private final double defaultValue;
	private final double minValue;
	private final double maxValue;
	private double value;
	private boolean hasSlidingControl = false;
	public ConfigPropertyDouble(String name, double defaultValue, double minValue, double maxValue, String comment, int order) {
		super(name, comment, order);
		this.defaultValue = defaultValue;
		this.minValue = minValue;
		this.maxValue = maxValue;
		value = defaultValue;
	}

	public double getValue() {
		return value;
	}
	public void setValue(double value) {
		this.value = value;
		dirty = true;
	}
	public double getMinValue() {
		return minValue;
	}
	public double getMaxValue() {
		return maxValue;
	}

	@Override
	public boolean readValue(String value) {
		try {
			double i = Double.parseDouble(value);
			if (i < minValue || i > maxValue)
				return false;
			this.value = Double.parseDouble(value);
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
			double value = Double.parseDouble(str);
			return value >= minValue && value <= maxValue;
		} catch (NumberFormatException ignore) {
			return false;
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IGuiConfigEntry toEntry(ModGuiConfig screen, ModGuiConfigEntries entryList) {
		return new DoubleEntry(screen, entryList, this);
	}

	public boolean hasSlidingControl() {
		return hasSlidingControl;
	}
	public void setHasSlidingControl(boolean hasSlidingControl) {
		this.hasSlidingControl = hasSlidingControl;
	}
}
