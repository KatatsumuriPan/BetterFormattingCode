package kpan.better_fc.asm.hook;

import com.google.common.collect.Lists;
import com.google.gson.JsonParseException;
import kpan.better_fc.api.RenderFontUtil;
import net.minecraft.client.gui.GuiScreenBook;
import net.minecraft.client.gui.GuiUtilRenderComponents;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemWrittenBook;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import org.apache.commons.lang3.tuple.Pair;

@SuppressWarnings("unused")
public class HK_GuiScreenBook {

	private static final ResourceLocation BOOK_GUI_TEXTURES = new ResourceLocation("textures/gui/book.png");

	public static void drawScreen(GuiScreenBook self, int mouseX, int mouseY, float partialTicks) {
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		self.mc.getTextureManager().bindTexture(BOOK_GUI_TEXTURES);
		int startX = (self.width - 192) / 2;
		self.drawTexturedModalRect(startX, 2, 0, 0, 192, 192);

		if (self.bookGettingSigned) {
			String editTitle = I18n.format("book.editTitle");
			float width_editTitle = RenderFontUtil.getStringWidthFloat(self.fontRenderer, editTitle);
			RenderFontUtil.drawString(self.fontRenderer, editTitle, startX + 36 + (116 - width_editTitle) / 2, 34, 0);
			String title = self.bookTitle;


			HK_FontRenderer.startEditMode();
			float width_title = RenderFontUtil.getStringWidthFloat(self.fontRenderer, title);
			float x = RenderFontUtil.drawString(self.fontRenderer, title, startX + 36 + (116 - width_title) / 2, 50, 0);
			HK_FontRenderer.endEditMode();
			if (self.bookIsUnsigned) {
				String cursor;
				if (self.updateCount / 6 % 2 == 0) {
					cursor = TextFormatting.BLACK + "_";
				} else {
					cursor = TextFormatting.GRAY + "_";
				}
				RenderFontUtil.drawString(self.fontRenderer, cursor, x, 50, 0, false);
			}
			String byAuthor = I18n.format("book.byAuthor", self.editingPlayer.getName());
			float width_byAuthor = RenderFontUtil.getStringWidthFloat(self.fontRenderer, byAuthor);
			RenderFontUtil.drawString(self.fontRenderer, TextFormatting.DARK_GRAY + byAuthor, startX + 36 + (116 - width_byAuthor) / 2, 60, 0);
			String finalizeWarning = I18n.format("book.finalizeWarning");
			RenderFontUtil.drawSplitString(self.fontRenderer, finalizeWarning, startX + 36, 82, 116, 0);
		} else {
			String pageIndicator = I18n.format("book.pageIndicator", self.currPage + 1, self.bookTotalPages);
			String currentText = "";

			if (self.bookPages != null && self.currPage >= 0 && self.currPage < self.bookPages.tagCount()) {
				currentText = self.bookPages.getStringTagAt(self.currPage);
			}

			if (!self.bookIsUnsigned && self.cachedPage != self.currPage) {
				if (ItemWrittenBook.validBookTagContents(self.book.getTagCompound())) {
					try {
						ITextComponent itextcomponent = ITextComponent.Serializer.jsonToComponent(currentText);
						self.cachedComponents = itextcomponent != null ? GuiUtilRenderComponents.splitText(itextcomponent, 116, self.fontRenderer, true, true) : null;
					} catch (JsonParseException var13) {
						self.cachedComponents = null;
					}
				} else {
					TextComponentString textcomponentstring = new TextComponentString(TextFormatting.DARK_RED + "* Invalid book tag *");
					self.cachedComponents = Lists.newArrayList(textcomponentstring);
				}

				self.cachedPage = self.currPage;
			}

			float j1 = RenderFontUtil.getStringWidthFloat(self.fontRenderer, pageIndicator);
			RenderFontUtil.drawString(self.fontRenderer, pageIndicator, startX - j1 + 192 - 44, 18, 0);

			if (self.cachedComponents == null) {
				if (self.bookIsUnsigned) {
					HK_FontRenderer.startEditMode();
					Pair<Float, Float> pair = RenderFontUtil.drawSplitString(self.fontRenderer, currentText, startX + 36, 34, 116 * 2, 0, false);
					HK_FontRenderer.endEditMode();
					String cursor;
					if (self.fontRenderer.getBidiFlag()) {
						cursor = "_";
					} else if (self.updateCount / 6 % 2 == 0) {
						cursor = TextFormatting.BLACK + "_";
					} else {
						cursor = TextFormatting.GRAY + "_";
					}
					RenderFontUtil.drawString(self.fontRenderer, cursor, (int) (float) pair.getLeft(), (int) (float) pair.getRight(), 0);
				} else {
					RenderFontUtil.drawSplitString(self.fontRenderer, currentText, startX + 36, 34, 116, 0, false);
				}
			} else {
				int k1 = Math.min(128 / self.fontRenderer.FONT_HEIGHT, self.cachedComponents.size());

				for (int l1 = 0; l1 < k1; ++l1) {
					ITextComponent itextcomponent2 = self.cachedComponents.get(l1);
					RenderFontUtil.drawString(self.fontRenderer, itextcomponent2.getUnformattedText(), startX + 36, 34 + l1 * self.fontRenderer.FONT_HEIGHT, 0);
				}

				ITextComponent itextcomponent1 = self.getClickedComponentAt(mouseX, mouseY);

				if (itextcomponent1 != null) {
					self.handleComponentHover(itextcomponent1, mouseX, mouseY);
				}
			}
		}
	}

	public static int getTitleMaxLength() {
		return 32500;
	}

	public static void preRender() {
		HK_FontRenderer.startEditMode();
	}

	public static void postRender() {
		HK_FontRenderer.endEditMode();
	}

	public static int getWordWrappedMaxHeight() {
		return (int) (RenderFontUtil.FONT_HEIGHT * 20);
	}
	public static int getTextMaxLength() {
		return 32500;
	}
}
