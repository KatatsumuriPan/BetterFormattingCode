package kpan.better_fc.asm.hook;

@SuppressWarnings("unused")
public class HK_CPacketChatMessage {
	public static int getChatMaxLength() {
		return HK_GuiChat.getChatMaxLength();
	}
}
