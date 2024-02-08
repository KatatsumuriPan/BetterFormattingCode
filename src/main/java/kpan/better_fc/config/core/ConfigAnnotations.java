package kpan.better_fc.config.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class ConfigAnnotations {

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public @interface Name {
		String value();
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public @interface Comment {
		String[] value();
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public @interface ConfigOrder {
		int value() default 0;
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public @interface RangeInt {
		int minValue() default Integer.MIN_VALUE;
		int maxValue() default Integer.MAX_VALUE;
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public @interface RangeLong {
		long minValue() default Long.MIN_VALUE;
		long maxValue() default Long.MAX_VALUE;
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public @interface RangeFloat {
		float minValue() default -Float.MAX_VALUE;
		float maxValue() default Float.MAX_VALUE;
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public @interface RangeDouble {
		double minValue() default -Double.MAX_VALUE;
		double maxValue() default Double.MAX_VALUE;
	}

}
