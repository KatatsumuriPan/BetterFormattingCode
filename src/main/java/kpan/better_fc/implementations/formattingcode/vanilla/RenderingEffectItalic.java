package kpan.better_fc.implementations.formattingcode.vanilla;

import kpan.better_fc.api.IRenderingEffectFancyStyle;
import kpan.better_fc.api.contexts.chara.RenderingCharContext;

public class RenderingEffectItalic implements IRenderingEffectFancyStyle {

	public static final RenderingEffectItalic INSTANCE = new RenderingEffectItalic();

	private RenderingEffectItalic() { }

	@Override
	public void preRender(RenderingCharContext context) {
		renderItalic(context);
	}

	@Override
	public int priority() { return 80000; }

	public static void renderItalic(RenderingCharContext context) {
		if (context.charToRender != ' ') {
			context.renderLeftTopX += 1;
			context.renderRightTopX += 1;
			context.renderLeftBottomX -= 1;
			context.renderRightBottomX -= 1;
		}
	}
}
