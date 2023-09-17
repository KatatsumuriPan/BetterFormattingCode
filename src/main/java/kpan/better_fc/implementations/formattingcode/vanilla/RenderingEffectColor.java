package kpan.better_fc.implementations.formattingcode.vanilla;

import kpan.better_fc.api.IRenderingEffectColor;
import kpan.better_fc.api.contexts.chara.RenderingCharContext;
import net.minecraft.util.text.TextFormatting;

public class RenderingEffectColor implements IRenderingEffectColor {

	public final TextFormatting color;
	public RenderingEffectColor(TextFormatting color) {
		this.color = color;
	}

	@Override
	public void preRender(RenderingCharContext context) {
		int j1 = context.fontRenderer.colorCode[color.getColorIndex() + (context.asShadow ? 16 : 0)];
		float r = (float) (j1 >> 16) / 255.0F;
		float g = (float) (j1 >> 8 & 255) / 255.0F;
		float b = (float) (j1 & 255) / 255.0F;
		context.red = r;
		context.green = g;
		context.blue = b;
	}

	@Override
	public int priority() { return 10000; }
}
