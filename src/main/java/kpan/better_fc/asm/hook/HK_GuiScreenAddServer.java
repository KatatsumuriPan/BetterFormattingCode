package kpan.better_fc.asm.hook;

import kpan.better_fc.asm.tf.EnumSectionSignMode;
import net.minecraft.util.text.TextFormatting;

@SuppressWarnings("unused")
public class HK_GuiScreenAddServer {
	public static int getServerNameMaxLength() {
		return 32500;
	}
	public static EnumSectionSignMode getSectionSignMode() {
		return EnumSectionSignMode.EDIT;
	}
	public static String appendResetCode(String text) {
		return text + TextFormatting.RESET;
	}
	public static String removeLastResetCode(String text) {
		return text.endsWith(TextFormatting.RESET.toString()) ? text.substring(0, text.length() - 2) : text;
	}
}
