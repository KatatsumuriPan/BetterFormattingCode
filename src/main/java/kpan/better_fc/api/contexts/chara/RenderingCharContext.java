package kpan.better_fc.api.contexts.chara;

import net.minecraft.client.gui.FontRenderer;

public class RenderingCharContext {
	public static final float FONT_HEIGHT = 9;

	public final FontRenderer fontRenderer;
	public final char charToRender;
	public final float charRenderingWidth;
	public final float charHeight;
	public final boolean asShadow;
	public final boolean isStringRendering;
	public final int framebufferObject;
	public float minU;
	public float minV;
	public float maxU;
	public float maxV;
	public float posX;
	public float posY;
	public float renderLeftTopX;
	public float renderLeftBottomX;
	public float renderRightTopX;
	public float renderRightBottomX;
	public float renderLeftTopY;
	public float renderLeftBottomY;
	public float renderRightTopY;
	public float renderRightBottomY;
	public float renderLeftTopZ;
	public float renderLeftBottomZ;
	public float renderRightTopZ;
	public float renderRightBottomZ;
	public float red;
	public float green;
	public float blue;
	public float alpha;

	public float nextRenderXOffset;

	public float renderedWidth;


	public RenderingCharContext(FontRenderer fontRenderer, char c, float charRenderingWidth, float charHeight, boolean asShadow, boolean isStringRendering, float minU, float minV, float posX, float posY, float red, float green, float blue, float alpha, float nextRenderXOffset, int framebufferObject) {
		this.fontRenderer = fontRenderer;
		charToRender = c;
		this.charRenderingWidth = charRenderingWidth;
		this.charHeight = charHeight;
		this.asShadow = asShadow;
		this.isStringRendering = isStringRendering;
		this.minU = minU;
		this.minV = minV;
		this.framebufferObject = framebufferObject;
		maxU = minU + charRenderingWidth / 128f;
		maxV = minV + charHeight / 128f;
		this.posX = posX;
		this.posY = posY;
		renderLeftTopX = 0;
		renderLeftBottomX = 0;
		renderRightTopX = charRenderingWidth;
		renderRightBottomX = charRenderingWidth;
		renderLeftTopY = 0;
		renderLeftBottomY = charHeight;
		renderRightTopY = 0;
		renderRightBottomY = charHeight;
		renderLeftTopZ = renderLeftBottomZ = renderRightTopZ = renderRightBottomZ = 0;
		this.red = red;
		this.green = green;
		this.blue = blue;
		this.alpha = alpha;
		renderedWidth = 0;
		this.nextRenderXOffset = nextRenderXOffset;
	}
}
