package kpan.better_fc.implementations.formattingcode.vanilla;

import kpan.better_fc.api.FormattingCodeSimple;
import kpan.better_fc.api.RenderingEffects;

public class FormattingCodeReset extends FormattingCodeSimple {

	public static final FormattingCodeReset INSTANCE = new FormattingCodeReset();

	private FormattingCodeReset() {
		super("§r");
	}


	@Override
	public boolean isValid(String option) {
		return true;
	}
	@Override
	protected void applyFormat(RenderingEffects effects, String option) {
		effects.clear();
	}

}
