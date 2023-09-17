package kpan.better_fc.api.contexts.chara;

import kpan.better_fc.api.IRenderingCharEffect;
import kpan.better_fc.util.CharArrayRingList;
import net.minecraft.client.gui.FontRenderer;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class PreparingContext {

	public final FontRenderer fontRenderer;
	public char charToRender;
	public final boolean isEdit;
	public final boolean isRendering;


	public final String originalText;
	@Nullable
	private final CharArrayRingList ringList;
	public final Collection<IRenderingCharEffect> effects;

	public PreparingContext(FontRenderer fontRenderer, char charToRender, boolean isEdit, boolean isRendering, String originalText, @Nullable CharArrayRingList ringList, Collection<IRenderingCharEffect> effects) {
		this.fontRenderer = fontRenderer;
		this.charToRender = charToRender;
		this.isEdit = isEdit;
		this.isRendering = isRendering;
		this.originalText = originalText;
		if (isEdit)
			this.ringList = null;
		else if (ringList == null)
			throw new NullPointerException("ringList can't be null if not in EditMode!");
		else
			this.ringList = ringList;
		this.effects = effects;
	}

	public CharArrayRingList getRingList() {
		if (isEdit)
			throw new IllegalStateException("RingList is not available in EditMode!");
		else
			return ringList;
	}
}
