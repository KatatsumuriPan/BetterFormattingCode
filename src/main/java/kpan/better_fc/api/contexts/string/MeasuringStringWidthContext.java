package kpan.better_fc.api.contexts.string;

import kpan.better_fc.api.RenderingEffects;
import kpan.better_fc.util.CharArrayRingList;
import net.minecraft.client.gui.FontRenderer;
import org.jetbrains.annotations.Nullable;

public class MeasuringStringWidthContext {

	public final FontRenderer fontRenderer;

	public final String originalText;
	public final boolean isEdit;

	@Nullable
	private final CharArrayRingList ringList;
	public final RenderingEffects effects = new RenderingEffects();

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
