package kpan.better_fc.api;

import java.util.function.Consumer;
import kpan.better_fc.api.contexts.string.RenderingStringContext;

public abstract class FormattingCodeStencilColor extends FormattingCodeSimple {
	public final StencilTextRenderer renderer;
	protected FormattingCodeStencilColor(String formattingCode, Consumer<RenderingStringContext> renderer) {
		super(formattingCode);
		this.renderer = new StencilTextRenderer(renderer);
	}

	protected abstract RenderingEffectStencilText getEffect(RenderingEffects effects, String option);

	@Override
	protected void applyFormat(RenderingEffects effects, String option) {
		RenderingEffectStencilText effect = getEffect(effects, option);
		effects.removeIf(e -> e instanceof IRenderingEffectColor);
		effects.add(effect);
	}

	@Override
	public void applyFormat(RenderingStringContext context, String option) {
		if (!context.listners.contains(renderer)) {
			context.listners.add(renderer);
			renderer.clear(context);
		}
		super.applyFormat(context, option);
	}

}
