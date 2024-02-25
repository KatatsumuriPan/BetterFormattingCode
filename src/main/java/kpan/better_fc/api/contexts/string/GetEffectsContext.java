package kpan.better_fc.api.contexts.string;

import kpan.better_fc.api.RenderingEffects;

public class GetEffectsContext {

	public final String originalText;
	public final boolean isEdit;

	public final RenderingEffects effects = new RenderingEffects();

	public GetEffectsContext(String text, boolean isEdit) {
		originalText = text;
		this.isEdit = isEdit;
	}
}
