package kpan.better_fc.asm.hook;

import kpan.better_fc.api.RenderFontUtil;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiEditSign;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.text.TextComponentString;
import org.lwjgl.input.Keyboard;

import java.io.IOException;

@SuppressWarnings("unused")
public class HK_GuiEditSign {

	public static int editLine = -1;

	public static void keyTyped(GuiEditSign self, char typedChar, int keyCode) throws IOException {
		HK_FontRenderer.startEditMode();
		if (GuiScreen.isKeyComboCtrlV(keyCode)) {
			writeText(self, GuiScreen.getClipboardString());
		} else {
			switch (keyCode) {
				case Keyboard.KEY_ESCAPE -> self.actionPerformed(self.doneBtn);
				case Keyboard.KEY_UP -> self.editLine = self.editLine - 1 & 3;
				case Keyboard.KEY_DOWN, Keyboard.KEY_RETURN, Keyboard.KEY_NUMPADENTER -> self.editLine = self.editLine + 1 & 3;
				case Keyboard.KEY_BACK -> {
					String s = self.tileSign.signText[self.editLine].getUnformattedText();
					if (!s.isEmpty()) {
						s = s.substring(0, s.length() - 1);
						self.tileSign.signText[self.editLine] = new TextComponentString(s);
					}
				}
				default -> {
					writeText(self, String.valueOf(typedChar));
				}
			}
		}
		HK_FontRenderer.endEditMode();
	}
	public static boolean writeText(GuiEditSign self, String text) {
		String s = self.tileSign.signText[self.editLine].getUnformattedText();
		text = ChatAllowedCharacters.filterAllowedCharacters(text);
		if (text.isEmpty())
			return false;
		String new_text = RenderFontUtil.trimStringToWidth(self.fontRenderer, s + text, getMaxWidth(), false);
		if (!s.equals(new_text)) {
			self.tileSign.signText[self.editLine] = new TextComponentString(new_text);
			return true;
		} else {
			return false;
		}
	}

	public static void preRender(GuiEditSign gui) {
		editLine = gui.editLine;
	}

	public static void postRender() {
		editLine = -1;
	}

	public static int getMaxWidth() { return 32500 * 4; }
}
