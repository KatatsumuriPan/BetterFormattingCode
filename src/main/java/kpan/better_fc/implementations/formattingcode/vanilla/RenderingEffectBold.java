package kpan.better_fc.implementations.formattingcode.vanilla;

import kpan.better_fc.api.IRenderingEffectFancyStyle;
import kpan.better_fc.api.RenderFontUtil;
import kpan.better_fc.api.contexts.chara.MeasuringCharWidthContext;
import kpan.better_fc.api.contexts.chara.RenderingCharContext;
import net.minecraft.client.gui.FontRenderer;

public class RenderingEffectBold implements IRenderingEffectFancyStyle {

	public static final RenderingEffectBold INSTANCE = new RenderingEffectBold();

	private RenderingEffectBold() { }

	@Override
	public void postRender(RenderingCharContext context) {
		renderBold(context);
	}

	@Override
	public void second(MeasuringCharWidthContext context) {
		context.charWidthWithSpace += 1;//optifineのoffsetBoldは無視する
	}
	@Override
	public int priority() { return 90000; }

	public static void renderBold(RenderingCharContext context) {
		if (context.charToRender != ' ') {
			float offset = getOffset(context.fontRenderer, context.charToRender);
			RenderFontUtil.renderCharRaw(context.red, context.green, context.blue, context.alpha, context.minU, context.minV, context.maxU, context.maxV, context.posX + context.renderLeftTopX + offset, context.posY + context.renderLeftTopY, context.renderLeftTopZ, context.posX + context.renderLeftBottomX + offset, context.posY + context.renderLeftBottomY, context.renderLeftBottomZ, context.posX + context.renderRightTopX + offset, context.posY + context.renderRightTopY, context.renderRightTopZ, context.posX + context.renderRightBottomX + offset, context.posY + context.renderRightBottomY, context.renderRightBottomZ);
		}
		context.nextRenderXOffset += 1;//optifineのoffsetBoldは無視する
	}
	private static float getOffset(FontRenderer fontRenderer, char c) {
		if (RenderFontUtil.getAsciiCharIndex(fontRenderer, c) != -1)
			return RenderFontUtil.getOffsetBold(fontRenderer);//ここだけはoffsetBoldを使おう
		else
			return 0.5f;
	}
}
