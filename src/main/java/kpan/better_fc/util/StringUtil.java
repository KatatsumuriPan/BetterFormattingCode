package kpan.better_fc.util;

import org.apache.commons.lang3.ArrayUtils;

public class StringUtil {
	public static int multipleIndexOf(String str, char... chars) {
		return multipleIndexOf(str, 0, chars);
	}
	public static int multipleIndexOf(String str, int startIndex, char... chars) {
		for (int i = startIndex; i < str.length(); i++) {
			if (ArrayUtils.contains(chars, str.charAt(i)))
				return i;
		}
		return -1;
	}
}
