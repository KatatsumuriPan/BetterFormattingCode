package kpan.better_fc.api.contexts.string;

import kpan.better_fc.api.IRenderingCharEffect;
import kpan.better_fc.util.CharArrayRingList;
import kpan.better_fc.util.SortedList;
import net.minecraft.client.gui.FontRenderer;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class MeasuringStringWidthContext {

	public final FontRenderer fontRenderer;

	public final String originalText;
	public final boolean isEdit;

	@Nullable
	private final CharArrayRingList ringList;
	public final Collection<IRenderingCharEffect> effects = new SortedList<>(IRenderingCharEffect.COMPARATOR);

	public MeasuringStringWidthContext(FontRenderer fontRenderer, String text, boolean isEdit) {
		this.fontRenderer = fontRenderer;
		originalText = text;
		this.isEdit = isEdit;
		if (isEdit)
			ringList = null;
		else
			ringList = new CharArrayRingList();
	}
	public CharArrayRingList getRingList() {
		if (isEdit)
			throw new IllegalStateException("RingList is not available in EditMode!");
		else
			return ringList;
	}
	@Nullable
	public CharArrayRingList tryGetRingList() {
		if (isEdit)
			return null;
		else
			return ringList;
	}
}
