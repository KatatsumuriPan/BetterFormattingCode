package kpan.better_fc.api;

import kpan.better_fc.api.contexts.chara.RenderingCharContext;
import kpan.better_fc.api.contexts.string.RenderingStringContext;
import kpan.better_fc.util.GLStateManagerUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

public abstract class RenderingEffectStencilColor implements IRenderingEffectColor {

	public final FormattingCodeStencilColor formattingCodeStencilColor;
	protected RenderingEffectStencilColor(FormattingCodeStencilColor formattingCodeStencilColor) {
		this.formattingCodeStencilColor = formattingCodeStencilColor;
	}

	protected abstract void renderTexture(RenderingCharContext context);
	protected abstract void renderTexture(RenderingStringContext context);

	// this should be higher
	@Override
	public abstract int priority();

	@Override
	public void preRender(RenderingCharContext context) {
		if (context.asShadow) {
			context.red = 0.25f;
			context.green = 0.25f;
			context.blue = 0.25f;
		} else {
			if (context.stringContext.stencilColorPrepared)
				return;
			context.red = 1;
			context.green = 1;
			context.blue = 1;
			beginRender(context.framebufferObject);
		}
	}

	@Override
	public void postRender(RenderingCharContext context) {
		if (context.asShadow)
			return;
		if (context.stringContext.stencilColorPrepared)
			return;
		endRender(context);
	}

	public void beginRender(int framebufferObject) {
		formattingCodeStencilColor.init();
		Framebuffer framebuffer = formattingCodeStencilColor.framebuffer;
//		GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, framebufferObject);
		GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, framebuffer.framebufferObject);
		GlStateManager.viewport(0, 0, framebuffer.framebufferWidth, framebuffer.framebufferHeight);
		GlStateManager.clearColor(framebuffer.framebufferColor[0], framebuffer.framebufferColor[1], framebuffer.framebufferColor[2], framebuffer.framebufferColor[3]);
		GlStateManager.clearDepth(1.0D);
		GlStateManager.clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_STENCIL_BUFFER_BIT);
		int w = GLStateManagerUtil.viewportWidth;
		int h = GLStateManagerUtil.viewportHeight;
		GL30.glBlitFramebuffer(0, 0, w, h, 0, 0, w, h, GL11.GL_DEPTH_BUFFER_BIT, GL11.GL_NEAREST);

		GL11.glStencilMask(255);
		GL11.glStencilFunc(GL11.GL_ALWAYS, 10, ~0);
		GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);
		GL11.glEnable(GL11.GL_STENCIL_TEST);
		GlStateManager.enableAlpha();
		GlStateManager.alphaFunc(GL11.GL_GREATER, 0);
		GlStateManager.colorMask(false, false, false, true);
		GlStateManager.tryBlendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.SRC_ALPHA, DestFactor.ONE);

	}

	public void endRender(RenderingStringContext context) {
		endRender(() -> renderTexture(context), context.framebufferObject);
	}

	public void endRender(RenderingCharContext context) {
		endRender(() -> renderTexture(context), context.framebufferObject);
	}

	public void endRender(Runnable renderTexure, int framebufferObject) {
		Framebuffer framebuffer = formattingCodeStencilColor.framebuffer;
		GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
		GlStateManager.colorMask(true, true, true, true);
		GL11.glStencilFunc(GL11.GL_EQUAL, 0, ~0);
		GlStateManager.tryBlendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ONE_MINUS_SRC_ALPHA);
		GL11.glDisable(GL11.GL_STENCIL_TEST);
		boolean fog_enabled = GL11.glGetBoolean(GL11.GL_FOG);
		GlStateManager.disableFog();
		boolean depth_enabled = GL11.glGetBoolean(GL11.GL_DEPTH_TEST);
		GlStateManager.enableAlpha();
		GlStateManager.enableBlend();
		GlStateManager.disableDepth();
		GL11.glEnable(GL11.GL_STENCIL_TEST);
		GL11.glStencilFunc(GL11.GL_EQUAL, 10, ~0);
		GlStateManager.colorMask(true, true, true, false);
		GlStateManager.color(1, 1, 1, 1);
		int glTextureEnvMode = GL11.glGetTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE);
		GlStateManager.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);//これが無いと、SmoothFontと組み合わせたときに半透明の文字が真っ白になる
		renderTexure.run();

		GL11.glDisable(GL11.GL_STENCIL_TEST);

//		GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
//		GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, framebufferObject);
//		GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, framebuffer.framebufferObject);
//		int w = GLStateManagerUtil.viewportWidth;
//		int h = GLStateManagerUtil.viewportHeight;
//		GL30.glBlitFramebuffer(0, 0, w, h, 0, 0, w, h, GL11.GL_DEPTH_BUFFER_BIT, GL11.GL_NEAREST);

		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, framebufferObject);
		GlStateManager.matrixMode(GL11.GL_MODELVIEW);
		GlStateManager.pushMatrix();
		GlStateManager.matrixMode(GL11.GL_PROJECTION);
		GlStateManager.pushMatrix();
		GlStateManager.enableBlend();
		renderFrameBuffer(framebuffer);
		GlStateManager.matrixMode(GL11.GL_PROJECTION);
		GlStateManager.popMatrix();
		GlStateManager.matrixMode(GL11.GL_MODELVIEW);
		GlStateManager.popMatrix();
		if (depth_enabled)
			GlStateManager.enableDepth();
		if (fog_enabled)
			GlStateManager.enableFog();
		GlStateManager.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, glTextureEnvMode);//これが無いと、SmoothFontと組み合わせたときに半透明の文字が真っ白になる
	}

	//renderFrameBufferExt(false)からdisableDepthを抜いたもの
	private void renderFrameBuffer(Framebuffer framebuffer) {
		int width = Minecraft.getMinecraft().displayWidth;
		int height = Minecraft.getMinecraft().displayHeight;
		GlStateManager.colorMask(true, true, true, false);
		GlStateManager.depthMask(false);
		GlStateManager.matrixMode(5889);
		GlStateManager.loadIdentity();
		GlStateManager.ortho(0.0D, width, height, 0.0D, 1000.0D, 3000.0D);
		GlStateManager.matrixMode(5888);
		GlStateManager.loadIdentity();
		GlStateManager.translate(0.0F, 0.0F, -2000.0F);
		GlStateManager.viewport(0, 0, width, height);
		GlStateManager.enableTexture2D();
		GlStateManager.disableLighting();
		GlStateManager.disableAlpha();

		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		framebuffer.bindFramebufferTexture();
		float f = (float) width;
		float f1 = (float) height;
		float f2 = (float) framebuffer.framebufferWidth / (float) framebuffer.framebufferTextureWidth;
		float f3 = (float) framebuffer.framebufferHeight / (float) framebuffer.framebufferTextureHeight;
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
		bufferbuilder.pos(0.0D, f1, 0.0D).tex(0.0D, 0.0D).color(255, 255, 255, 255).endVertex();
		bufferbuilder.pos(f, f1, 0.0D).tex(f2, 0.0D).color(255, 255, 255, 255).endVertex();
		bufferbuilder.pos(f, 0.0D, 0.0D).tex(f2, f3).color(255, 255, 255, 255).endVertex();
		bufferbuilder.pos(0.0D, 0.0D, 0.0D).tex(0.0D, f3).color(255, 255, 255, 255).endVertex();
		tessellator.draw();
		framebuffer.unbindFramebufferTexture();
		GlStateManager.depthMask(true);
		GlStateManager.colorMask(true, true, true, true);
	}

}
