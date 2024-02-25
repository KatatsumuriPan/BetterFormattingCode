package kpan.better_fc.implementations.formattingcode.vanilla;

import kpan.better_fc.api.FormattingCodeSimple;
import kpan.better_fc.api.IRenderingCharEffect;
import kpan.better_fc.api.RenderingEffects;

public class FormattingCodeFancyStyle extends FormattingCodeSimple {

	private final IRenderingCharEffect effect;

	public FormattingCodeFancyStyle(IRenderingCharEffect effect, String formattingCode) {
		super(formattingCode);
		this.effect = effect;
	}

	@Override
	public boolean isValid(String option) {
		return true;
	}
	@Override
	protected void applyFormat(RenderingEffects effects, String option) {
		effects.add(effect);
	}
}
