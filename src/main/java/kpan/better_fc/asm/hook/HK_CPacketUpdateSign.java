package kpan.better_fc.asm.hook;

import net.minecraft.util.text.ITextComponent;

@SuppressWarnings("unused")
public class HK_CPacketUpdateSign {
	public static String toString(ITextComponent itc) {
		String res = itc.getUnformattedText();
		if (res.length() >= 2)
			res = res.substring(0, res.length() - 2);
		return res;
	}
}
