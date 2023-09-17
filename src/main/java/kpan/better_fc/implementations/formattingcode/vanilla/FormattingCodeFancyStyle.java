package kpan.better_fc.implementations.formattingcode.vanilla;

import kpan.better_fc.api.FormattingCodeSimple;
import kpan.better_fc.api.IRenderingCharEffect;

import java.util.Collection;

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
	protected void applyFormat(Collection<IRenderingCharEffect> effects, String option) {
		if (!effects.contains(effect))
			effects.add(effect);
	}
}
