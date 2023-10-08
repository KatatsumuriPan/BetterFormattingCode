package kpan.better_fc.asm.hook;

import kpan.better_fc.api.RenderFontUtil;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.text.ITextComponent;
import org.lwjgl.opengl.GL11;

@SuppressWarnings("unused")
public class HK_TileEntitySignRenderer {
	public static void onRenderText(TileEntitySign te, FontRenderer fontrenderer, int color, int j) {
		ITextComponent itextcomponent = te.signText[j];
		String s = itextcomponent.getFormattedText();
		if (!s.isEmpty())
			s = s.substring(0, s.length() - 2);

		int y = j * 10 - te.signText.length * 5;

		boolean isLightingEnabled = GL11.glGetBoolean(GL11.GL_LIGHTING);
		RenderHelper.disableStandardItemLighting();
		if (HK_GuiEditSign.editLine == j) {
			HK_FontRenderer.startEditMode();
			float w = RenderFontUtil.getStringWidthFloat(fontrenderer, s);
			RenderFontUtil.drawString(fontrenderer, s, -w / 2, y, color);
			HK_FontRenderer.endEditMode();
			if (j == te.lineBeingEdited) {
				RenderFontUtil.drawString(fontrenderer, "> ", -w / 2 - RenderFontUtil.getStringWidthFloat(fontrenderer, "> "), y, color);
				RenderFontUtil.drawString(fontrenderer, " <", w / 2, y, color);
			}
		} else {
			int end = RenderFontUtil.getEndIndexExcOfTrimmedSubString(fontrenderer, s, 0, 95);//ピッタリは96だけど斜めから見るとちょっとはみ出る(なんなら95もわずかにはみ出る)
			float w = RenderFontUtil.getSubStringWidthFloat(fontrenderer, s, 0, end);
			RenderFontUtil.drawSubString(fontrenderer, s, 0, end, -w / 2, y, color);
		}
		if (isLightingEnabled)
			RenderHelper.enableStandardItemLighting();
		GlStateManager.enableDepth();
	}
}
