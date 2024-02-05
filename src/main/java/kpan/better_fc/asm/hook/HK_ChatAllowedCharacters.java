package kpan.better_fc.asm.hook;

import kpan.better_fc.api.RenderFontUtil;
import kpan.better_fc.config.ConfigHolder;

@SuppressWarnings("unused")
public class HK_ChatAllowedCharacters {

	public static boolean allowSectionSign = false;

	public static boolean isAllowedCharacter(char character) {
		if (character == 167)
			return (RenderFontUtil.isEditMode || allowSectionSign) && ConfigHolder.common.allowEditingFormattingChar;
		else
			return character >= ' ' && character != 127;
	}
}
