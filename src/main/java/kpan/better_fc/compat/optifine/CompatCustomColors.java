package kpan.better_fc.compat.optifine;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

public class CompatCustomColors {
	private static final MethodHandle getTextColor;

	static {
		MethodHandles.Lookup lookup = MethodHandles.lookup();

		try {
			{
				Method m = Class.forName("net.optifine.CustomColors").getDeclaredMethod("getTextColor", int.class, int.class);
				getTextColor = lookup.unreflect(m);
			}
		} catch (NoSuchMethodException | IllegalAccessException | ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	public static int getTextColor(int index, int defaultColor) {
		try {
			return (int) getTextColor.invokeExact(index, defaultColor);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

}
