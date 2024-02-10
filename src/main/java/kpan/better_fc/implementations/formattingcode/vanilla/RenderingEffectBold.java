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
		if (!RenderFontUtil.isSpace(context.charToRender)) {
			float offset = getOffset(context.fontRenderer, context.charToRender);
			RenderFontUtil.renderCharRaw(context.red, context.green, context.blue, context.alpha,//rgba
					context.minU, context.minV, context.maxU, context.maxV,//uv
					context.posX + context.renderLeftTopX + offset, context.posY + context.renderLeftTopY, context.renderLeftTopZ,//leftTop
					context.posX + context.renderLeftBottomX + offset, context.posY + context.renderLeftBottomY, context.renderLeftBottomZ,//leftBottom
					context.posX + context.renderRightTopX + offset, context.posY + context.renderRightTopY, context.renderRightTopZ,//rightTop
					context.posX + context.renderRightBottomX + offset, context.posY + context.renderRightBottomY, context.renderRightBottomZ);//rightBottom
			if (context.stringContext.smoothFontIntegration != null)
				context.stringContext.smoothFontIntegration.renderBold(context, offset);
		}
		context.nextRenderXOffset += 1;//optifineのoffsetBoldは無視する
	}
	private static float getOffset(FontRenderer fontRenderer, char c) {
		return RenderFontUtil.getOffsetBold(fontRenderer, c);//ここだけはoffsetBoldを使う
	}
}
