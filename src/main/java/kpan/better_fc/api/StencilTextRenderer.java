package kpan.better_fc.api;

import kpan.better_fc.api.contexts.string.RenderingStringContext;
import kpan.better_fc.util.GLStateManagerUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import java.util.function.Consumer;

public class StencilTextRenderer implements IStringRenderEndListener {

	public Framebuffer framebuffer = null;
	public final Consumer<RenderingStringContext> renderer;
	public StencilTextRenderer(Consumer<RenderingStringContext> renderer) {
		this.renderer = renderer;
	}

	@Override
	public void onRenderEnd(RenderingStringContext context) {
		framebuffer.bindFramebuffer(false);
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
		renderer.accept(context);
		GL11.glDisable(GL11.GL_STENCIL_TEST);

		OpenGlHelper.glBindFramebuffer(OpenGlHelper.GL_FRAMEBUFFER, context.framebufferObject);
		GlStateManager.matrixMode(GL11.GL_MODELVIEW);
		GlStateManager.pushMatrix();
		GlStateManager.matrixMode(GL11.GL_PROJECTION);
		GlStateManager.pushMatrix();
		GlStateManager.enableBlend();
		renderFrameBuffer();
		GlStateManager.matrixMode(GL11.GL_PROJECTION);
		GlStateManager.popMatrix();
		GlStateManager.matrixMode(GL11.GL_MODELVIEW);
		GlStateManager.popMatrix();
		if (depth_enabled)
			GlStateManager.enableDepth();
		if (fog_enabled)
			GlStateManager.enableFog();
	}

	//renderFrameBufferExt(false)からdisableDepthを抜いたもの
	private void renderFrameBuffer() {
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

	public void clear(RenderingStringContext context) {
		if (framebuffer == null) {
			framebuffer = new Framebuffer(GLStateManagerUtil.viewportWidth, GLStateManagerUtil.viewportHeight, true);
			framebuffer.setFramebufferColor(0.0F, 0.0F, 0.0F, 0.0F);
			framebuffer.enableStencil();
		}
		framebuffer.framebufferClear();
		framebuffer.bindFramebuffer(false);
		GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
		GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, framebuffer.framebufferObject);
		GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, context.framebufferObject);
		int x = GLStateManagerUtil.viewportX;
		int y = GLStateManagerUtil.viewportY;
		int w = GLStateManagerUtil.viewportWidth;
		int h = GLStateManagerUtil.viewportHeight;
		GL30.glBlitFramebuffer(x, y, w, h, x, y, w, h, GL11.GL_DEPTH_BUFFER_BIT, GL11.GL_NEAREST);
		OpenGlHelper.glBindFramebuffer(OpenGlHelper.GL_FRAMEBUFFER, context.framebufferObject);
	}
}
