package kpan.better_fc.implementations.formattingcode.test;

import kpan.better_fc.api.RenderingEffectStencilText;
import net.minecraft.client.shader.Framebuffer;

public class RenderingEffectRainbow extends RenderingEffectStencilText {

	public RenderingEffectRainbow(Framebuffer framebuffer) {
		super(framebuffer);
	}

	@Override
	public int priority() { return -100000; }
}
