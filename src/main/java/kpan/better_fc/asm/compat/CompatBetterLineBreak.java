package kpan.better_fc.asm.compat;

public class CompatBetterLineBreak {
	private static final boolean loaded;

	static {
		boolean tmp;
		try {
			Class.forName("kpan.b_line_break.asm.core.AsmPlugin");
			tmp = true;
		} catch (ClassNotFoundException e) {
			tmp = false;
		}
		loaded = tmp;
	}

	public static boolean isLoaded() {
		return loaded;
	}
}
