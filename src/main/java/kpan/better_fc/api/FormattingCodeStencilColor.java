package kpan.better_fc.api;

import kpan.better_fc.util.GLStateManagerUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.shader.Framebuffer;

public abstract class FormattingCodeStencilColor extends FormattingCodeSimple {
	public Framebuffer framebuffer = null;
	protected FormattingCodeStencilColor(String formattingCode) {
		super(formattingCode);
	}

	protected abstract RenderingEffectStencilColor getEffect(RenderingEffects effects, String option);

	@Override
	protected void applyFormat(RenderingEffects effects, String option) {
		RenderingEffectStencilColor effect = getEffect(effects, option);
		effects.removeIf(e -> e instanceof IRenderingEffectColor);
		effects.add(effect);
	}

	public void init() {
		if (framebuffer == null || GLStateManagerUtil.viewportWidth != framebuffer.framebufferWidth || GLStateManagerUtil.viewportHeight != framebuffer.framebufferHeight) {
			int w = Math.max(GLStateManagerUtil.viewportWidth, Minecraft.getMinecraft().displayWidth);
			int h = Math.max(GLStateManagerUtil.viewportHeight, Minecraft.getMinecraft().displayHeight);
			if (framebuffer != null)
				framebuffer.deleteFramebuffer();
			framebuffer = new Framebuffer(w, h, true);
			framebuffer.setFramebufferColor(0.0F, 0.0F, 0.0F, 0.0F);
			framebuffer.enableStencil();
		}
	}
}
