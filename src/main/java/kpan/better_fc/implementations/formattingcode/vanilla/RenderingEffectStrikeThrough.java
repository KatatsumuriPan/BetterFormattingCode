package kpan.better_fc.implementations.formattingcode.vanilla;

import kpan.better_fc.api.IRenderingEffectFancyStyle;
import kpan.better_fc.api.RenderFontUtil;
import kpan.better_fc.api.contexts.chara.RenderingCharContext;
import kpan.better_fc.asm.compat.CompatSmoothFont;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

public class RenderingEffectStrikeThrough implements IRenderingEffectFancyStyle {

	public static final RenderingEffectStrikeThrough INSTANCE = new RenderingEffectStrikeThrough();

	private RenderingEffectStrikeThrough() { }

	@Override
	public void postRender(RenderingCharContext context) {
		if (!RenderFontUtil.isSpace(context.charToRender)) {
			float left_x = context.posX;
			float right_x = left_x + context.nextRenderXOffset;
			float left_z = context.renderLeftBottomZ;
			float right_z = context.renderRightBottomZ;
			float y = context.centerY;
			if (CompatSmoothFont.isLoaded()) {
				context.stringContext.smoothFontIntegration.preStrikethroughRender();
			}
			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder bufferbuilder = tessellator.getBuffer();
			GlStateManager.disableTexture2D();
			bufferbuilder.begin(7, DefaultVertexFormats.POSITION);
			bufferbuilder.pos(left_x, y, left_z).endVertex();
			bufferbuilder.pos(right_x, y, right_z).endVertex();
			bufferbuilder.pos(right_x, y - 1.0F, right_z).endVertex();
			bufferbuilder.pos(left_x, y - 1.0F, left_z).endVertex();
			tessellator.draw();
			GlStateManager.enableTexture2D();
			if (CompatSmoothFont.isLoaded()) {
				context.stringContext.smoothFontIntegration.postStrikethroughRender();
			}
		}
	}

	@Override
	public int priority() { return 90000; }

}
