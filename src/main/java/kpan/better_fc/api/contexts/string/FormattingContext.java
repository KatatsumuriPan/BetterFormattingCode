package kpan.better_fc.api.contexts.string;

import kpan.better_fc.api.RenderingEffects;

public class FormattingContext {

	public final String originalText;
	public final boolean isEdit;

	public final RenderingEffects effects = new RenderingEffects();

	public FormattingContext(String text, boolean isEdit) {
		originalText = text;
		this.isEdit = isEdit;
	}
}
