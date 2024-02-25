package kpan.better_fc.api;

import java.util.HashMap;
import java.util.Map;
import kpan.better_fc.api.contexts.string.GetEffectsContext;
import kpan.better_fc.api.contexts.string.MeasuringStringWidthContext;
import kpan.better_fc.api.contexts.string.RenderingStringContext;
import kpan.better_fc.util.StringReader;
import org.jetbrains.annotations.Nullable;

public abstract class FormattingCodeSimple implements IFormattingCode {

	protected final String formattingCode;

	protected FormattingCodeSimple(String formattingCode) { this.formattingCode = formattingCode; }

	protected abstract void applyFormat(RenderingEffects effects, String option);

	@Override
	public void applyFormat(RenderingStringContext context, String option) {
		applyFormat(context.effects, option);
	}
	@Override
	public void applyFormat(MeasuringStringWidthContext context, String option) {
		applyFormat(context.effects, option);
	}
	@Override
	public void applyFormat(GetEffectsContext context, String option) {
		applyFormat(context.effects, option);
	}

	@Override
	public String getFormatString(String option) {
		return formattingCode + option;
	}

	public static int getMappedArgStringLength(StringReader stringReader) {
		if (!stringReader.canRead() || stringReader.peek() != '(')
			return -1;
		int length = stringReader.getRemainingLength();
		for (int i = 1; i < length; i++) {
			if (stringReader.peek(i) == ')')
				return i + 1;//今見た')'も含む
		}
		return length;
	}
	@Nullable
	public static Map<String, String> parseMappedArg(String text) {
		if (text.length() < 2)
			return null;
		if (text.charAt(0) != '(' || text.charAt(text.length() - 1) != ')')
			return null;
		Map<String, String> result = new HashMap<>();
		StringReader stringReader = new StringReader(text.substring(1, text.length() - 1));
		while (stringReader.canRead()) {
			String key = stringReader.tryReadToChar('=');
			if (key == null) {
				result.put(stringReader.getRemaining(), "");
				break;
			}
			String value = stringReader.tryReadToChar(',');
			if (value == null) {
				result.put(key, stringReader.getRemaining());
				break;
			}
			result.put(key, value);
		}
		return result;
	}
}
