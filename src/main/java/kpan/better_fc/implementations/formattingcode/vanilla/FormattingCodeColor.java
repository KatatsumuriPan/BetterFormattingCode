package kpan.better_fc.implementations.formattingcode.vanilla;

import kpan.better_fc.api.FormattingCodeSimple;
import kpan.better_fc.api.IRenderingCharEffect;
import kpan.better_fc.api.IRenderingEffectColor;
import kpan.better_fc.api.IRenderingEffectFancyStyle;
import kpan.better_fc.api.contexts.string.GetEffectsContext;
import kpan.better_fc.api.contexts.string.RenderingStringContext;
import net.minecraft.util.text.TextFormatting;

import java.util.Collection;

public class FormattingCodeColor extends FormattingCodeSimple {

	public final TextFormatting color;
	public FormattingCodeColor(TextFormatting color) {
		super(color.toString());
		this.color = color;
		if (!color.isColor())
			throw new IllegalArgumentException("the argument is not a color!:" + color);
	}

	@Override
	public boolean isValid(String option) {
		return true;
	}
	@Override
	protected void applyFormat(Collection<IRenderingCharEffect> effects, String option) {
		effects.removeIf(e -> e instanceof IRenderingEffectColor);
		effects.removeIf(e -> e instanceof IRenderingEffectFancyStyle);
	}
	@Override
	public void applyFormat(RenderingStringContext context, String option) {
		super.applyFormat(context, option);
		context.effects.add(new RenderingEffectColor(color));
	}
	@Override
	public void applyFormat(GetEffectsContext context, String option) {
		super.applyFormat(context, option);
		context.effects.add(new RenderingEffectColor(color));
	}
}
