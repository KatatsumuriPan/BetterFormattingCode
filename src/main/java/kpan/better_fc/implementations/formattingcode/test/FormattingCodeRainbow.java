package kpan.better_fc.implementations.formattingcode.test;

import kpan.better_fc.ModTagsGenerated;
import kpan.better_fc.api.FormattingCodeSimple;
import kpan.better_fc.api.IRenderingCharEffect;
import kpan.better_fc.api.IRenderingEffectColor;
import kpan.better_fc.api.StencilTextRenderer;
import kpan.better_fc.api.contexts.string.RenderingStringContext;
import kpan.better_fc.util.GLStateManagerUtil;
import kpan.better_fc.util.handlers.TickingClientHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.util.Collection;

public class FormattingCodeRainbow extends FormattingCodeSimple {
	public static final ResourceLocation RAINBOW = new ResourceLocation(ModTagsGenerated.MODGROUP.substring(ModTagsGenerated.MODGROUP.lastIndexOf('.') + 1), "textures/text/rainbow.png");
	public static final FormattingCodeRainbow INSTANCE = new FormattingCodeRainbow();
	public static final StencilTextRenderer RENDERER = new StencilTextRenderer(FormattingCodeRainbow::render);
	private FormattingCodeRainbow() {
		super("§<RAINBOW>");
	}
	@Override
	public boolean isValid(String option) {
		return true;
	}
	@Override
	protected void applyFormat(Collection<IRenderingCharEffect> effects, String option) {
		RenderingEffectRainbow effect = new RenderingEffectRainbow(RENDERER.framebuffer);
		effects.removeIf(e -> e instanceof IRenderingEffectColor);
		effects.add(effect);
	}

	@Override
	public void applyFormat(RenderingStringContext context, String option) {
		if (!context.listners.contains(RENDERER)) {
			context.listners.add(RENDERER);
			RENDERER.clear(context);
		}
		super.applyFormat(context, option);
	}

	public static void render(RenderingStringContext context) {
		int width = GLStateManagerUtil.viewportWidth;
		int height = GLStateManagerUtil.viewportHeight;
		float left = context.startX - width * 0.1F;
		float top = context.startY - height * 0.1F;
		float right = left + width * 1.2F;
		float bottom = top + height * 1.2F;
		render(left, right, top, bottom);
	}
	public static void render(float left, float right, float top, float bottom) {
		GlStateManager.enableTexture2D();
		Minecraft.getMinecraft().renderEngine.bindTexture(RAINBOW);
		int loops = 3;
		int period = 80;
		float t = (float) (TickingClientHandler.clientTime % period) / period;
		float u = t + (left * loops / (right - left)) % 1;
		float v = t + (top * loops / (bottom - top)) % 1;
		GlStateManager.glBegin(GL11.GL_TRIANGLE_STRIP);
		GlStateManager.glTexCoord2f(u, v);
		GlStateManager.glVertex3f(left, top, 0);
		GlStateManager.glTexCoord2f(u, loops + v);
		GlStateManager.glVertex3f(left, bottom, 0);
		GlStateManager.glTexCoord2f(loops + u, v);
		GlStateManager.glVertex3f(right, top, 0);
		GlStateManager.glTexCoord2f(loops + u, loops + v);
		GlStateManager.glVertex3f(right, bottom, 0);
		GlStateManager.glEnd();
	}
}
