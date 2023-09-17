package kpan.better_fc.asm.hook;

@SuppressWarnings("unused")
public class HK_GuiNewChat {
	public static String removeLastResetCode(String str) {
		if (str.isEmpty())
			return "";
		return str.substring(0, str.length() - 2);
	}
}
