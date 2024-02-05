package kpan.better_fc.api.contexts.chara;

import kpan.better_fc.api.contexts.string.RenderingStringContext;
import net.minecraft.client.gui.FontRenderer;

public class RenderingCharContext {

	public final RenderingStringContext stringContext;
	public final FontRenderer fontRenderer;
	public final char charToRender;
	public final float charRenderingWidth;
	public final float charHeight;
	public final boolean asShadow;
	public final int framebufferObject;
	public float minU;
	public float minV;
	public float maxU;
	public float maxV;
	public float posX;
	public float posY;
	public float centerY; //文字によってYの中心位置が変わるのは割と困るので
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


	public RenderingCharContext(RenderingStringContext stringContext, char c, float charRenderingWidth, float charHeight, float minU, float minV, float maxU, float maxV, float posX, float posY, float centerY, float red, float green, float blue, float alpha, float nextRenderXOffset) {
		this.stringContext = stringContext;
		charToRender = c;
		this.charRenderingWidth = charRenderingWidth;
		this.charHeight = charHeight;
		this.minU = minU;
		this.minV = minV;
		this.maxU = maxU;
		this.maxV = maxV;
		this.posX = posX;
		this.posY = posY;
		this.centerY = centerY;
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

		fontRenderer = stringContext.fontRenderer;
		asShadow = stringContext.asShadow;
		framebufferObject = stringContext.framebufferObject;
	}
}
