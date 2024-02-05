package kpan.better_fc.compat;

import bre.smoothfont.FontRendererHook;
import kpan.better_fc.api.RenderFontUtil;
import kpan.better_fc.asm.acc.ACC_FontRendererHook;
import kpan.better_fc.asm.compat.CompatOptifine;
import kpan.better_fc.asm.compat.CompatSmoothFont;
import kpan.better_fc.compat.optifine.CompatFontRenderer_Optifine;
import kpan.better_fc.compat.smoothfont.CompatFontRenderer_SmoothFont;
import net.minecraft.client.gui.FontRenderer;

public class CompatFontRenderer {
	public static float getCharWidthFloat(FontRenderer fontRenderer, char ch) {
		if (CompatSmoothFont.isLoaded())
			return CompatFontRenderer_SmoothFont.getCharWidthFloat(fontRenderer, ch);
		else if (CompatOptifine.isLoaded())
			return CompatFontRenderer_Optifine.getCharWidthFloat(fontRenderer, ch);
		else
			return fontRenderer.getCharWidth(ch);
	}

	public static float getOffsetBold(FontRenderer fontRenderer, char ch) {
		if (CompatSmoothFont.isLoaded())
			return CompatFontRenderer_SmoothFont.getOffsetBold(fontRenderer, ch);
		else {
			if (RenderFontUtil.getAsciiCharIndex(fontRenderer, ch) == -1)
				return 0.5f;
			if (CompatOptifine.isLoaded())
				return CompatFontRenderer_Optifine.getOffsetBold(fontRenderer);
			else
				return 1;
		}
	}

	public static float getSpaceWidth(FontRenderer fontRenderer) {
		if (CompatSmoothFont.isLoaded()) {
			FontRendererHook fontRendererHook = CompatFontRenderer_SmoothFont.getFontRendererHook(fontRenderer);
			return ((ACC_FontRendererHook) fontRendererHook).getSpaceWidth();
		} else {
			return 4;
		}
	}
}
