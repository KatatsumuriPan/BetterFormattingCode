package kpan.better_fc.asm.hook;

import kpan.better_fc.util.GLStateManagerUtil;

public class HK_GlStateManager {
	public static void onSetViewport(int x, int y, int width, int height) {
		GLStateManagerUtil.onSetViewportInternal(x, y, width, height);
	}
}
