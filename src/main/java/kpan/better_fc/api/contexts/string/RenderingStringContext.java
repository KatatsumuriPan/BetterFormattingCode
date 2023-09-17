package kpan.better_fc.api.contexts.string;

import kpan.better_fc.api.IRenderingCharEffect;
import kpan.better_fc.api.IStringRenderEndListener;
import kpan.better_fc.util.CharArrayRingList;
import kpan.better_fc.util.SortedList;
import net.minecraft.client.gui.FontRenderer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;

public class RenderingStringContext {

	public final FontRenderer fontRenderer;

	public final String originalText;
	public final boolean isEdit;
	public final int framebufferObject;

	public final boolean asShadow;
	public final float startX;
	public final float startY;

	public float posX;
	public float posY;

	@Nullable
	private final CharArrayRingList ringList;

	public final Collection<IRenderingCharEffect> effects = new SortedList<>(IRenderingCharEffect.COMPARATOR);
	public final ArrayList<IStringRenderEndListener> listners = new ArrayList<>();

	public RenderingStringContext(FontRenderer fontRenderer, String text, float startX, float startY, boolean isEdit, boolean asShadow, int framebufferObject) {
		this.fontRenderer = fontRenderer;
		originalText = text;
		this.isEdit = isEdit;
		this.startX = posX = startX;
		this.startY = posY = startY;
		this.asShadow = asShadow;
		if (isEdit)
			ringList = null;
		else
			ringList = new CharArrayRingList();
		this.framebufferObject = framebufferObject;
	}

	public CharArrayRingList getRingList() {
		if (isEdit)
			throw new IllegalStateException("RingList is not available in EditMode!");
		return ringList;
	}
	@Nullable
	public CharArrayRingList tryGetRingList() {
		if (isEdit)
			return null;
		else
			return ringList;
	}
	public void onRenderEnd() {
		for (IStringRenderEndListener listner : listners) {
			listner.onRenderEnd(this);
		}
	}
}
