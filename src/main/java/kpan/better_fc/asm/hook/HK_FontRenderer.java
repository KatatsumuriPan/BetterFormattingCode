package kpan.better_fc.asm.hook;

import kpan.better_fc.api.RenderFontUtil;
import net.minecraft.client.gui.FontRenderer;

public class HK_FontRenderer {


	private static int editModeCount = 0;

	@SuppressWarnings("unused")
	public static void startEditMode() {
		/*
		これらを呼んでたらinject必須
		GuiTextField
			deleteFromCursor
			deleteWords
			drawTextBox
			mouseClicked
			moveCursorBy
			setCursorPosition
			setCursorPositionEnd
			setCursorPositionZero
			setSelectionPos
			setText
			textboxKeyTyped
			writeText
		TabCompleter
			complete
			setCompletions

		以下inject対象
		GuiEditSign
			keyTyped*置き換え
			drawScreen*一部

		GuiChat
			keyTyped*一部
			mouseClicked*一部
			setText
			getSentHistory
			drawScreen*一部
			setCompletions

		GuiCommandBlock
			updateGui*一部
			keyTyped*一部
			mouseClicked*一部
			drawScreen*一部
			setCompletions

		GuiRepair
			keyTyped
			mouseClicked*一部
			drawScreen*一部
			sendSlotContents

		NetHandlerPlayServer
			processChatMessage*一部
			processCustomPayload*一部
		 */
		editModeCount++;
		RenderFontUtil.isEditMode = true;
		//aaaa§aaaaaa§bffef§lefef§yrfg§nrgfbsfew§ofefefefef§efbrb§§rbrb
	}
	@SuppressWarnings("unused")
	public static void endEditMode() {
		editModeCount--;
		if (editModeCount == 0)
			RenderFontUtil.isEditMode = false;
	}

	//overwrites
	@SuppressWarnings("unused")
	public static boolean renderStringAtPos(FontRenderer self, String text, boolean shadow) {
		if (self.getClass() != FontRenderer.class)
			return false;
		self.posX = RenderFontUtil.renderString(self, text, self.posX, self.posY, shadow);
		return true;
	}

	@SuppressWarnings("unused")
	public static int getStringWidth(FontRenderer self, String text) {
		if (text == null)
			return 0;
		return (int) RenderFontUtil.getStringWidthFloat(self, text);
	}

	@SuppressWarnings("unused")
	public static String trimStringToWidth(FontRenderer self, String text, int width, boolean reverse) {
		return RenderFontUtil.trimStringToWidth(self, text, width, reverse);
	}

	@SuppressWarnings("unused")
	public static String wrapFormattedStringToWidth(FontRenderer self, String str, int wrapWidth) {
		return RenderFontUtil.wrapFormattedStringToWidth(self, str, wrapWidth, true);
	}

	@SuppressWarnings("unused")
	public static String getFormatFromString(String text) {
		return RenderFontUtil.getFormatFromString(text);
	}

}
