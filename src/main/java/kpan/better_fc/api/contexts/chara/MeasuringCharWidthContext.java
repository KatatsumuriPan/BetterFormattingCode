package kpan.better_fc.api.contexts.chara;

import net.minecraft.client.gui.FontRenderer;

public class MeasuringCharWidthContext {

	public final FontRenderer fontRenderer;
	public final char charToMeasure;
	public final float charWidth;
	public final float charHeight;

	public float charWidthWithSpace;
	public MeasuringCharWidthContext(FontRenderer fontRenderer, char toMeasure, float charWidth, float charHeight) {
		this.fontRenderer = fontRenderer;
		charToMeasure = toMeasure;
		this.charWidth = charWidth;
		this.charHeight = charHeight;
		charWidthWithSpace = charWidth + 1;
	}
}
