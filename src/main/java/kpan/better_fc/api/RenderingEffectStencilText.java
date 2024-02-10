package kpan.better_fc.api;

import kpan.better_fc.api.contexts.chara.RenderingCharContext;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.opengl.GL11;

public abstract class RenderingEffectStencilText implements IRenderingEffectColor {

	public final Framebuffer framebuffer;
	protected RenderingEffectStencilText(Framebuffer framebuffer) { this.framebuffer = framebuffer; }

	@Override
	public void preRender(RenderingCharContext context) {
		if (context.asShadow) {
			context.red = 0.25f;
			context.green = 0.25f;
			context.blue = 0.25f;
		} else {
			context.red = 1;
			context.green = 1;
			context.blue = 1;
			GL11.glStencilFunc(GL11.GL_ALWAYS, 10, ~0);
			GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);
			GL11.glEnable(GL11.GL_STENCIL_TEST);
			GlStateManager.enableAlpha();
			GlStateManager.alphaFunc(GL11.GL_GREATER, 0);
			GlStateManager.colorMask(false, false, false, true);
			GlStateManager.tryBlendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.SRC_ALPHA, DestFactor.ONE);
			framebuffer.bindFramebuffer(false);
		}
	}

	@Override
	public void postRender(RenderingCharContext context) {
		if (context.asShadow)
			return;

		GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
		GlStateManager.colorMask(true, true, true, true);
		OpenGlHelper.glBindFramebuffer(OpenGlHelper.GL_FRAMEBUFFER, context.framebufferObject);
		GL11.glStencilFunc(GL11.GL_EQUAL, 0, ~0);
		GlStateManager.tryBlendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ONE_MINUS_SRC_ALPHA);
		GL11.glDisable(GL11.GL_STENCIL_TEST);

	}

}
