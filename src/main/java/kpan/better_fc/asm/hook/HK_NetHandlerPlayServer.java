package kpan.better_fc.asm.hook;

import kpan.better_fc.api.RenderFontUtil;
import net.minecraft.network.NetHandlerPlayServer;

@SuppressWarnings("unused")
public class HK_NetHandlerPlayServer {

	public NetHandlerPlayServer dbg;

	public static String formatSignText(String text) {
		return text;
	}

	public static int getItemNameMaxLength() {
		return HK_GuiRepair.getItemNameMaxLength();
	}
	public static String escapeChatCommandText(String text) {
		return RenderFontUtil.escape(text);
	}
}
