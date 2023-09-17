package kpan.better_fc.api;

import kpan.better_fc.api.contexts.string.FixingContext;
import kpan.better_fc.api.contexts.string.GetEffectsContext;
import kpan.better_fc.api.contexts.string.MeasuringStringWidthContext;
import kpan.better_fc.api.contexts.string.RenderingStringContext;
import kpan.better_fc.util.StringReader;

public interface IFormattingCode {
	boolean isValid(String option);
	void applyFormat(RenderingStringContext context, String option);
	void applyFormat(MeasuringStringWidthContext context, String option);
	void applyFormat(GetEffectsContext context, String option);
	default void applyFormat(FixingContext context, String option, int beginIndex, int endIndexExcl, StringBuilder sb) {
		sb.append(getFormatString(option));
	}
	default int getArgStringLength(StringReader stringReader) { return 0; }
	String getFormatString(String option);
}
