package kpan.better_fc.compat.smoothfont;

import bre.smoothfont.FontRasterizer;
import bre.smoothfont.FontRendererHook;
import bre.smoothfont.FontUtils;
import bre.smoothfont.asm.CorePlugin;
import kpan.better_fc.api.RenderFontUtil;
import kpan.better_fc.asm.acc.ACC_FontRendererHook;
import kpan.better_fc.compat.optifine.CompatFontRenderer_Optifine;
import net.minecraft.client.gui.FontRenderer;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;

public class CompatFontRenderer_SmoothFont {

	private static final MethodHandle fontRendererHook;

	static {
		MethodHandles.Lookup lookup = MethodHandles.lookup();

		try {
			{
				Field f = FontRenderer.class.getField("fontRendererHook");
				fontRendererHook = lookup.unreflectGetter(f);
			}
		} catch (IllegalAccessException | NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}

	public static float getCharWidthFloat(FontRenderer fontRenderer, char ch) {
		return getCharWidthFloat(getFontRendererHook(fontRenderer), ch);
	}

	public static float getOffsetBold(FontRenderer fontRenderer, char ch) {
		FontRendererHook fontRendererHook = getFontRendererHook(fontRenderer);
		if (CorePlugin.optifineExist) {
			int defaultGlyph = FontUtils.getDefaultGlyphIndex(ch);
			return (defaultGlyph == -1 || fontRendererHook.thinFontFlag) ? 0.5F : CompatFontRenderer_Optifine.getOffsetBold(fontRenderer);
		} else {
			if (fontRendererHook.changeFont)
				return 0.5f;
			if (RenderFontUtil.getAsciiCharIndex(fontRenderer, ch) != -1)
				return 1;
			else
				return 0.5f;
		}
	}

	public static FontRendererHook getFontRendererHook(FontRenderer fontRenderer) {
		try {
			return (FontRendererHook) fontRendererHook.invokeExact(fontRenderer);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}


	private static float getCharWidthFloat(FontRendererHook fontRendererHook, char character) {
		ACC_FontRendererHook accFontRendererHook = (ACC_FontRendererHook) fontRendererHook;
		FontRasterizer rasterizer = accFontRendererHook.get_rasterizer();
		switch (character) {
			case ' ':
			case ' ':
				return accFontRendererHook.getSpaceWidth();
			case '§': //セクション記号も表示したい
			default: {
				int i = FontUtils.getDefaultGlyphIndex(character);
				if (character > 0 && i != -1 && !fontRendererHook.fontRenderer.unicodeFlag) {
					if (fontRendererHook.changeFont) {
						switch (fontRendererHook.precisionMode) {
							case 0:
							default:
								return rasterizer.charWidthFloat[i];
							case 1:
								return (float) FontUtils.toNormalWidth(rasterizer.charWidthFloat[i]);
							case 2:
								return (float) rasterizer.charWidthInt[i];
						}
					} else {
						return CorePlugin.optifineExist && fontRendererHook.optifineCharWidthFloat != null ? fontRendererHook.optifineCharWidthFloat[i] : (float) fontRendererHook.mcCharWidth[i];
					}
				} else if (fontRendererHook.fontRenderer.glyphWidth[character] != 0) {
					int j;
					int k;
					int l;
					if (fontRendererHook.changeFont) {
						switch (fontRendererHook.precisionMode) {
							case 0:
							default:
								return rasterizer.glyphWidthFloat8[character];
							case 1:
								return (float) FontUtils.toNormalWidth(rasterizer.glyphWidthFloat8[character]);
							case 2:
								j = rasterizer.glyphWidthByte[character] & 255;
								k = j >>> 4;
								l = j & 15;
								++l;
								return (float) ((l - k) / 2);
						}
					} else {
						j = fontRendererHook.fontRenderer.glyphWidth[character] & 255;
						k = j >>> 4;
						l = j & 15;
						++l;
						return CorePlugin.optifineExist ? (float) (l - k) / 2.0F + 1.0F : (float) ((l - k) / 2 + 1);
					}
				} else {
					return 0.0F;
				}
			}
		}
	}
}
