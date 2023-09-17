package kpan.better_fc.api.contexts.string;

import kpan.better_fc.api.IRenderingCharEffect;
import kpan.better_fc.util.SortedList;

import java.util.Collection;

public class FormattingContext {

	public final String originalText;
	public final boolean isEdit;

	public final Collection<IRenderingCharEffect> effects = new SortedList<>(IRenderingCharEffect.COMPARATOR);

	public FormattingContext(String text, boolean isEdit) {
		originalText = text;
		this.isEdit = isEdit;
	}
}
