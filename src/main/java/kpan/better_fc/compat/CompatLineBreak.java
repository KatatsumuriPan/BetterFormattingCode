package kpan.better_fc.compat;

import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import kpan.b_line_break.LineBreakingUtil;
import kpan.better_fc.asm.compat.CompatBetterLineBreak;

public class CompatLineBreak {

	public static boolean canBreak(char prevChar, char c, int index, IntSet breakIndices) {
		if (!CompatBetterLineBreak.isLoaded())
			return false;
		return LineBreakingUtil.canBreak(prevChar, c, index, breakIndices);
	}
	public static IntSet phraseIndices(String str) {
		if (!CompatBetterLineBreak.isLoaded())
			return IntSets.EMPTY_SET;
		return LineBreakingUtil.phraseIndices(str, LineBreakingUtil.getParser());
	}

}
