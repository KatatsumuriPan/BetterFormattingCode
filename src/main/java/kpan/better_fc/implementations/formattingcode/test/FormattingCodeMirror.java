package kpan.better_fc.implementations.formattingcode.test;

import kpan.better_fc.api.FormattingCodeSimple;
import kpan.better_fc.api.IRenderingCharEffect;

import java.util.Collection;

public class FormattingCodeMirror extends FormattingCodeSimple {
	public static final FormattingCodeMirror INSTANCE = new FormattingCodeMirror();
	private FormattingCodeMirror() {
		super("§p");
	}
	@Override
	public boolean isValid(String option) {
		return true;
	}
	@Override
	protected void applyFormat(Collection<IRenderingCharEffect> effects, String option) {
		if (!effects.contains(RenderingEffectMirror.INSTANCE))
			effects.add(RenderingEffectMirror.INSTANCE);
	}
}
