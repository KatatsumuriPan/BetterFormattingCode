package kpan.better_fc.api;

import io.netty.util.collection.CharObjectHashMap;
import kpan.better_fc.util.StringReader;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;


public class FormattingCodesRegistry {
	private static final CharObjectHashMap<IFormattingCode> shortKeyMap = new CharObjectHashMap<>();
	private static final Map<String, IFormattingCode> longKeyMap = new HashMap<>();

	@Nullable
	public static String getKeyString(CharSequence charSequence, int index) {
		if (index >= charSequence.length())
			return null;
		char key = charSequence.charAt(index);
		if (key == '<') {
			if (charSequence instanceof StringBuilder sb) {
				int i = sb.indexOf(">", index);
				if (i == -1)
					return null;
				return sb.substring(index, i + 1);//<>を含む
			} else {
				String text = charSequence.toString();
				int i = text.indexOf('>', index);
				if (i == -1)
					return null;
				return text.substring(index, i + 1);//<>を含む
			}
		}
		return Character.toString(key);
	}
	@Nullable
	public static String getKeyString(StringReader stringReader) {
		if (!stringReader.canRead())
			return null;
		char key = stringReader.read();
		if (key == '<') {
			String long_key = stringReader.tryReadToChar('>');
			if (long_key == null)
				return null;
			return "<" + long_key;//<>を含む
		}
		return Character.toString(key);
	}
	@Nullable
	public static IFormattingCode get(CharSequence charSequence, int index) {
		String code_string = getKeyString(charSequence, index);
		if (code_string == null)
			return null;
		if (code_string.length() == 1)
			return shortKeyMap.get(code_string.charAt(0));
		else
			return longKeyMap.get(code_string);
	}
	@Nullable
	public static Pair<IFormattingCode, String> getCodeAndString(CharSequence charSequence, int index) {
		String code_string = getKeyString(charSequence, index);
		if (code_string == null)
			return null;
		if (code_string.length() == 1)
			return Pair.of(shortKeyMap.get(code_string.charAt(0)), code_string);
		else
			return Pair.of(longKeyMap.get(code_string), code_string);
	}
	@Nullable
	public static Pair<IFormattingCode, String> getCodeAndString(StringReader stringReader) {
		String code_string = getKeyString(stringReader);
		if (code_string == null)
			return null;
		if (code_string.length() == 1)
			return Pair.of(shortKeyMap.get(code_string.charAt(0)), code_string);
		else
			return Pair.of(longKeyMap.get(code_string), code_string);
	}

	public static void register(IFormattingCode code, char key) {
		if (key == '§')
			throw new IllegalArgumentException("You can't use § as a key!");
		if (key == '{')
			throw new IllegalArgumentException("You can't use { as a key!");
		if (key == '<')
			throw new IllegalArgumentException("You can't use < as a key!");
		if (shortKeyMap.containsKey(key))
			throw new IllegalStateException("The key '" + key + "' is already exists!");
		shortKeyMap.put(key, code);
	}

	public static void register(IFormattingCode code, char... keys) {
		for (char key : keys) {
			register(code, key);
		}
	}

	public static void register(IFormattingCode code, String key) {
		if (key.contains(">"))
			throw new IllegalStateException("You can't use a key that contains '>'!");
		if (longKeyMap.containsKey(key))
			throw new IllegalStateException("The key \"" + key + "\" is already exists!");
		longKeyMap.put("<" + key + ">", code);
	}

	@SuppressWarnings("unused")
	public static void unregister(char key) {
		if (key == 'r')
			throw new IllegalArgumentException("You can't unregister the reset code!");
		if (!shortKeyMap.containsKey(key))
			throw new IllegalStateException("The key '" + key + "' is not found!");
		shortKeyMap.remove(key);
	}

	@SuppressWarnings("unused")
	public static void unregister(String key) {
		if (!longKeyMap.containsKey(key))
			throw new IllegalStateException("The key '" + key + "' is not found!");
		longKeyMap.remove("<" + key + ">");
	}

}
