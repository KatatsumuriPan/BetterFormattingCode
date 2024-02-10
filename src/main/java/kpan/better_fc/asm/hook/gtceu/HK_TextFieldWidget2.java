package kpan.better_fc.asm.hook.gtceu;

import gregtech.api.gui.widgets.TextFieldWidget2;
import gregtech.common.gui.widget.HighlightedTextField;
import kpan.better_fc.api.RenderFontUtil;
import kpan.better_fc.asm.hook.HK_FontRenderer;
import kpan.better_fc.util.MyReflectionHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

public class HK_TextFieldWidget2 {

	public static int getCursorPosFromMouse(TextFieldWidget2 self, int mouseX, int textX, String text) {
		start(self);
		int position = RenderFontUtil.getEndIndexExcOfTrimmedSubString(Minecraft.getMinecraft().fontRenderer, text, 0, mouseX - textX);
		end(self);
		return position;
	}


	public static void drawSelectionBox(TextFieldWidget2 self, String text, int cursorPos, int cursorPos2, float scale, int textX) {
		FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
		int y = self.getPosition().y;
		float startX = RenderFontUtil.getSubStringWidthFloat(fontRenderer, text, 0, Math.min(cursorPos, cursorPos2)) * scale + (float) textX;
		float width = RenderFontUtil.getSubStringWidthFloat(fontRenderer, text, Math.min(cursorPos, cursorPos2), Math.max(cursorPos, cursorPos2));
		MyReflectionHelper.invokePrivateMethod(TextFieldWidget2.class, self, "drawSelectionBox", startX / scale, (float) y, width);
	}
	public static void drawCursor(TextFieldWidget2 self, String text, int cursorPos, float scale, int textX) {
		FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
		int y = self.getPosition().y;
		float x = RenderFontUtil.getSubStringWidthFloat(fontRenderer, text, 0, cursorPos) * scale + (float) textX;
		x /= scale;
		MyReflectionHelper.invokePrivateMethod(TextFieldWidget2.class, self, "drawCursor", x, (float) y);
	}

	public static void start(TextFieldWidget2 self) {
		if (self instanceof HighlightedTextField)
			return;
		HK_FontRenderer.startEditMode();
	}
	public static void end(TextFieldWidget2 self) {
		if (self instanceof HighlightedTextField)
			return;
		HK_FontRenderer.endEditMode();
	}
}
