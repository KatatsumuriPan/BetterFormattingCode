package kpan.better_fc.compat.optifine;

import net.minecraft.client.gui.FontRenderer;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class CompatFontRenderer_Optifine {

	private static final MethodHandle getCharWidthFloat;
	private static final MethodHandle offsetBold;

	static {
		MethodHandles.Lookup lookup = MethodHandles.lookup();

		try {
			{
				Method m = FontRenderer.class.getDeclaredMethod("getCharWidthFloat", char.class);
				m.setAccessible(true);
				getCharWidthFloat = lookup.unreflect(m);
			}
			{
				Field f = FontRenderer.class.getField("offsetBold");
				offsetBold = lookup.unreflectGetter(f);
			}
		} catch (NoSuchMethodException | IllegalAccessException | NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}

	public static float getCharWidthFloat(FontRenderer fontRenderer, char ch) {
		try {
			return (float) getCharWidthFloat.invokeExact(fontRenderer, ch);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public static float getOffsetBold(FontRenderer fontRenderer) {
		try {
			return (float) offsetBold.invokeExact(fontRenderer);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}
}
