package kpan.better_fc.util;

import org.jetbrains.annotations.Nullable;

public class StringReader {

	private final String string;
	private int cursor;
	public StringReader(final StringReader other) {
		string = other.string;
		cursor = other.cursor;
	}
	public StringReader(final String string) {
		this.string = string;
		setCursor(0);
	}

	public String getString() {
		return string;
	}
	public void setCursor(int cursor) {
		this.cursor = cursor;
	}

	public void setCursorToEnd() {
		cursor = string.length();
	}

	public int getRemainingLength() {
		return string.length() - cursor;
	}

	public int getTotalLength() {
		return string.length();
	}

	public int getCursor() {
		return cursor;
	}

	public String getRead() {
		return string.substring(0, cursor);
	}

	public String getRemaining() {
		return string.substring(cursor);
	}

	public boolean canRead(int length) {
		return cursor + length <= string.length();
	}

	public boolean canRead() {
		return canRead(1);
	}

	public char peek() {
		return string.charAt(cursor);
	}

	public char peek(int offset) {
		return string.charAt(cursor + offset);
	}
	public String peeks(int length) {
		return string.substring(cursor, cursor + length);
	}
	public char read() {
		return string.charAt(cursor++);
	}
	public void skip() {
		cursor++;
	}
	public void skip(int count) {
		cursor += count;
	}

	public void skipWhitespace() {
		while (canRead() && Character.isWhitespace(peek())) {
			skip();
		}
	}

	@Nullable
	public String readToChar(char charInclusive) {
		int index = string.indexOf(charInclusive, cursor);
		if (index == -1)
			return null;
		return readTo(index + 1);
	}
	public String read(int length) {
		return readTo(cursor + length);
	}

	private String readTo(int indexExcl) {
		String res = string.substring(cursor, indexExcl);
		cursor = indexExcl;
		return res;
	}
}
