package kpan.better_fc.config.core;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import kpan.better_fc.config.core.gui.ModGuiConfig;
import kpan.better_fc.config.core.gui.ModGuiConfigEntries;
import kpan.better_fc.config.core.gui.ModGuiConfigEntries.CategoryEntry;
import kpan.better_fc.config.core.gui.ModGuiConfigEntries.IGuiConfigEntry;
import kpan.better_fc.config.core.properties.AbstractConfigProperty;
import kpan.better_fc.config.core.properties.ConfigPropertyBool;
import kpan.better_fc.config.core.properties.ConfigPropertyDouble;
import kpan.better_fc.config.core.properties.ConfigPropertyEnum;
import kpan.better_fc.config.core.properties.ConfigPropertyFloat;
import kpan.better_fc.config.core.properties.ConfigPropertyInt;
import kpan.better_fc.config.core.properties.ConfigPropertyLong;
import kpan.better_fc.config.core.properties.ConfigPropertyString;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ModConfigCategory implements IConfigElement {
	private static final String INDENT = "    ";
	public static final CharMatcher allowedProperties = CharMatcher.forPredicate(ModConfigCategory::isValidChar);
	private final String name;
	public final boolean isRoot;
	private final ModConfigurationFile configuration;
	private String comment = "";
	private int order = 0;
	private boolean showInGUI = true;
	private final Map<String, ModConfigCategory> children = new TreeMap<>();
	private final Map<String, AbstractConfigProperty> name2PropertyMap = new TreeMap<>();

	public ModConfigCategory(String name, boolean isRoot, ModConfigurationFile configuration) {
		this.name = name;
		this.isRoot = isRoot;
		this.configuration = configuration;
	}

	public String getName() {
		return name;
	}
	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}
	public void clear() {
		children.clear();
		name2PropertyMap.clear();
		comment = "";
	}
	public void put(String name, AbstractConfigProperty property) {
		name2PropertyMap.put(name, property);
	}
	@Nullable
	public AbstractConfigProperty get(String name) {
		return name2PropertyMap.get(name);
	}

	public List<IConfigElement> getOrderedElements() {
		List<IConfigElement> list = new ArrayList<>(children.size() + name2PropertyMap.size());
		list.addAll(children.values());
		list.addAll(name2PropertyMap.values());
		list.sort(Comparator.comparingInt(IConfigElement::getOrder));
		return list;
	}

	@Override
	public void write(BufferedWriter out, int indent) throws IOException {
		String pad = getIndent(indent);

		if (comment != null && !comment.isEmpty()) {
			writeLine(out, pad, Configuration.COMMENT_SEPARATOR);
			writeLine(out, pad, "# ", name);
			writeLine(out, pad, "#--------------------------------------------------------------------------------------------------------#");
			Splitter splitter = Splitter.onPattern("\r?\n");

			for (String line : splitter.split(comment)) {
				writeLine(out, pad, "# ", line);
			}

			writeLine(out, pad, Configuration.COMMENT_SEPARATOR);
		}

		if (!isRoot) {
			String displayName = name;
			if (!allowedProperties.matchesAllOf(name)) {
				displayName = '"' + name + '"';
			}
			writeLine(out, pad, displayName, " {");
		}

		out.newLine();
		for (IConfigElement element : getOrderedElements()) {
			element.write(out, indent + 1);
			out.write(Configuration.NEW_LINE);
		}

		if (!isRoot)
			writeLine(out, pad, "}");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IGuiConfigEntry toEntry(ModGuiConfig screen, ModGuiConfigEntries entryList) {
		return new CategoryEntry(screen, entryList, this);
	}

	public ModConfigCategory getOrCreateCategory(String name) {
		ModConfigCategory category = children.get(name);
		if (category == null) {
			category = new ModConfigCategory(name, false, configuration);
			children.put(category.name, category);
		}
		return category;
	}
	@Nullable
	public ModConfigCategory tryGetCategory(String name) {
		return children.get(name);
	}
	public void create(String name, boolean defaultValue, String comment, int order) {
		if (get(name) != null)
			throw new IllegalStateException("property named to \"" + name + "\" already exists!");
		ConfigPropertyBool property = new ConfigPropertyBool(name, defaultValue, comment, order);
		put(name, property);
	}
	public void create(String name, int defaultValue, int minValue, int maxValue, String comment, int order) {
		if (get(name) != null)
			throw new IllegalStateException("property named to \"" + name + "\" already exists!");
		ConfigPropertyInt property = new ConfigPropertyInt(name, defaultValue, minValue, maxValue, comment, order);
		put(name, property);
	}
	public void create(String name, long defaultValue, long minValue, long maxValue, String comment, int order) {
		if (get(name) != null)
			throw new IllegalStateException("property named to \"" + name + "\" already exists!");
		ConfigPropertyLong property = new ConfigPropertyLong(name, defaultValue, minValue, maxValue, comment, order);
		put(name, property);
	}
	public void create(String name, float defaultValue, float minValue, float maxValue, String comment, int order) {
		if (get(name) != null)
			throw new IllegalStateException("property named to \"" + name + "\" already exists!");
		ConfigPropertyFloat property = new ConfigPropertyFloat(name, defaultValue, minValue, maxValue, comment, order);
		put(name, property);
	}
	public void create(String name, double defaultValue, double minValue, double maxValue, String comment, int order) {
		if (get(name) != null)
			throw new IllegalStateException("property named to \"" + name + "\" already exists!");
		ConfigPropertyDouble property = new ConfigPropertyDouble(name, defaultValue, minValue, maxValue, comment, order);
		put(name, property);
	}
	public void create(String name, String defaultValue, String comment, int order) {
		if (get(name) != null)
			throw new IllegalStateException("property named to \"" + name + "\" already exists!");
		ConfigPropertyString property = new ConfigPropertyString(name, defaultValue, comment, order);
		put(name, property);
	}
	public void create(String name, Enum<?> defaultValue, String comment, int order) {
		if (get(name) != null)
			throw new IllegalStateException("property named to \"" + name + "\" already exists!");
		ConfigPropertyEnum property = new ConfigPropertyEnum(name, defaultValue, comment, order);
		put(name, property);
	}
	public boolean getBool(String name) {
		AbstractConfigProperty property = get(name);
		if (property instanceof ConfigPropertyBool p)
			return p.getValue();
		else
			throw new IllegalStateException("Bool property \"" + name + "\" is not found!");
	}
	public int getInt(String name) {
		AbstractConfigProperty property = get(name);
		if (property instanceof ConfigPropertyInt p)
			return p.getValue();
		else
			throw new IllegalStateException("Int property \"" + name + "\" is not found!");
	}
	public long getLong(String name) {
		AbstractConfigProperty property = get(name);
		if (property instanceof ConfigPropertyLong p)
			return p.getValue();
		else
			throw new IllegalStateException("Long property \"" + name + "\" is not found!");
	}
	public float getFloat(String name) {
		AbstractConfigProperty property = get(name);
		if (property instanceof ConfigPropertyFloat p)
			return p.getValue();
		else
			throw new IllegalStateException("Float property \"" + name + "\" is not found!");
	}
	public double getDouble(String name) {
		AbstractConfigProperty property = get(name);
		if (property instanceof ConfigPropertyDouble p)
			return p.getValue();
		else
			throw new IllegalStateException("Double property \"" + name + "\" is not found!");
	}
	public String getString(String name) {
		AbstractConfigProperty property = get(name);
		if (property instanceof ConfigPropertyString p)
			return p.getValue();
		else
			throw new IllegalStateException("String property \"" + name + "\" is not found!");
	}
	@SuppressWarnings("unchecked")
	public <E extends Enum<E>> E getEnum(String name) {
		AbstractConfigProperty property = get(name);
		if (property instanceof ConfigPropertyEnum p)
			return (E) p.getValue();
		else
			throw new IllegalStateException("String property \"" + name + "\" is not found!");
	}
	public void setBool(String name, boolean value) {
		AbstractConfigProperty property = get(name);
		if (property instanceof ConfigPropertyBool p)
			p.setValue(value);
		else
			throw new IllegalStateException("Bool property \"" + name + "\" is not found!");
	}
	public void setInt(String name, int value) {
		AbstractConfigProperty property = get(name);
		if (property instanceof ConfigPropertyInt p)
			p.setValue(value);
		else
			throw new IllegalStateException("Int property \"" + name + "\" is not found!");
	}
	public void setLong(String name, long value) {
		AbstractConfigProperty property = get(name);
		if (property instanceof ConfigPropertyLong p)
			p.setValue(value);
		else
			throw new IllegalStateException("Long property \"" + name + "\" is not found!");
	}
	public void setFloat(String name, float value) {
		AbstractConfigProperty property = get(name);
		if (property instanceof ConfigPropertyFloat p)
			p.setValue(value);
		else
			throw new IllegalStateException("Float property \"" + name + "\" is not found!");
	}
	public void setDouble(String name, double value) {
		AbstractConfigProperty property = get(name);
		if (property instanceof ConfigPropertyDouble p)
			p.setValue(value);
		else
			throw new IllegalStateException("Double property \"" + name + "\" is not found!");
	}
	public void setString(String name, String value) {
		AbstractConfigProperty property = get(name);
		if (property instanceof ConfigPropertyString p)
			p.setValue(value);
		else
			throw new IllegalStateException("String property \"" + name + "\" is not found!");
	}
	public void setEnum(String name, Enum<?> value) {
		AbstractConfigProperty property = get(name);
		if (property instanceof ConfigPropertyEnum p)
			p.setValue(value);
		else
			throw new IllegalStateException("String property \"" + name + "\" is not found!");
	}


	//TODO:
	public String getLanguageKey() { return getName(); }
	public boolean requiresWorldRestart() { return false; }
	public boolean requiresMcRestart() { return false; }

	@Override
	public boolean showInGui() {
		return showInGUI;
	}


	public static String getIndent(int indent) {
		return StringUtils.repeat(INDENT, Math.max(0, indent));
	}
	public static void writeLine(BufferedWriter out, String... data) throws IOException {
		for (String datum : data) {
			out.write(datum);
		}
		out.write(Configuration.NEW_LINE);
	}
	private static boolean isValidChar(char c) {
		return Character.isLetterOrDigit(c) || c == '_';
	}

	@Override
	public int getOrder() {
		return order;
	}
	public void setOrder(int order) {
		this.order = order;
	}
}
