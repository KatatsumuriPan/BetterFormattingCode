package kpan.better_fc.api;

import net.minecraft.client.gui.FontRenderer;

public interface IRenderingEffectSingleColor extends IRenderingEffectColor {
	int getColor(FontRenderer fontRenderer);
}
