package kpan.better_fc.config.core;

import com.google.common.base.Joiner;
import kpan.better_fc.config.core.ConfigAnnotations.Comment;
import kpan.better_fc.config.core.ConfigAnnotations.ConfigOrder;
import kpan.better_fc.config.core.ConfigAnnotations.Name;
import kpan.better_fc.config.core.ConfigAnnotations.RangeDouble;
import kpan.better_fc.config.core.ConfigAnnotations.RangeFloat;
import kpan.better_fc.config.core.ConfigAnnotations.RangeInt;
import kpan.better_fc.config.core.ConfigAnnotations.RangeLong;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import javax.annotation.Nullable;
import java.io.File;
import java.lang.reflect.Field;
import java.util.function.Consumer;

public class ConfigHandler {
	private static final Joiner NEW_LINE = Joiner.on('\n');

	public final Class<?> holderClass;
	public final String name;
	public final String version;
	public final Consumer<ConfigVersionUpdateContext> updater;
	public ModConfigurationFile config;
	public ConfigHandler(Class<?> holderClass, String name, String version, Consumer<ConfigVersionUpdateContext> updater) {
		this.holderClass = holderClass;
		this.name = name;
		this.version = version;
		this.updater = updater;
	}

	public void preInit(FMLPreInitializationEvent event) {
		config = new ModConfigurationFile(new File(event.getModConfigurationDirectory() + "/" + name + ".cfg"), version);
		createProperties();
		config.load(updater);
		syncToFieldAndSave();
	}
	public ModConfigCategory getRootCategory() {
		return config.getRootCategory();
	}
	public void syncToFieldAndSave() {
		try {
			for (Field field : holderClass.getFields()) {
				elementToField(config, "", null, field);
			}
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
		save();
	}
	public void syncToConfigElementAndSave() {
		try {
			for (Field field : holderClass.getFields()) {
				fieldToElement(config, "", null, field);
			}
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
		save();
	}
	private void save() {
		config.save();
	}

	private void createProperties() {
		try {
			for (Field field : holderClass.getFields()) {
				create(config, "", null, field);
			}
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
	private static void create(ModConfigurationFile config, String categoryPath, @Nullable Object instance, Field field) throws IllegalArgumentException, IllegalAccessException {
		Class<?> type = field.getType();
		String name = getName(field);
		String comment = getComment(field);
		if (type == boolean.class) {
			boolean default_value = field.getBoolean(instance);
			config.createBool(name, categoryPath, default_value, comment, getOrder(field));
		} else if (type == int.class) {
			int default_value = field.getInt(instance);
			int min = Integer.MIN_VALUE;
			int max = Integer.MAX_VALUE;
			RangeInt annotation = field.getAnnotation(RangeInt.class);
			if (annotation != null) {
				min = annotation.minValue();
				max = annotation.maxValue();
			}
			config.createInt(name, categoryPath, default_value, min, max, comment, getOrder(field));
		} else if (type == long.class) {
			long default_value = field.getLong(instance);
			long min = Long.MIN_VALUE;
			long max = Long.MAX_VALUE;
			RangeLong annotation = field.getAnnotation(RangeLong.class);
			if (annotation != null) {
				min = annotation.minValue();
				max = annotation.maxValue();
			}
			config.createLong(name, categoryPath, default_value, min, max, comment, getOrder(field));
		} else if (type == float.class) {
			float default_value = field.getFloat(instance);
			float min = -Float.MAX_VALUE;
			float max = Float.MAX_VALUE;
			RangeFloat annotation = field.getAnnotation(RangeFloat.class);
			if (annotation != null) {
				min = annotation.minValue();
				max = annotation.maxValue();
			}
			config.createFloat(name, categoryPath, default_value, min, max, comment, getOrder(field));
		} else if (type == double.class) {
			double default_value = field.getDouble(instance);
			double min = -Double.MAX_VALUE;
			double max = Double.MAX_VALUE;
			RangeDouble annotation = field.getAnnotation(RangeDouble.class);
			if (annotation != null) {
				min = annotation.minValue();
				max = annotation.maxValue();
			}
			config.createDouble(name, categoryPath, default_value, min, max, comment, getOrder(field));
		} else if (type.isPrimitive()) {
			throw new RuntimeException("Not Supported:" + type.getName());
		} else if (type.isEnum()) {
			Enum<?> default_value = (Enum<?>) field.get(instance);
			config.createEnum(name, categoryPath, default_value, comment, getOrder(field));
		} else if (type.isArray()) {
			throw new RuntimeException("Array not Supported");
		} else if (type == String.class) {
			String default_value = (String) field.get(instance);
			config.createString(name, categoryPath, default_value, comment, getOrder(field));
		} else {
			String new_category_path;
			if (categoryPath.isEmpty()) {
				new_category_path = name;
			} else {
				new_category_path = categoryPath + "." + name;
			}
			ModConfigCategory new_category = config.getOrCreateCategory(new_category_path);
			new_category.setComment(comment);
			new_category.setOrder(getOrder(field));
			for (Field f : field.getType().getFields()) {
				create(config, new_category_path, field.get(instance), f);
			}
		}
	}
	private static void elementToField(ModConfigurationFile config, String category, @Nullable Object instance, Field field) throws IllegalArgumentException, IllegalAccessException {
		Class<?> type = field.getType();
		String name = getName(field);
		if (type == boolean.class) {
			field.setBoolean(instance, config.getBool(name, category));
		} else if (type == int.class) {
			field.setInt(instance, config.getInt(name, category));
		} else if (type == long.class) {
			field.setLong(instance, config.getLong(name, category));
		} else if (type == float.class) {
			field.setFloat(instance, config.getFloat(name, category));
		} else if (type == double.class) {
			field.setDouble(instance, config.getDouble(name, category));
		} else if (type.isPrimitive()) {
			throw new RuntimeException("Not Supported:" + type.getName());
		} else if (type.isEnum()) {
			field.set(instance, config.getEnum(name, category));
		} else if (type.isArray()) {
			throw new RuntimeException("Array not Supported");
		} else if (type == String.class) {
			field.set(instance, config.getString(name, category));
		} else {
			String category_path;
			if (category.isEmpty()) {
				category_path = name;
			} else {
				category_path = category + "." + name;
			}
			for (Field f : field.getType().getFields()) {
				elementToField(config, category_path, field.get(instance), f);
			}
		}
	}
	private static void fieldToElement(ModConfigurationFile config, String category, @Nullable Object instance, Field field) throws IllegalArgumentException, IllegalAccessException {
		Class<?> type = field.getType();
		String name = getName(field);
		if (type == boolean.class) {
			config.setBool(name, category, field.getBoolean(instance));
		} else if (type == int.class) {
			config.setInt(name, category, field.getInt(instance));
		} else if (type == long.class) {
			config.setLong(name, category, field.getLong(instance));
		} else if (type == float.class) {
			config.setFloat(name, category, field.getFloat(instance));
		} else if (type == double.class) {
			config.setDouble(name, category, field.getDouble(instance));
		} else if (type.isPrimitive()) {
			throw new RuntimeException("Not Supported:" + type.getName());
		} else if (type.isEnum()) {
			config.setEnum(name, category, (Enum<?>) field.get(instance));
		} else if (type.isArray()) {
			throw new RuntimeException("Array not Supported");
		} else if (type == String.class) {
			config.setString(name, category, (String) field.get(instance));
		} else {
			String category_path;
			if (category.isEmpty()) {
				category_path = name;
			} else {
				category_path = category + "." + name;
			}
			for (Field f : field.getType().getFields()) {
				fieldToElement(config, category_path, field.get(instance), f);
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
	private static int getOrder(Field field) {
		ConfigOrder annotation = field.getAnnotation(ConfigOrder.class);
		if (annotation == null)
			return 0;
		return annotation.value();
	}


}
