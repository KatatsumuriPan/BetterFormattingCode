package kpan.better_fc.api;

import java.util.Comparator;
import kpan.better_fc.api.contexts.chara.MeasuringCharWidthContext;
import kpan.better_fc.api.contexts.chara.PreparingContext;
import kpan.better_fc.api.contexts.chara.RenderingCharContext;
import kpan.better_fc.api.contexts.string.FixingContext;
import kpan.better_fc.api.contexts.string.MeasuringStringWidthContext;
import kpan.better_fc.api.contexts.string.RenderingStringContext;

public interface IRenderingCharEffect {
	default boolean prepare(PreparingContext context) { return false; }

	/**
	 * Do not render in preRender!
	 *
	 * @param context
	 */
	default void preRender(RenderingCharContext context) { }
	default void postRender(RenderingCharContext context) { }
	default float onRenderingCancelled(RenderingStringContext context, char ch, float width) { return width; }
	default void first(MeasuringCharWidthContext context) { }
	default void second(MeasuringCharWidthContext context) { }
	default float onMeasuringCancelled(MeasuringStringWidthContext context, char ch, float width) { return width; }
	default void onFixingCancelled(FixingContext context, char ch) { }

	// higher -> earlier (inverted on postRender)
	int priority();

	Comparator<IRenderingCharEffect> COMPARATOR = Comparator.comparingInt(IRenderingCharEffect::priority).reversed();
}
