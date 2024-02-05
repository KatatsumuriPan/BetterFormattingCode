package kpan.better_fc.asm.hook;

import kpan.better_fc.SectionCodeSign;
import net.minecraft.client.gui.GuiScreen;

public class HK_GuiScreen {
	public static void onKeyTyped(GuiScreen guiScreen, char typedChar, int keyCode) {
		SectionCodeSign.onKeyTyped(guiScreen, typedChar, keyCode);
	}
	public static void onMouseClicked() {
		SectionCodeSign.resetTime();
	}
}
