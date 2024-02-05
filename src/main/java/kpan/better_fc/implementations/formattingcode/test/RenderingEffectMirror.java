package kpan.better_fc.implementations.formattingcode.test;

import kpan.better_fc.api.IRenderingCharEffect;
import kpan.better_fc.api.contexts.chara.RenderingCharContext;

public class RenderingEffectMirror implements IRenderingCharEffect {

	public static final RenderingEffectMirror INSTANCE = new RenderingEffectMirror();

	private RenderingEffectMirror() { }
	@Override
	public void preRender(RenderingCharContext context) {
		float temp = context.minU;
		context.minU = context.maxU;
		context.maxU = temp;
	}

	@Override
	public int priority() { return 130000; }
}
