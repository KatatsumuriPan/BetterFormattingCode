package kpan.better_fc.api;

import kpan.better_fc.asm.hook.HK_ChatAllowedCharacters;
import kpan.better_fc.asm.hook.HK_FontRenderer;
import kpan.better_fc.asm.tf.EnumSectionSignMode;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.input.Keyboard;

public class ModifiedGuiTextField extends GuiTextField {

	public EnumSectionSignMode sectionSignMode;

	public ModifiedGuiTextField(int componentId, FontRenderer fontrendererObj, int x, int y, int width, int height) {
		this(componentId, fontrendererObj, x, y, width, height, EnumSectionSignMode.DISABLED);
	}
	public ModifiedGuiTextField(int componentId, FontRenderer fontrendererObj, int x, int y, int width, int height, EnumSectionSignMode sectionSignMode) {
		super(componentId, fontrendererObj, x, y, width, height);
		this.sectionSignMode = sectionSignMode;
	}


	@Override
	public void writeText(String textToWrite) {
		StringBuilder newTextBuilder = new StringBuilder();
		beforeChatAllowedCharacters();
		String filtered = ChatAllowedCharacters.filterAllowedCharacters(textToWrite);
		afterChatAllowedCharacters();
		int selectionLeft = Math.min(cursorPosition, selectionEnd);
		int selectionRight = Math.max(cursorPosition, selectionEnd);//selectionEndは名前に似合わずstartの時もある
		int restSize = maxStringLength - text.length() - (selectionLeft - selectionRight);

		if (!text.isEmpty()) {
			newTextBuilder.append(text.substring(0, selectionLeft));
		}

		int addedSize;

		if (restSize < filtered.length()) {
			newTextBuilder.append(filtered.substring(0, restSize));
			addedSize = restSize;
		} else {
			newTextBuilder.append(filtered);
			addedSize = filtered.length();
		}

		if (!text.isEmpty() && selectionRight < text.length()) {
			newTextBuilder.append(text.substring(selectionRight));
		}

		String newText = newTextBuilder.toString();
		if (validator.apply(newText)) {
			text = newText;
			moveCursorBy(selectionLeft - selectionEnd + addedSize);
			setResponderEntryValue(id, text);
		}
	}
	@Override
	public boolean textboxKeyTyped(char typedChar, int keyCode) {
		if (!isFocused) {
			return false;
		} else if (GuiScreen.isKeyComboCtrlA(keyCode)) {
			setCursorPositionEnd();
			setSelectionPos(0);
			return true;
		} else if (GuiScreen.isKeyComboCtrlC(keyCode)) {
			GuiScreen.setClipboardString(getSelectedText());
			return true;
		} else if (GuiScreen.isKeyComboCtrlV(keyCode)) {
			if (isEnabled) {
				writeText(GuiScreen.getClipboardString());
			}
			return true;
		} else if (GuiScreen.isKeyComboCtrlX(keyCode)) {
			GuiScreen.setClipboardString(getSelectedText());

			if (isEnabled) {
				writeText("");
			}

			return true;
		} else {
			switch (keyCode) {
				case Keyboard.KEY_BACK -> {
					if (GuiScreen.isCtrlKeyDown()) {
						if (isEnabled) {
							deleteWords(-1);
						}
					} else if (isEnabled) {
						deleteFromCursor(-1);
					}
					return true;
				}
				case Keyboard.KEY_HOME -> {
					if (GuiScreen.isShiftKeyDown()) {
						setSelectionPos(0);
					} else {
						setCursorPositionZero();
					}
					return true;
				}
				case Keyboard.KEY_LEFT -> {
					if (GuiScreen.isShiftKeyDown()) {
						if (GuiScreen.isCtrlKeyDown()) {
							setSelectionPos(getNthWordFromPos(-1, getSelectionEnd()));
						} else {
							setSelectionPos(getSelectionEnd() - 1);
						}
					} else if (GuiScreen.isCtrlKeyDown()) {
						setCursorPosition(getNthWordFromCursor(-1));
					} else {
						moveCursorBy(-1);
					}
					return true;
				}
				case Keyboard.KEY_RIGHT -> {
					if (GuiScreen.isShiftKeyDown()) {
						if (GuiScreen.isCtrlKeyDown()) {
							setSelectionPos(getNthWordFromPos(1, getSelectionEnd()));
						} else {
							setSelectionPos(getSelectionEnd() + 1);
						}
					} else if (GuiScreen.isCtrlKeyDown()) {
						setCursorPosition(getNthWordFromCursor(1));
					} else {
						moveCursorBy(1);
					}
					return true;
				}
				case Keyboard.KEY_END -> {
					if (GuiScreen.isShiftKeyDown()) {
						setSelectionPos(text.length());
					} else {
						setCursorPositionEnd();
					}
					return true;
				}
				case Keyboard.KEY_DELETE -> {
					if (GuiScreen.isCtrlKeyDown()) {
						if (isEnabled) {
							deleteWords(1);
						}
					} else if (isEnabled) {
						deleteFromCursor(1);
					}
					return true;
				}
				default -> {
					beforeChatAllowedCharacters();
					boolean is_allowed = ChatAllowedCharacters.isAllowedCharacter(typedChar);
					afterChatAllowedCharacters();
					if (is_allowed) {
						if (isEnabled) {
							writeText(Character.toString(typedChar));
						}

						return true;
					} else {
						return false;
					}
				}
			}
		}
	}
	@Override
	public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
		boolean areaClicked = mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;

		if (canLoseFocus) {
			setFocused(areaClicked);
		}

		if (isFocused && areaClicked && mouseButton == 0) {
			int dx = mouseX - x;

			if (enableBackgroundDrawing)
				dx -= 4;

			int position = getEndIndexOfTrimmedString(fontRenderer, text, lineScrollOffset, dx);
			setCursorPosition(position);
			return true;
		} else {
			return false;
		}
	}
	@Override
	public void drawTextBox() {
		if (getVisible()) {
			if (getEnableBackgroundDrawing()) {
				Gui.drawRect(x - 1, y - 1, x + width + 1, y + height + 1, -6250336);
				Gui.drawRect(x, y, x + width, y + height, -16777216);
			}
			int color = isEnabled ? enabledColor : disabledColor;
			int scrolledCursorPosition = cursorPosition - lineScrollOffset;
			int scrolledSelectionEnd = selectionEnd - lineScrollOffset;
			int width = getWidth();
			int displayTextEnd;
			displayTextEnd = getEndIndexOfTrimmedString(fontRenderer, text, lineScrollOffset, width);
			int displayTextLength = displayTextEnd - lineScrollOffset;
			boolean isCursorInRange = scrolledCursorPosition >= 0 && scrolledCursorPosition <= displayTextLength;
			boolean drawCusorNow = isFocused && cursorCounter / 6 % 2 == 0 && isCursorInRange;
			int startX = enableBackgroundDrawing ? x + 4 : x;
			int posY = enableBackgroundDrawing ? y + (height - 8) / 2 : y;
			int posX = startX;

			if (scrolledSelectionEnd > displayTextLength) {
				scrolledSelectionEnd = displayTextLength;
			}

			if (displayTextLength > 0) {
				switch (getProcessedSectionSignMode()) {
					case DISABLED -> {
						posX = startX + (int) RenderFontUtil.getSubStringWidthFloat(fontRenderer, text, lineScrollOffset, cursorPosition) + 1;
						RenderFontUtil.drawSubStringWithShadow(fontRenderer, text, lineScrollOffset, displayTextEnd, startX, posY, color);
					}
					case RAW -> {
						posX = startX + (int) RenderFontUtil.getRawStringWidthFloat(fontRenderer, text.substring(lineScrollOffset, cursorPosition)) + 1;
						RenderFontUtil.drawRawStringWithShadow(fontRenderer, text.substring(lineScrollOffset, displayTextEnd), startX, posY, color);
					}
					case COMMAND -> {
						posX = startX + (int) RenderFontUtil.getRawStringWidthFloat(fontRenderer, text.substring(lineScrollOffset, cursorPosition)) + 1;
						RenderFontUtil.drawRawStringWithShadow(fontRenderer, text.substring(lineScrollOffset, displayTextEnd), startX, posY, color);
					}
					case EDIT -> {
						HK_FontRenderer.startEditMode();
						posX = startX + (int) RenderFontUtil.getSubStringWidthFloat(fontRenderer, text, lineScrollOffset, cursorPosition) + 1;
						RenderFontUtil.drawSubStringWithShadow(fontRenderer, text, lineScrollOffset, displayTextEnd, startX, posY, color);
						HK_FontRenderer.endEditMode();
					}
					case REPAIR -> {
						HK_FontRenderer.startEditMode();
						posX = startX + (int) RenderFontUtil.getSubStringWidthFloat(fontRenderer, text, lineScrollOffset, cursorPosition) + 1;
						RenderFontUtil.drawSubStringWithShadow(fontRenderer, TextFormatting.ITALIC + text, 2 + lineScrollOffset, 2 + displayTextEnd, startX, posY, color);//italicによって文字の太さが変わらない前提
						HK_FontRenderer.endEditMode();
					}
					default -> throw new RuntimeException("invalid enum value:" + getProcessedSectionSignMode());
				}
			}

			boolean pipeCursor = cursorPosition < text.length() || text.length() >= getMaxStringLength();
			int cursorX = posX;

			if (!isCursorInRange) {
				cursorX = scrolledCursorPosition > 0 ? startX + width : startX;
			} else if (pipeCursor) {
				cursorX = posX - 1;
				--posX;
			}

			if (drawCusorNow) {
				if (pipeCursor) {
					Gui.drawRect(cursorX, posY - 1, cursorX + 1, posY + 1 + fontRenderer.FONT_HEIGHT, -3092272);
				} else {
					fontRenderer.drawStringWithShadow("_", (float) cursorX, (float) posY, color);
				}
			}

			if (scrolledSelectionEnd != scrolledCursorPosition) {
				float widthToSelectionEnd;
				switch (getProcessedSectionSignMode()) {
					case DISABLED -> {
						widthToSelectionEnd = RenderFontUtil.getSubStringWidthFloat(fontRenderer, text, lineScrollOffset, selectionEnd);
					}
					case RAW -> {
						widthToSelectionEnd = RenderFontUtil.getRawStringWidthFloat(fontRenderer, text.substring(lineScrollOffset, selectionEnd));
					}
					case COMMAND -> {
						widthToSelectionEnd = RenderFontUtil.getRawStringWidthFloat(fontRenderer, text.substring(lineScrollOffset, selectionEnd));
					}
					case EDIT, REPAIR -> {
						HK_FontRenderer.startEditMode();
						widthToSelectionEnd = RenderFontUtil.getSubStringWidthFloat(fontRenderer, text, lineScrollOffset, selectionEnd);
						HK_FontRenderer.endEditMode();
					}
					default -> throw new RuntimeException("invalid enum value:" + getProcessedSectionSignMode());
				}
				int x = startX + (int) widthToSelectionEnd;
				drawSelectionBox(cursorX, posY - 1, x - 1, posY + 1 + fontRenderer.FONT_HEIGHT);
			}
		}
	}
	@Override
	public void setSelectionPos(int position) {
		int length = text.length();

		position = MathHelper.clamp(position, 0, length);

		selectionEnd = position;
		if (fontRenderer == null)
			return;
		if (lineScrollOffset > length)
			lineScrollOffset = length;


		int width = getWidth();

		if (position == lineScrollOffset) {
			switch (getProcessedSectionSignMode()) {
				case DISABLED -> {
					lineScrollOffset = RenderFontUtil.getBeginIndexInclOfReversedTrimmedSubString(fontRenderer, text, lineScrollOffset, width);
				}
				case RAW -> {
					lineScrollOffset = lineScrollOffset - RenderFontUtil.trimRawStringToWidth(fontRenderer, text.substring(0, lineScrollOffset), width, true).length();
				}
				case COMMAND -> {
					lineScrollOffset = lineScrollOffset - RenderFontUtil.trimRawStringToWidth(fontRenderer, text.substring(0, lineScrollOffset), width, true).length();
				}
				case EDIT, REPAIR -> {
					HK_FontRenderer.startEditMode();
					lineScrollOffset = RenderFontUtil.getBeginIndexInclOfReversedTrimmedSubString(fontRenderer, text, lineScrollOffset, width);
					HK_FontRenderer.endEditMode();
				}
				default -> throw new RuntimeException("invalid enum value:" + getProcessedSectionSignMode());
			}
		}

		int displayTextEnd = getEndIndexOfTrimmedString(fontRenderer, text, lineScrollOffset, width);

		if (position > displayTextEnd) {
			lineScrollOffset += position - displayTextEnd;
		} else if (position <= lineScrollOffset) {
			lineScrollOffset -= lineScrollOffset - position;
		}

		lineScrollOffset = MathHelper.clamp(lineScrollOffset, 0, length);
	}

	@Override
	public int getWidth() {
		return enableBackgroundDrawing ? width - 8 : width - 2;//ここの-2にしたところが困り種
	}

	public EnumSectionSignMode getProcessedSectionSignMode() {
		if (sectionSignMode == EnumSectionSignMode.CHAT) {
			if (text.startsWith("/"))
				return EnumSectionSignMode.COMMAND;
			else
				return EnumSectionSignMode.EDIT;
		}
		return sectionSignMode;
	}

	public int getEndIndexOfTrimmedString(FontRenderer fontRenderer, String text, int lineScrollOffset, int width) {
		int displayTextEnd;
		switch (getProcessedSectionSignMode()) {
			case DISABLED -> {
				displayTextEnd = RenderFontUtil.getEndIndexExcOfTrimmedSubString(fontRenderer, text, lineScrollOffset, width);
			}
			case RAW -> {
				displayTextEnd = lineScrollOffset + RenderFontUtil.trimRawStringToWidth(fontRenderer, text.substring(lineScrollOffset), width, false).length();
			}
			case COMMAND -> {
				displayTextEnd = lineScrollOffset + RenderFontUtil.trimRawStringToWidth(fontRenderer, text.substring(lineScrollOffset), width, false).length();
			}
			case EDIT, REPAIR -> {
				HK_FontRenderer.startEditMode();
				displayTextEnd = RenderFontUtil.getEndIndexExcOfTrimmedSubString(fontRenderer, text, lineScrollOffset, width);
				HK_FontRenderer.endEditMode();
			}
			default -> throw new RuntimeException("invalid enum value:" + getProcessedSectionSignMode());
		}
		return displayTextEnd;
	}

	public void beforeChatAllowedCharacters() {
		if (sectionSignMode != EnumSectionSignMode.DISABLED)
			HK_ChatAllowedCharacters.allowSectionSign = true;
	}
	public void afterChatAllowedCharacters() {
		HK_ChatAllowedCharacters.allowSectionSign = false;
	}

}
