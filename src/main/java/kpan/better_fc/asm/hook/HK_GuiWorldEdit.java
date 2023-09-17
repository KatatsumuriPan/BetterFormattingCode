package kpan.better_fc.asm.hook;

import kpan.better_fc.asm.tf.EnumSectionSignMode;
import net.minecraft.util.text.TextFormatting;

@SuppressWarnings("unused")
public class HK_GuiWorldEdit {
	public static int getWorldNameMaxLength() {
		return HK_GuiCreateWorld.getWorldNameMaxLength();
	}
	public static EnumSectionSignMode getSectionSignMode() {
		return HK_GuiCreateWorld.getSectionSignMode();
	}
	public static String appendResetCode(String text) {
		return HK_GuiCreateWorld.appendResetCode(text);
	}
	public static String removeLastResetCode(String text) {
		return text.endsWith(TextFormatting.RESET.toString()) ? text.substring(0, text.length() - 2) : text;
	}
}
