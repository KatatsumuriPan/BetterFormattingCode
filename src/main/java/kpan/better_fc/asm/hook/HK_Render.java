package kpan.better_fc.asm.hook;

import kpan.better_fc.api.RenderFontUtil;
import net.minecraft.client.gui.FontRenderer;

@SuppressWarnings("unused")
public class HK_Render {

	public static int getColor(FontRenderer fontRenderer, String text) {
		return RenderFontUtil.getColor(fontRenderer, RenderFontUtil.getEffects(text));
	}
}
