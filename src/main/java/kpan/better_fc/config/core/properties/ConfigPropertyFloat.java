package kpan.better_fc.config.core.properties;

import kpan.better_fc.config.core.gui.ModGuiConfig;
import kpan.better_fc.config.core.gui.ModGuiConfigEntries;
import kpan.better_fc.config.core.gui.ModGuiConfigEntries.FloatEntry;
import kpan.better_fc.config.core.gui.ModGuiConfigEntries.IGuiConfigEntry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ConfigPropertyFloat extends AbstractConfigProperty {

	public static final String TYPE = "F";

	private final float defaultValue;
	private final float minValue;
	private final float maxValue;
	private float value;
	private boolean hasSlidingControl = false;
	public ConfigPropertyFloat(String name, float defaultValue, float minValue, float maxValue, String comment, int order) {
		super(name, comment, order);
		this.defaultValue = defaultValue;
		this.minValue = minValue;
		this.maxValue = maxValue;
		value = defaultValue;
	}

	public float getValue() {
		return value;
	}
	public void setValue(float value) {
		this.value = value;
		dirty = true;
	}
	public float getMinValue() {
		return minValue;
	}
	public float getMaxValue() {
		return maxValue;
	}

	@Override
	public boolean readValue(String value) {
		try {
			float i = Float.parseFloat(value);
			if (i < minValue || i > maxValue)
				return false;
			this.value = Float.parseFloat(value);
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
			float value = Float.parseFloat(str);
			return value >= minValue && value <= maxValue;
		} catch (NumberFormatException ignore) {
			return false;
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IGuiConfigEntry toEntry(ModGuiConfig screen, ModGuiConfigEntries entryList) {
		return new FloatEntry(screen, entryList, this);
	}

	public boolean hasSlidingControl() {
		return hasSlidingControl;
	}
	public void setHasSlidingControl(boolean hasSlidingControl) {
		this.hasSlidingControl = hasSlidingControl;
	}
}
