package kpan.better_fc.implementations.formattingcode.vanilla;

import kpan.better_fc.api.IRenderingEffectSingleColor;
import kpan.better_fc.api.contexts.chara.RenderingCharContext;
import kpan.better_fc.asm.compat.CompatOptifine;
import kpan.better_fc.compat.optifine.CompatCustomColors;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.text.TextFormatting;

public class RenderingEffectColor implements IRenderingEffectSingleColor {

	public final TextFormatting color;
	public RenderingEffectColor(TextFormatting color) {
		this.color = color;
	}

	@Override
	public void preRender(RenderingCharContext context) {
		int j1 = getColor(context.fontRenderer, color.getColorIndex() + (context.asShadow ? 16 : 0));
		float r = (float) (j1 >> 16) / 255.0F;
		float g = (float) (j1 >> 8 & 255) / 255.0F;
		float b = (float) (j1 & 255) / 255.0F;
		context.red = r;
		context.green = g;
		context.blue = b;
	}

	@Override
	public int getColor(FontRenderer fontRenderer) {
		int index = color.getColorIndex();
		return getColor(fontRenderer, index);
	}
	@Override
	public int priority() { return 10000; }

	public static int getColor(FontRenderer fontRenderer, int index) {
		int color = fontRenderer.colorCode[index];
		if (CompatOptifine.isLoaded())
			return CompatCustomColors.getTextColor(index, color);
		else
			return color;
	}
}
