package kpan.better_fc.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.function.Predicate;
import kpan.better_fc.api.contexts.chara.MeasuringCharWidthContext;
import kpan.better_fc.api.contexts.chara.PreparingContext;
import kpan.better_fc.api.contexts.chara.RenderingCharContext;
import kpan.better_fc.api.contexts.string.FixingContext;
import kpan.better_fc.api.contexts.string.MeasuringStringWidthContext;
import kpan.better_fc.api.contexts.string.RenderingStringContext;

public class RenderingEffects {

	public static final RenderingEffects EMPTY = new RenderingEffects();

	private final NavigableSet<IRenderingCharEffect> effects = new TreeSet<>(IRenderingCharEffect.COMPARATOR);

	public NavigableSet<IRenderingCharEffect> getEffects() { return effects; }

	public void add(IRenderingCharEffect effect) {
		effects.add(effect);
	}

	public void addAll(Collection<? extends IRenderingCharEffect> effects) {
		this.effects.addAll(effects);
	}

	public void clear() {
		effects.clear();
	}

	public boolean remove(IRenderingCharEffect o) {
		return effects.remove(o);
	}

	public boolean removeIf(Predicate<? super IRenderingCharEffect> filter) {
		return effects.removeIf(filter);
	}

	public void preRender(RenderingCharContext charContext) {
		for (IRenderingCharEffect effect : effects) {
			effect.preRender(charContext);
		}
	}

	public void postRender(RenderingCharContext charContext) {
		for (IRenderingCharEffect effect : effects.descendingSet()) {
			effect.postRender(charContext);
		}
	}

	public void measure(MeasuringCharWidthContext context) {
		for (IRenderingCharEffect effect : effects) {
			effect.first(context);
		}
		for (IRenderingCharEffect effect : effects) {
			effect.second(context);
		}
	}

	public boolean prepare(PreparingContext prepare) {
		for (IRenderingCharEffect effect : effects) {
			boolean cancelled = effect.prepare(prepare);
			if (cancelled)
				return true;
		}
		return false;
	}

	public float onRenderingCancelled(RenderingStringContext context, char ch) {
		float width = 0;
		for (IRenderingCharEffect effect : new ArrayList<>(effects)) {
			width = effect.onRenderingCancelled(context, ch, width);
		}
		return width;
	}

	public float onMeasuringCancelled(MeasuringStringWidthContext context, char ch) {
		float width = 0;
		for (IRenderingCharEffect effect : new ArrayList<>(effects)) {
			width = effect.onMeasuringCancelled(context, ch, width);
		}
		return width;
	}

	public void onFixingCancelled(FixingContext context, char ch) {
		for (IRenderingCharEffect effect : new ArrayList<>(effects)) {
			effect.onFixingCancelled(context, ch);
		}
	}
}
