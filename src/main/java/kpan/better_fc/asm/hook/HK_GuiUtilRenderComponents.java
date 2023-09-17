package kpan.better_fc.asm.hook;

import kpan.better_fc.api.RenderFontUtil;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.text.ITextComponent;

import java.util.List;

@SuppressWarnings("unused")
public class HK_GuiUtilRenderComponents {

	public static List<ITextComponent> splitText(ITextComponent textComponent, int maxTextLenght, FontRenderer fontRendererIn, boolean p_178908_3_, boolean forceTextColor) {
		return RenderFontUtil.split(textComponent, maxTextLenght, fontRendererIn, p_178908_3_, forceTextColor);
	}
}
