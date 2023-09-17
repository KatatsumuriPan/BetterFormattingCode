package kpan.better_fc.asm.hook;

import kpan.better_fc.asm.tf.EnumSectionSignMode;

public class HK_GuiChat {

	public static int getChatMaxLength() {
		return 32500;//CPacketChatMessageも置き換え必要
	}

	public static EnumSectionSignMode getSectionSignMode() {
		return EnumSectionSignMode.CHAT;
	}
}
