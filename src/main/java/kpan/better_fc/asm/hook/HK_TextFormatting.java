package kpan.better_fc.asm.hook;

import kpan.better_fc.api.RenderFontUtil;

import javax.annotation.Nullable;

@SuppressWarnings("unused")
public class HK_TextFormatting {
	@Nullable
	public static String getTextWithoutFormattingCodes(@Nullable String text) {
		if (text == null)
			return null;
		return RenderFontUtil.removeFormat(text);
	}
}
