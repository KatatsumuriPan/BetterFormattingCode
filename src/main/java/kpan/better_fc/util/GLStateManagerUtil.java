package kpan.better_fc.util;

public class GLStateManagerUtil {

	public static int viewportX;
	public static int viewportY;
	public static int viewportWidth;
	public static int viewportHeight;
	public static void onSetViewportInternal(int x, int y, int width, int height) {
		viewportX = x;
		viewportY = y;
		viewportWidth = width;
		viewportHeight = height;
	}
}
