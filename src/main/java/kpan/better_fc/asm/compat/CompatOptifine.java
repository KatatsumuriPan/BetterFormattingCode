package kpan.better_fc.asm.compat;

public class CompatOptifine {
	private static final boolean loaded;

	static {
		boolean tmp;
		try {
			Class.forName("optifine.Patcher");
			tmp = true;
		} catch (ClassNotFoundException e) {
			tmp = false;
		}
		loaded = tmp;
	}

	public static boolean isOptifineLoaded() {
		return loaded;
	}
}
