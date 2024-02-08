package kpan.better_fc.implementations.formattingcode.test;

import kpan.better_fc.api.FormattingCodeSimple;
import kpan.better_fc.api.IRenderingCharEffect;
import kpan.better_fc.util.StringReader;

import java.util.Collection;
import java.util.Map;

public class FormattingCodeRotate extends FormattingCodeSimple {
	public static final FormattingCodeRotate INSTANCE = new FormattingCodeRotate();
	private FormattingCodeRotate() {
		super("§<rot>");
	}
	@Override
	public boolean isValid(String option) {
		Map<String, String> map = parseMappedArg(option);
		if (map == null)
			return false;
		try {
			Double.parseDouble(map.getOrDefault("spd", "1"));
			Double.parseDouble(map.getOrDefault("phs", "0"));
		} catch (NumberFormatException e) {
			return false;
		}
		map.remove("spd");
		map.remove("phs");
		return map.isEmpty();
	}

	@Override
	public int getArgStringLength(StringReader stringReader) {
		int length = getMappedArgStringLength(stringReader);
		if (length != -1)
			return length;
		else
			return 0;
	}
	@Override
	protected void applyFormat(Collection<IRenderingCharEffect> effects, String option) {
		Map<String, String> map = parseMappedArg(option);
		double speed = Double.parseDouble(map.getOrDefault("spd", "1"));
		double phase = Double.parseDouble(map.getOrDefault("phs", "0"));
		effects.removeIf(e -> e instanceof RenderingEffectRotate);
		effects.add(new RenderingEffectRotate((float) speed, (float) phase));
	}
}
