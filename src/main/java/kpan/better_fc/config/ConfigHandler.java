package kpan.better_fc.config;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Longs;
import kpan.better_fc.ModTagsGenerated;
import kpan.better_fc.config.ConfigAnnotations.BooleanValue;
import kpan.better_fc.config.ConfigAnnotations.Comment;
import kpan.better_fc.config.ConfigAnnotations.DoubleValue;
import kpan.better_fc.config.ConfigAnnotations.EnumValue;
import kpan.better_fc.config.ConfigAnnotations.FloatValue;
import kpan.better_fc.config.ConfigAnnotations.IntValue;
import kpan.better_fc.config.ConfigAnnotations.LongValue;
import kpan.better_fc.config.ConfigAnnotations.Name;
import kpan.better_fc.config.ConfigAnnotations.StringValue;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.config.DummyConfigElement.DummyCategoryElement;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import javax.annotation.Nullable;
import java.io.File;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;

public class ConfigHandler {

	public static Configuration config;
	private static final Joiner NEW_LINE = Joiner.on('\n');

	public static void preInit(FMLPreInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(new ConfigEventHandler());
		config = new Configuration(new File(event.getModConfigurationDirectory() + "/" + ModTagsGenerated.MODID + ".cfg"), ConfigHolder.getVersion(), true);
		config.load();
		ConfigHolder.updateVersion(config);
		syncAll();
	}

	public static List<IConfigElement> getConfigElements(Configuration config) {

		List<IConfigElement> elements = Lists.newArrayList();
		Set<String> catNames = config.getCategoryNames();
		for (String catName : catNames) {
			if (catName.isEmpty())
				continue;
			ConfigCategory category = config.getCategory(catName);
			if (category.isChild())
				continue;
			DummyCategoryElement element = new DummyCategoryElement(category.getName(), category.getLanguagekey(), new ConfigElement(category).getChildElements());
			element.setRequiresMcRestart(category.requiresMcRestart());
			element.setRequiresWorldRestart(category.requiresWorldRestart());
			elements.add(element);
		}
		return elements;
	}

	public static void syncAll() {
		sync(config, ConfigHolder.class);

		config.save();
	}

	private static void sync(Configuration config, Class<?> configClass) {
		try {
			for (Field field : configClass.getFields()) {
				sync(config, null, null, field);
			}
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
	private static void sync(Configuration config, @Nullable String category, @Nullable Object instance, Field field) throws IllegalArgumentException, IllegalAccessException {
		Class<?> type = field.getType();
		String name = getName(field);
		if (name == null)
			name = field.getName();
		String nonnull_category = category != null ? category : "general";
		String comment = getComment(field);
		if (type == boolean.class) {
			boolean default_value = false;
			BooleanValue annotation = field.getAnnotation(BooleanValue.class);
			if (annotation != null) {
				default_value = annotation.defaultValue();
			}
			boolean value = config.getBoolean(name, nonnull_category, default_value, comment);
			field.set(instance, value);
		} else if (type == int.class) {
			int default_value = 0;
			int min = Integer.MIN_VALUE;
			int max = Integer.MAX_VALUE;
			IntValue annotation = field.getAnnotation(IntValue.class);
			if (annotation != null) {
				default_value = annotation.defaultValue();
				min = annotation.minValue();
				max = annotation.maxValue();
			}
			int value = config.getInt(name, nonnull_category, default_value, min, max, comment);
			field.set(instance, value);
		} else if (type == long.class) {
			long default_value = 0;
			long min = Long.MIN_VALUE;
			long max = Long.MAX_VALUE;
			LongValue annotation = field.getAnnotation(LongValue.class);
			if (annotation != null) {
				default_value = annotation.defaultValue();
				min = annotation.minValue();
				max = annotation.maxValue();
			}
			long value = getLong(config, name, nonnull_category, default_value, min, max, comment);
			field.set(instance, value);
		} else if (type == float.class) {
			float default_value = 0;
			float min = -Float.MAX_VALUE;
			float max = Float.MAX_VALUE;
			FloatValue annotation = field.getAnnotation(FloatValue.class);
			if (annotation != null) {
				default_value = annotation.defaultValue();
				min = annotation.minValue();
				max = annotation.maxValue();
			}
			float value = config.getFloat(name, nonnull_category, default_value, min, max, comment);
			field.set(instance, value);
		} else if (type == double.class) {
			double default_value = 0;
			double min = -Double.MAX_VALUE;
			double max = Double.MAX_VALUE;
			DoubleValue annotation = field.getAnnotation(DoubleValue.class);
			if (annotation != null) {
				default_value = annotation.defaultValue();
				min = annotation.minValue();
				max = annotation.maxValue();
			}
			double value = getDouble(config, name, nonnull_category, default_value, min, max, comment);
			field.set(instance, value);
		} else if (type.isPrimitive()) {
			throw new RuntimeException("Not Supported:" + type.getName());
		} else if (type.isEnum()) {
			Enum<?> default_value = (Enum<?>) type.getEnumConstants()[0];
			EnumValue annotation = field.getAnnotation(EnumValue.class);
			if (annotation != null) {
				for (Object o : type.getEnumConstants()) {
					if (((Enum<?>) o).name().equalsIgnoreCase(annotation.defaultValueString()))
						default_value = (Enum<?>) o;
				}
			}
			String value = config.getString(name, nonnull_category, default_value.toString(), comment);
			field.set(instance, value);
			throw new RuntimeException("Enum not Supported");
		} else if (type.isArray()) {
			throw new RuntimeException("Array not Supported");
		} else if (type == String.class) {
			String default_value = "";
			StringValue annotation = field.getAnnotation(StringValue.class);
			if (annotation != null) {
				default_value = annotation.defaultValue();
			}
			String value = config.getString(name, nonnull_category, default_value, comment);
			field.set(instance, value);
		} else {
			String new_category;
			if (category != null)
				new_category = category + "." + name;
			else
				new_category = name;
			config.addCustomCategoryComment(new_category, comment);
			for (Field f : field.getType().getFields()) {
				sync(config, new_category, field.get(instance), f);
			}
		}
	}
	private static String getName(Field field) {
		Name annotation = field.getAnnotation(Name.class);
		if (annotation == null)
			return field.getName();
		return annotation.value();
	}
	private static String getComment(Field field) {
		Comment annotation = field.getAnnotation(Comment.class);
		if (annotation == null)
			return "";
		return NEW_LINE.join(annotation.value());
	}

	static <T extends Enum<T>> Class<T> castEnumType(Class<?> enumType) {
		return (Class<T>) enumType;
	}

	public static long getLong(Configuration config, String name, String category, long defaultValue, long minValue, long maxValue, String comment) {
		return getLong(config, name, category, defaultValue, minValue, maxValue, comment, name);
	}
	public static long getLong(Configuration config, String name, String category, long defaultValue, long minValue, long maxValue, String comment, String langKey) {
		Property prop = config.get(category, name, Long.toString(defaultValue), name);
		prop.setLanguageKey(langKey);
		prop.setComment(comment + " [range: " + minValue + " ~ " + maxValue + ", default: " + defaultValue + "]");
		prop.setMinValue(minValue);
		prop.setMaxValue(maxValue);
		try {
			long parseLong = Long.parseLong(prop.getString());
			return Longs.constrainToRange(parseLong, minValue, maxValue);
		} catch (Exception e) {
			FMLLog.log.error("Failed to get float for {}/{}", name, category, e);
		}
		return defaultValue;
	}
	public static double getDouble(Configuration config, String name, String category, double defaultValue, double minValue, double maxValue, String comment) {
		return getDouble(config, name, category, defaultValue, minValue, maxValue, comment, name);
	}
	public static double getDouble(Configuration config, String name, String category, double defaultValue, double minValue, double maxValue, String comment, String langKey) {
		Property prop = config.get(category, name, Double.toString(defaultValue), name);
		prop.setLanguageKey(langKey);
		prop.setComment(comment + " [range: " + minValue + " ~ " + maxValue + ", default: " + defaultValue + "]");
		prop.setMinValue(minValue);
		prop.setMaxValue(maxValue);
		try {
			double parseDouble = Double.parseDouble(prop.getString());
			return Doubles.constrainToRange(parseDouble, minValue, maxValue);
		} catch (Exception e) {
			FMLLog.log.error("Failed to get float for {}/{}", name, category, e);
		}
		return defaultValue;
	}
}
