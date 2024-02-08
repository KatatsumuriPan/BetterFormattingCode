package kpan.better_fc.implementations.formattingcode.test;

import kpan.better_fc.api.IRenderingCharEffect;
import kpan.better_fc.api.contexts.chara.RenderingCharContext;
import kpan.better_fc.util.handlers.TickingClientHandler;
import net.minecraft.client.renderer.GlStateManager;

public class RenderingEffectRotate implements IRenderingCharEffect {

	public final float speed;
	public final float phase;
	public RenderingEffectRotate(float speed, float phase) {
		this.speed = speed;
		this.phase = phase;
	}
	@Override
	public void preRender(RenderingCharContext context) {
		float x = context.posX + (context.renderLeftTopX + context.renderRightTopX + context.renderLeftBottomX + context.renderRightBottomX) / 4;
		float y = context.centerY;
		double deg = phase * 360d + speed * TickingClientHandler.clientTime * 6;//speed=3で1秒1周
		deg %= 360;
		GlStateManager.translate(x, y, 0);
		GlStateManager.rotate(-(float) deg, 0, 0, 1);
		GlStateManager.translate(-x, -y, 0);
	}
	@Override
	public void postRender(RenderingCharContext context) {
		float x = context.posX + (context.renderLeftTopX + context.renderRightTopX + context.renderLeftBottomX + context.renderRightBottomX) / 4;
		float y = context.centerY;
		double deg = phase * 360d + speed * TickingClientHandler.clientTime * 6;//speed=3で1秒1周
		deg %= 360;
		GlStateManager.translate(x, y, 0);
		GlStateManager.rotate((float) deg, 0, 0, 1);
		GlStateManager.translate(-x, -y, 0);
	}
	@Override
	public int priority() { return -80000; }
}
