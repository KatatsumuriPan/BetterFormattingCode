package kpan.better_fc.api;

import kpan.better_fc.api.contexts.chara.MeasuringCharWidthContext;
import kpan.better_fc.api.contexts.chara.PreparingContext;
import kpan.better_fc.api.contexts.chara.RenderingCharContext;
import kpan.better_fc.api.contexts.string.FixingContext;
import kpan.better_fc.api.contexts.string.FormattingContext;
import kpan.better_fc.api.contexts.string.GetEffectsContext;
import kpan.better_fc.api.contexts.string.MeasuringStringWidthContext;
import kpan.better_fc.api.contexts.string.RenderingStringContext;
import kpan.better_fc.asm.compat.CompatOptifine;
import kpan.better_fc.compat.optifine.CompatFontRenderer;
import kpan.better_fc.util.CharArrayRingList;
import kpan.better_fc.util.ListUtil;
import kpan.better_fc.util.StringReader;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiUtilRenderComponents;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

public class RenderFontUtil {

	public static final float FONT_HEIGHT = 9;
	public static final float CHAR_HEIGHT = FONT_HEIGHT - 1 - 0.1f;
	public static final String ASCII_CHARS = "ÀÁÂÈÊËÍÓÔÕÚßãõğİıŒœŞşŴŵžȇ\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000ÇüéâäàåçêëèïîìÄÅÉæÆôöòûùÿÖÜø£Ø×ƒáíóúñÑªº¿®¬½¼¡«»░▒▓│┤╡╢╖╕╣║╗╝╜╛┐└┴┬├─┼╞╟╚╔╩╦╠═╬╧╨╤╥╙╘╒╓╫╪┘┌█▄▌▐▀αβΓπΣσμτΦΘΩδ∞∅∈∩≡±≥≤⌠⌡÷≈°∙·√ⁿ²■\u0000";
	private static String validDisplayStr = TextFormatting.ITALIC.toString() + TextFormatting.BOLD;
	private static String invalidDisplayStr = TextFormatting.UNDERLINE.toString();
	private static List<IRenderingCharEffect> validDisplay = null;
	private static List<IRenderingCharEffect> invalidDisplay = null;
	public static final List<IRenderingCharEffect> appliedEffects = new ArrayList<>();
	public static boolean isEditMode = false;

	public static List<IRenderingCharEffect> getValidDisplayEffects() {
		if (validDisplay == null)
			validDisplay = new ArrayList<>(getEffects(getValidDisplayStr()));
		return validDisplay;
	}
	public static List<IRenderingCharEffect> getInvalidDisplayEffects() {
		if (invalidDisplay == null)
			invalidDisplay = new ArrayList<>(getEffects(getInvalidDisplayStr()));
		return invalidDisplay;
	}
	public static String getValidDisplayStr() {
		return validDisplayStr;
	}
	public static void setValidDisplayStr(String validDisplayStr) {
		RenderFontUtil.validDisplayStr = validDisplayStr;
		validDisplay = null;
	}
	public static String getInvalidDisplayStr() {
		return invalidDisplayStr;
	}
	public static void setInvalidDisplayStr(String invalidDisplayStr) {
		RenderFontUtil.invalidDisplayStr = invalidDisplayStr;
		invalidDisplay = null;
	}

	public static class FormattedStringObject {
		//	String:=	Term*
		//	Term:=		Text|SectionSignTerm
		//	Text:=		[^(§|RangeEnd)]+
		//	SectionSignTerm:=	SingleSectionSign|EscapedSectionSign|FormattingCode
		//	SingleSectionSign:=	§($|RangeEnd)
		//	EscapedSectionSign:=	§§
		//	FormattingRange:=	§{Term*}
		//	FormattingCode:=	§([^§<]|<.*?>)Options
		private final int beginIndex;
		private final int endIndexExcl;
		private final FormattedStringObject.Type type;

		@Nullable
		private final Pair<String, IFormattingCode> pair;
		private final String option;
		@Nullable
		private FormattedStringObject next = null;
		private final int markerNum;
		@Nullable
		private final FormattedStringObject component;//空ならばnull
		private static final FormattedStringObject EMPTY = new FormattedStringObject(0, 0, Type.TEXT);

		private FormattedStringObject(int beginIndex, int endIndexExcl, Type type) {
			this(beginIndex, endIndexExcl, type, null, "");
		}
		private FormattedStringObject(int beginIndex, int endIndexExcl, Type type, @Nullable Pair<String, IFormattingCode> pair, String option) {
			this(beginIndex, endIndexExcl, type, pair, option, -1, null);
		}
		public FormattedStringObject(int beginIndex, int endIndexExcl, Type type, @Nullable Pair<String, IFormattingCode> pair, String option, int markerNum, @Nullable FormattedStringObject component) {
			this.beginIndex = beginIndex;
			this.endIndexExcl = endIndexExcl;
			this.type = type;
			this.pair = pair;
			this.option = option;
			this.markerNum = markerNum;
			this.component = component;
		}

		private static FormattedStringObject text(int beginIndex, int endIndexExcl) {
			return new FormattedStringObject(beginIndex, endIndexExcl, Type.TEXT);
		}
		private static FormattedStringObject singleSectionSign(int beginIndex) {
			return new FormattedStringObject(beginIndex, beginIndex + 1, Type.SINGLE_SECTION_SIGN);
		}
		private static FormattedStringObject escapedSectionSign(int beginIndex) {
			return new FormattedStringObject(beginIndex, beginIndex + 2, Type.ESCAPED_SECTION_SIGN);
		}
		private static FormattedStringObject unterminatedLongKey(int beginIndex, int endIndexExcl) {
			return new FormattedStringObject(beginIndex, endIndexExcl, Type.FORMATTING_CODE_UNTERMINATED_LONGKEY);
		}
		private static FormattedStringObject unknownFormattingCode(int beginIndex, int endIndexExcl) {
			return new FormattedStringObject(beginIndex, endIndexExcl, Type.FORMATTING_CODE_UNKNOWN);
		}
		private static FormattedStringObject formattingCode(int beginIndex, int endIndexExcl, Pair<String, IFormattingCode> pair, String argument) {
			return new FormattedStringObject(beginIndex, endIndexExcl, Type.FORMATTING_CODE, pair, argument);
		}
		private static FormattedStringObject formattingRange(int beginIndex, int endIndexExcl, int markerNum, @Nullable FormattedStringObject component) {
			return new FormattedStringObject(beginIndex, endIndexExcl, Type.FORMATTING_RANGE, null, "", markerNum, component);
		}
		private static FormattedStringObject formattingRangeUnterminated(int beginIndex, int endIndexExcl, int markerNum, @Nullable FormattedStringObject component) {
			return new FormattedStringObject(beginIndex, endIndexExcl, Type.FORMATTING_RANGE_UNTERMINATED, null, "", markerNum, component);
		}


		public static FormattedStringObject parse(String text) {
			if (text.isEmpty())
				return EMPTY;
			StringReader stringReader = new StringReader(text);
			FormattedStringObject first = null;
			FormattedStringObject last = null;
			while (stringReader.canRead()) {
				FormattedStringObject obj = parseTerm(stringReader, null);
				if (first == null) {
					first = obj;
					last = obj;
				} else {
					last.next = obj;
					last = obj;
				}
			}
			return first;
		}
		private static FormattedStringObject parseTerm(StringReader stringReader, @Nullable String rangeEnd) {
			if (stringReader.peek() == '§')
				return parseSectionSignTerm(stringReader, rangeEnd);
			else
				return parseText(stringReader, rangeEnd);
		}
		private static FormattedStringObject parseText(StringReader stringReader, @Nullable String rangeEnd) {
			int begin = stringReader.getCursor();
			while (stringReader.canRead() && stringReader.peek() != '§') {
				if (isRangeEnd(stringReader, rangeEnd))
					break;
				stringReader.skip();
			}
			return FormattedStringObject.text(begin, stringReader.getCursor());
		}
		private static FormattedStringObject parseSectionSignTerm(StringReader stringReader, @Nullable String rangeEnd) {
			stringReader.skip();
			if (!stringReader.canRead() || isRangeEnd(stringReader, rangeEnd))
				return FormattedStringObject.singleSectionSign(stringReader.getCursor() - 1);
			if (stringReader.peek() == '§') {
				stringReader.skip();
				return FormattedStringObject.escapedSectionSign(stringReader.getCursor() - 2);
			}
			if (stringReader.peek() == '{') {
				return parseFormattingRange(stringReader);
			}
			return parseFormattingCode(stringReader);
		}
		private static FormattedStringObject parseFormattingRange(StringReader stringReader) {
			int begin = stringReader.getCursor() - 1;
			int markerNum = 0;
			while (stringReader.canRead() && stringReader.peek() == '{') {
				stringReader.skip();
				markerNum++;
			}
			if (!stringReader.canRead())
				return FormattedStringObject.formattingRangeUnterminated(begin, stringReader.getCursor(), markerNum, null);
			String end = StringUtils.repeat('}', markerNum);
			FormattedStringObject first = null;
			FormattedStringObject last = null;
			while (stringReader.canRead() && !isRangeEnd(stringReader, end)) {
				FormattedStringObject obj = parseTerm(stringReader, end);
				if (first == null) {
					first = obj;
					last = obj;
				} else {
					last.next = obj;
					last = obj;
				}
			}
			if (!stringReader.canRead())
				return FormattedStringObject.formattingRangeUnterminated(begin, stringReader.getCursor(), markerNum, first);
			stringReader.skip(markerNum);
			return FormattedStringObject.formattingRange(begin, stringReader.getCursor(), markerNum, first);
		}
		private static FormattedStringObject parseFormattingCode(StringReader stringReader) {
			int begin = stringReader.getCursor() - 1;
			Pair<IFormattingCode, String> swap = FormattingCodesRegistry.getCodeAndString(stringReader);
			if (swap == null) {
				stringReader.setCursorToEnd();
				return FormattedStringObject.unterminatedLongKey(begin, stringReader.getTotalLength());
			}
			Pair<String, IFormattingCode> pair = Pair.of(swap.getValue(), swap.getKey());
			IFormattingCode formattingCode = pair.getValue();
			if (formattingCode == null)
				return FormattedStringObject.unknownFormattingCode(begin, stringReader.getCursor());
			String option = stringReader.read(formattingCode.getArgStringLength(stringReader));
			return FormattedStringObject.formattingCode(begin, stringReader.getCursor(), pair, option);
		}
		private static boolean isRangeEnd(StringReader stringReader, @Nullable String rangeEnd) {
			return rangeEnd != null && stringReader.canRead(rangeEnd.length()) && stringReader.peeks(rangeEnd.length()).equals(rangeEnd);
		}

		public static float render(FontRenderer fontRenderer, String text, int beginIndex, int endIndex, float posX, float posY, boolean asShadow) {
			RenderingStringContext context = new RenderingStringContext(fontRenderer, text, posX, posY, isEditMode, asShadow, GL11.glGetInteger(GL30.GL_FRAMEBUFFER_BINDING));
			context.effects.addAll(appliedEffects);
			FormattedStringObject.parse(text).render(context, beginIndex, endIndex);
			context.onRenderEnd();
			return context.posX;
		}
		private void render(RenderingStringContext context, int wholeBeginIndex, int wholeEndIndexExcl) {
			switch (type) {
				case TEXT -> {
					for (int i = Math.max(beginIndex, wholeBeginIndex); i < endIndexExcl; i++) {
						if (i >= wholeEndIndexExcl)
							return;
						char ch = context.originalText.charAt(i);
						if (context.isEdit) {
							context.posX += prepareAndRenderChar(context, ch);
						} else {
							CharArrayRingList ringList = context.getRingList();
							ringList.addLast(ch);
							while (!ringList.isEmpty()) {
								context.posX += prepareAndRenderChar(context, ringList.pollFirst());
							}
						}
					}
				}
				case SINGLE_SECTION_SIGN -> {
					if (beginIndex >= wholeEndIndexExcl)
						return;
					GlStateManager.color(context.fontRenderer.red, context.fontRenderer.green, context.fontRenderer.blue, context.fontRenderer.alpha);
					//§の次が無い
					if (context.isEdit)
						context.posX += renderChar(context.fontRenderer, '§', context.posX, context.posY, getInvalidDisplayEffects(), context.asShadow);
					else
						context.posX += renderChar(context.fontRenderer, '§', context.posX, context.posY, Collections.emptyList(), context.asShadow);
				}
				case ESCAPED_SECTION_SIGN -> {
					if (beginIndex >= wholeEndIndexExcl)
						return;
					if (context.isEdit) {
						GlStateManager.color(context.fontRenderer.red, context.fontRenderer.green, context.fontRenderer.blue, context.fontRenderer.alpha);
						context.posX += renderChar(context.fontRenderer, '§', context.posX, context.posY, getValidDisplayEffects(), context.asShadow);
						if (beginIndex + 1 >= wholeEndIndexExcl)
							return;
						context.posX += renderChar(context.fontRenderer, '§', context.posX, context.posY, getValidDisplayEffects(), context.asShadow);
					} else {
						context.posX += prepareAndRenderChar(context, '§');
					}
				}
				case FORMATTING_CODE_UNTERMINATED_LONGKEY, FORMATTING_CODE_UNKNOWN -> {
					if (renderInvalid(context, wholeBeginIndex, wholeEndIndexExcl))
						return;
				}
				case FORMATTING_CODE -> {
					if (pair.getValue().isValid(option)) {
						pair.getValue().applyFormat(context, option);
						if (context.isEdit) {
							GlStateManager.color(context.fontRenderer.red, context.fontRenderer.green, context.fontRenderer.blue, context.fontRenderer.alpha);
							for (int i = Math.max(beginIndex, wholeBeginIndex); i < endIndexExcl; i++) {
								if (i >= wholeEndIndexExcl)
									return;
								context.posX += renderChar(context.fontRenderer, context.originalText.charAt(i), context.posX, context.posY, getValidDisplayEffects(), context.asShadow);
							}
						} else {
							CharArrayRingList ringList = context.getRingList();
							while (!ringList.isEmpty()) {
								char c1 = ringList.pollFirst();
								context.posX += prepareAndRenderChar(context, c1);
							}
						}
					} else {
						if (renderInvalid(context, wholeBeginIndex, wholeEndIndexExcl))
							return;
					}
				}
				case FORMATTING_RANGE, FORMATTING_RANGE_UNTERMINATED -> {
					Collection<IRenderingCharEffect> effects_before = new ArrayList<>(context.effects);
					if (context.isEdit) {
						GlStateManager.color(context.fontRenderer.red, context.fontRenderer.green, context.fontRenderer.blue, context.fontRenderer.alpha);
						for (int i = Math.max(beginIndex, wholeBeginIndex); i < beginIndex + 1 + markerNum; i++) {
							if (i >= wholeEndIndexExcl)
								return;
							context.posX += renderChar(context.fontRenderer, context.originalText.charAt(i), context.posX, context.posY, getValidDisplayEffects(), context.asShadow);
						}
					} else {
						if (type == Type.FORMATTING_RANGE_UNTERMINATED) {
							GlStateManager.color(context.fontRenderer.red, context.fontRenderer.green, context.fontRenderer.blue, context.fontRenderer.alpha);
							for (int i = Math.max(beginIndex, wholeBeginIndex); i < beginIndex + 1 + markerNum; i++) {
								if (i >= wholeEndIndexExcl)
									return;
								context.posX += renderChar(context.fontRenderer, context.originalText.charAt(i), context.posX, context.posY, getValidDisplayEffects(), context.asShadow);
							}
						}
					}
					if (component != null)
						component.render(context, wholeBeginIndex, wholeEndIndexExcl);
					context.effects.clear();
					context.effects.addAll(effects_before);
					if (context.isEdit) {
						if (type == Type.FORMATTING_RANGE) {
							for (int i = endIndexExcl - markerNum; i < endIndexExcl; i++) {
								if (i >= wholeEndIndexExcl)
									return;
								context.posX += renderChar(context.fontRenderer, context.originalText.charAt(i), context.posX, context.posY, getValidDisplayEffects(), context.asShadow);
							}
						}
					}
				}
			}
			if (next != null && endIndexExcl < wholeEndIndexExcl)
				next.render(context, wholeBeginIndex, wholeEndIndexExcl);
		}
		private boolean renderInvalid(RenderingStringContext context, int wholeBeginIndex, int wholeEndIndexExcl) {
			GlStateManager.color(context.fontRenderer.red, context.fontRenderer.green, context.fontRenderer.blue, context.fontRenderer.alpha);
			for (int i = Math.max(beginIndex, wholeBeginIndex); i < endIndexExcl; i++) {
				if (i >= wholeEndIndexExcl)
					return true;
				context.posX += renderChar(context.fontRenderer, context.originalText.charAt(i), context.posX, context.posY, getInvalidDisplayEffects(), context.asShadow);
			}
			return false;
		}

		public MeasuringResult measure(MeasuringStringWidthContext context, int wholeBeginIndex, int wholeEndIndexExcl, float limitWidthIncl) {
			MeasuringResult result = measure(context, wholeBeginIndex, wholeEndIndexExcl, limitWidthIncl, 0);
			return result;
		}
		private MeasuringResult measure(MeasuringStringWidthContext context, int wholeBeginIndex, int wholeEndIndexExcl, float limitWidthIncl, float currentWidth) {
			switch (type) {
				case TEXT -> {
					for (int i = Math.max(beginIndex, wholeBeginIndex); i < endIndexExcl; i++) {
						if (i >= wholeEndIndexExcl)
							return new MeasuringResult(currentWidth, i);
						char ch = context.originalText.charAt(i);
						if (context.isEdit) {
							float w = prepareAndGetCharWidth(context, ch);
							if (currentWidth + w > limitWidthIncl)
								return new MeasuringResult(currentWidth, i);
							currentWidth += w;
						} else {
							CharArrayRingList ringList = context.getRingList();
							ringList.addLast(ch);
							while (!ringList.isEmpty()) {
								float w = prepareAndGetCharWidth(context, ringList.pollFirst());
								if (currentWidth + w > limitWidthIncl)
									return new MeasuringResult(currentWidth, i);
								currentWidth += w;
							}
						}
					}
				}
				case SINGLE_SECTION_SIGN -> {
					if (beginIndex >= wholeEndIndexExcl)
						return new MeasuringResult(currentWidth, beginIndex);
					//§の次が無い
					float w;
					if (context.isEdit)
						w = getCharWidthWithSpace(context.fontRenderer, '§', getInvalidDisplayEffects());
					else
						w = getCharWidthWithSpace(context.fontRenderer, '§', Collections.emptyList());
					if (currentWidth + w > limitWidthIncl)
						return new MeasuringResult(currentWidth, beginIndex);
					currentWidth += w;
				}
				case ESCAPED_SECTION_SIGN -> {
					if (beginIndex >= wholeEndIndexExcl)
						return new MeasuringResult(currentWidth, beginIndex);
					if (context.isEdit) {
						float w = getCharWidthWithSpace(context.fontRenderer, '§', getValidDisplayEffects());
						if (currentWidth + w > limitWidthIncl)
							return new MeasuringResult(currentWidth, beginIndex);
						currentWidth += w;
						if (beginIndex + 1 >= wholeEndIndexExcl)
							return new MeasuringResult(currentWidth, beginIndex + 1);
						if (currentWidth + w > limitWidthIncl)
							return new MeasuringResult(currentWidth, beginIndex + 1);
						currentWidth += w;
					} else {
						float w = prepareAndGetCharWidth(context, '§');
						if (currentWidth + w > limitWidthIncl)
							return new MeasuringResult(currentWidth, beginIndex);
						currentWidth += w;
					}
				}
				case FORMATTING_CODE_UNTERMINATED_LONGKEY, FORMATTING_CODE_UNKNOWN -> {
					for (int i = Math.max(beginIndex, wholeBeginIndex); i < endIndexExcl; i++) {
						if (i >= wholeEndIndexExcl)
							return new MeasuringResult(currentWidth, i);
						float w = getCharWidthWithSpace(context.fontRenderer, context.originalText.charAt(i), getInvalidDisplayEffects());
						if (currentWidth + w > limitWidthIncl)
							return new MeasuringResult(currentWidth, i);
						currentWidth += w;
					}
				}
				case FORMATTING_CODE -> {
					if (pair.getValue().isValid(option)) {
						pair.getValue().applyFormat(context, option);
						if (context.isEdit) {
							for (int i = Math.max(beginIndex, wholeBeginIndex); i < endIndexExcl; i++) {
								if (i >= wholeEndIndexExcl)
									return new MeasuringResult(currentWidth, i);
								float w = getCharWidthWithSpace(context.fontRenderer, context.originalText.charAt(i), getValidDisplayEffects());
								if (currentWidth + w > limitWidthIncl)
									return new MeasuringResult(currentWidth, i);
								currentWidth += w;
							}
						} else {
							CharArrayRingList ringList = context.getRingList();
							while (!ringList.isEmpty()) {
								float w = prepareAndGetCharWidth(context, ringList.pollFirst());
								if (currentWidth + w > limitWidthIncl)
									return new MeasuringResult(currentWidth, endIndexExcl);
								currentWidth += w;
							}
						}
					} else {
						for (int i = Math.max(beginIndex, wholeBeginIndex); i < endIndexExcl; i++) {
							if (i >= wholeEndIndexExcl)
								return new MeasuringResult(currentWidth, i);
							float w = getCharWidthWithSpace(context.fontRenderer, context.originalText.charAt(i), getInvalidDisplayEffects());
							if (currentWidth + w > limitWidthIncl)
								return new MeasuringResult(currentWidth, i);
							currentWidth += w;
						}
					}
				}
				case FORMATTING_RANGE, FORMATTING_RANGE_UNTERMINATED -> {
					Collection<IRenderingCharEffect> effects_before = new ArrayList<>(context.effects);
					if (context.isEdit) {
						for (int i = Math.max(beginIndex, wholeBeginIndex); i < beginIndex + 1 + markerNum; i++) {
							if (i >= wholeEndIndexExcl)
								return new MeasuringResult(currentWidth, i);
							float w = getCharWidthWithSpace(context.fontRenderer, context.originalText.charAt(i), getValidDisplayEffects());
							if (currentWidth + w > limitWidthIncl)
								return new MeasuringResult(currentWidth, i);
							currentWidth += w;
						}
					} else {
						if (type == Type.FORMATTING_RANGE_UNTERMINATED) {
							for (int i = Math.max(beginIndex, wholeBeginIndex); i < beginIndex + 1 + markerNum; i++) {
								if (i >= wholeEndIndexExcl)
									return new MeasuringResult(currentWidth, i);
								float w = getCharWidthWithSpace(context.fontRenderer, context.originalText.charAt(i), getValidDisplayEffects());
								if (currentWidth + w > limitWidthIncl)
									return new MeasuringResult(currentWidth, i);
								currentWidth += w;
							}
						}
					}
					if (component != null) {
						MeasuringResult result = component.measure(context, wholeBeginIndex, wholeEndIndexExcl, limitWidthIncl, currentWidth);
						if (result.lastIndexExc != -1)
							return result;
						currentWidth = result.totalWidth;
					}
					context.effects.clear();
					context.effects.addAll(effects_before);
					if (context.isEdit) {
						if (type == Type.FORMATTING_RANGE) {
							for (int i = endIndexExcl - markerNum; i < endIndexExcl; i++) {
								if (i >= wholeEndIndexExcl)
									return new MeasuringResult(currentWidth, i);
								float w = getCharWidthWithSpace(context.fontRenderer, context.originalText.charAt(i), getValidDisplayEffects());
								if (currentWidth + w > limitWidthIncl)
									return new MeasuringResult(currentWidth, i);
								currentWidth += w;
							}
						}
					}
				}
			}
			if (next != null && endIndexExcl < wholeEndIndexExcl)
				return next.measure(context, wholeBeginIndex, wholeEndIndexExcl, limitWidthIncl, currentWidth);
			else
				return new MeasuringResult(currentWidth, -1);
		}

		public static float[] getWidths(String text, MeasuringStringWidthContext context, boolean reverse) {
			float[] widths = new float[text.length()];
			parse(text).getWidths(widths, context, reverse);
			return widths;
		}
		private void getWidths(float[] widths, MeasuringStringWidthContext context, boolean reverse) {
			switch (type) {
				case TEXT -> {
					for (int i = beginIndex; i < endIndexExcl; i++) {
						char ch = context.originalText.charAt(i);
						if (context.isEdit) {
							float w = prepareAndGetCharWidth(context, ch);
							widths[i] = w;
						} else {
							float total_width = 0;
							CharArrayRingList ringList = context.getRingList();
							ringList.addLast(ch);
							while (!ringList.isEmpty()) {
								float w = prepareAndGetCharWidth(context, ringList.pollFirst());
								total_width += w;
							}
							widths[i] = total_width;
						}
					}
				}
				case SINGLE_SECTION_SIGN -> {
					//§の次が無い
					float w;
					if (context.isEdit)
						w = getCharWidthWithSpace(context.fontRenderer, '§', getInvalidDisplayEffects());
					else
						w = getCharWidthWithSpace(context.fontRenderer, '§', Collections.emptyList());
					widths[beginIndex] = w;
				}
				case ESCAPED_SECTION_SIGN -> {
					if (context.isEdit) {
						float w = getCharWidthWithSpace(context.fontRenderer, '§', getValidDisplayEffects());
						widths[beginIndex] = w;
						widths[beginIndex + 1] = w;
					} else {
						float w = prepareAndGetCharWidth(context, '§');
						if (reverse)
							widths[beginIndex] = w;
						else
							widths[endIndexExcl - 1] = w;
					}
				}
				case FORMATTING_CODE_UNTERMINATED_LONGKEY, FORMATTING_CODE_UNKNOWN -> {
					for (int i = beginIndex; i < endIndexExcl; i++) {
						float w = getCharWidthWithSpace(context.fontRenderer, context.originalText.charAt(i), getInvalidDisplayEffects());
						widths[i] = w;
					}
				}
				case FORMATTING_CODE -> {
					if (pair.getValue().isValid(option)) {
						pair.getValue().applyFormat(context, option);
						if (context.isEdit) {
							for (int i = beginIndex; i < endIndexExcl; i++) {
								float w = getCharWidthWithSpace(context.fontRenderer, context.originalText.charAt(i), getValidDisplayEffects());
								widths[i] = w;
							}
						} else {
							float total_width = 0;
							CharArrayRingList ringList = context.getRingList();
							while (!ringList.isEmpty()) {
								float w = prepareAndGetCharWidth(context, ringList.pollFirst());
								total_width += w;
							}
							if (reverse)
								widths[beginIndex] = total_width;
							else
								widths[endIndexExcl - 1] = total_width;
						}
					} else {
						for (int i = beginIndex; i < endIndexExcl; i++) {
							float w = getCharWidthWithSpace(context.fontRenderer, context.originalText.charAt(i), getInvalidDisplayEffects());
							widths[i] = w;
						}
					}
				}
				case FORMATTING_RANGE, FORMATTING_RANGE_UNTERMINATED -> {
					Collection<IRenderingCharEffect> effects_before = new ArrayList<>(context.effects);
					if (context.isEdit) {
						for (int i = beginIndex; i < beginIndex + 1 + markerNum; i++) {
							float w = getCharWidthWithSpace(context.fontRenderer, context.originalText.charAt(i), getValidDisplayEffects());
							widths[i] = w;
						}
					} else {
						if (type == Type.FORMATTING_RANGE_UNTERMINATED) {
							float total_width = 0;
							for (int i = beginIndex; i < beginIndex + 1 + markerNum; i++) {
								float w = getCharWidthWithSpace(context.fontRenderer, context.originalText.charAt(i), getValidDisplayEffects());
								total_width += w;
							}
							if (reverse)
								widths[beginIndex + 2] = total_width;
							else
								widths[beginIndex] = total_width;
						}
					}
					if (component != null)
						component.getWidths(widths, context, reverse);
					context.effects.clear();
					context.effects.addAll(effects_before);
					if (context.isEdit) {
						if (type == Type.FORMATTING_RANGE) {
							for (int i = endIndexExcl - markerNum; i < endIndexExcl; i++) {
								float w = getCharWidthWithSpace(context.fontRenderer, context.originalText.charAt(i), getValidDisplayEffects());
								widths[i] = w;
							}
						}
					}
				}
			}
			if (next != null)
				next.getWidths(widths, context, reverse);
		}

		public static String getFormatFromString(FormattingContext context) {
			StringBuilder sb = new StringBuilder();
			parse(context.originalText).getFormatFromString(context, sb);
			return sb.toString();
		}
		private void getFormatFromString(FormattingContext context, StringBuilder sb) {
			switch (type) {
				case TEXT, SINGLE_SECTION_SIGN, ESCAPED_SECTION_SIGN, FORMATTING_CODE_UNTERMINATED_LONGKEY,
						FORMATTING_CODE_UNKNOWN, FORMATTING_RANGE_UNTERMINATED -> {
				}
				case FORMATTING_CODE -> {
					if (pair.getValue().isValid(option))
						sb.append(pair.getValue().getFormatString(option));
					else
						sb.append(context.originalText, beginIndex, endIndexExcl);
				}
				case FORMATTING_RANGE -> {
					sb.append('§');
					sb.append(StringUtils.repeat('{', markerNum));
					if (component != null)
						component.getFormatFromString(context, sb);
					sb.append(StringUtils.repeat('}', markerNum));
				}
			}
			if (next != null)
				next.getFormatFromString(context, sb);
		}

		public static String removeFormat(FormattingContext context) {
			StringBuilder sb = new StringBuilder();
			parse(context.originalText).removeFormat(sb, context);
			return sb.toString();
		}
		private void removeFormat(StringBuilder sb, FormattingContext context) {
			switch (type) {
				case TEXT, SINGLE_SECTION_SIGN, ESCAPED_SECTION_SIGN, FORMATTING_CODE_UNTERMINATED_LONGKEY, FORMATTING_CODE_UNKNOWN -> {
					sb.append(context.originalText, beginIndex, endIndexExcl);
				}
				case FORMATTING_CODE -> {
				}
				case FORMATTING_RANGE, FORMATTING_RANGE_UNTERMINATED -> {
					if (component != null)
						component.removeFormat(sb, context);
				}
			}
			if (next != null)
				next.removeFormat(sb, context);
		}

		public static Collection<IRenderingCharEffect> getEffects(String text) {
			GetEffectsContext context = new GetEffectsContext(text, isEditMode);
			parse(context.originalText).getEffects(context);
			return context.effects;
		}
		private void getEffects(GetEffectsContext context) {
			switch (type) {
				case TEXT, SINGLE_SECTION_SIGN, ESCAPED_SECTION_SIGN, FORMATTING_CODE_UNTERMINATED_LONGKEY,
						FORMATTING_CODE_UNKNOWN, FORMATTING_RANGE_UNTERMINATED -> {
				}
				case FORMATTING_CODE -> {
					if (pair.getValue().isValid(option))
						pair.getValue().applyFormat(context, option);
				}
				case FORMATTING_RANGE -> {
					//Range内部での変更は外に漏れない
				}
			}
			if (next != null)
				next.getEffects(context);
		}

		public static String fix(FixingContext context) {
			StringBuilder sb = new StringBuilder();
			parse(context.originalText).fix(context, sb);
			return sb.toString();
		}
		private void fix(FixingContext context, StringBuilder sb) {
			switch (type) {
				case TEXT, ESCAPED_SECTION_SIGN -> {
					if (context.isEdit) {
						sb.append(context.originalText, beginIndex, endIndexExcl);
					} else {
						CharArrayRingList ringList = context.getRingList();
						for (int i = beginIndex; i < endIndexExcl; i++) {
							char ch = context.originalText.charAt(i);
							ringList.addLast(ch);
							while (!ringList.isEmpty()) {
								sb.append(prepareFix(context, ringList.pollFirst()));
							}
						}
					}
				}
				case SINGLE_SECTION_SIGN, FORMATTING_CODE_UNTERMINATED_LONGKEY, FORMATTING_CODE_UNKNOWN -> {
					sb.append(context.originalText, beginIndex, endIndexExcl);
				}
				case FORMATTING_CODE -> {
					if (pair.getValue().isValid(option))
						pair.getValue().applyFormat(context, option, beginIndex, endIndexExcl, sb);
					else
						sb.append(context.originalText, beginIndex, endIndexExcl);
				}
				case FORMATTING_RANGE, FORMATTING_RANGE_UNTERMINATED -> {
					Collection<IRenderingCharEffect> effects_before = new ArrayList<>(context.effects);
					sb.append('§');
					sb.append(StringUtils.repeat('{', markerNum));
					if (component != null)
						component.fix(context, sb);
					context.effects.clear();
					context.effects.addAll(effects_before);
					if (type == Type.FORMATTING_RANGE)
						sb.append(StringUtils.repeat('}', markerNum));
				}
			}
			if (next != null)
				next.fix(context, sb);
		}
		@Nullable
		private static Character prepareFix(FixingContext context, char ch) {
			PreparingContext prepare = new PreparingContext(context.fontRenderer, ch, context.isEdit, true, context.originalText, context.tryGetRingList(), context.effects);
			boolean cancelled = false;
			for (IRenderingCharEffect effect : context.effects) {
				cancelled = effect.prepare(prepare);
				if (cancelled)
					break;
			}
			if (cancelled) {
				for (IRenderingCharEffect effect : new ArrayList<>(context.effects)) {
					effect.onFixingCancelled(context, ch);
				}
				return null;
			}
			return ch;
		}

		public static SplitResult split(MeasuringStringWidthContext context, float wrapWidth, boolean keepFormatting) {
			return split(context, wrapWidth, keepFormatting, false, 0, false);
		}
		public static SplitResult split(MeasuringStringWidthContext context, float wrapWidth, boolean keepFormatting, boolean removeWhiteSpace, float currentWidth, boolean firstNewLineIfNoSpace) {
			SplitArgs splitArgs = new SplitArgs(context, wrapWidth, removeWhiteSpace, keepFormatting, currentWidth, firstNewLineIfNoSpace);
			parse(context.originalText).split(splitArgs);
			splitArgs.resultList.add(splitArgs.buffered.toString());
			return new SplitResult(splitArgs.currentWidth, splitArgs.resultList);
		}
		private void split(SplitArgs splitArgs) {
			MeasuringStringWidthContext context = splitArgs.context;
			switch (type) {
				case TEXT -> {
					String text = context.originalText.substring(beginIndex, endIndexExcl);
					splitString(splitArgs, text, context.effects);
				}
				case SINGLE_SECTION_SIGN -> {
					splitString(splitArgs, "§", getInvalidDisplayEffects());
				}
				case ESCAPED_SECTION_SIGN -> {
					if (context.isEdit)
						splitString(splitArgs, "§§", getValidDisplayEffects());
					else
						splitString(splitArgs, "§§", context.effects);
				}
				case FORMATTING_CODE_UNTERMINATED_LONGKEY, FORMATTING_CODE_UNKNOWN -> {
					String text = context.originalText.substring(beginIndex, endIndexExcl);
					splitString(splitArgs, text, getInvalidDisplayEffects());
				}
				case FORMATTING_CODE -> {
					if (pair.getValue().isValid(option)) {
						pair.getValue().applyFormat(context, option);
						if (splitArgs.keepFormatting)
							splitArgs.prevFormat.append(pair.getValue().getFormatString(option));
						if (context.isEdit) {
							String text = context.originalText.substring(beginIndex, endIndexExcl);
							splitString(splitArgs, text, getValidDisplayEffects());
						} else {
							String text = context.originalText.substring(beginIndex, endIndexExcl);
							splitArgs.buffered.append(text);
						}
					} else {
						String text = context.originalText.substring(beginIndex, endIndexExcl);
						splitString(splitArgs, text, getInvalidDisplayEffects());
					}
				}
				case FORMATTING_RANGE, FORMATTING_RANGE_UNTERMINATED -> {
					if (splitArgs.keepFormatting) {
						splitArgs.prevFormat.append('§');
						splitArgs.prevFormat.append(StringUtils.repeat('{', markerNum));
						splitArgs.closeMarkerStack.add(StringUtils.repeat('}', markerNum));
					}
					Collection<IRenderingCharEffect> effects_before = new ArrayList<>(context.effects);
					if (context.isEdit) {
						String text = context.originalText.substring(beginIndex, beginIndex + 1 + markerNum);
						splitString(splitArgs, text, getValidDisplayEffects());
						if (component != null)
							component.split(splitArgs);
						context.effects.clear();
						context.effects.addAll(effects_before);
						if (type == Type.FORMATTING_RANGE)
							splitString(splitArgs, StringUtils.repeat('}', markerNum), getValidDisplayEffects());
					} else {
						String text = context.originalText.substring(beginIndex, beginIndex + 1 + markerNum);
						if (type == Type.FORMATTING_RANGE_UNTERMINATED) {
							splitString(splitArgs, "§{" + TextFormatting.RESET + getValidDisplayStr() + "§" + text + "}", Collections.emptyList());
						}
						splitArgs.buffered.append(text);
						if (component != null)
							component.split(splitArgs);
						context.effects.clear();
						context.effects.addAll(effects_before);
						splitArgs.buffered.append(StringUtils.repeat('}', markerNum));//TODO
						splitArgs.prevFormat.append(StringUtils.repeat('}', markerNum));//TODO
					}
					splitArgs.closeMarkerStack.remove(splitArgs.closeMarkerStack.size() - 1);
				}
			}
			if (next != null)
				next.split(splitArgs);
		}
		private static void splitString(SplitArgs splitArgs, String text, Collection<IRenderingCharEffect> effects) {
			List<String> list = splitArgs.resultList;
			FontRenderer fontRenderer = splitArgs.context.fontRenderer;
			float wrapWidth = splitArgs.wrapWidth;
			boolean removeWhiteSpace = splitArgs.removeWhiteSpace;
			StringBuilder buffered = splitArgs.buffered;
			boolean keepFormatting = splitArgs.keepFormatting;
			StringBuilder prevFormat = splitArgs.prevFormat;
			List<String> closeMarkerStack = splitArgs.closeMarkerStack;
			float currentWidth = splitArgs.currentWidth;
			boolean newLineIfNoSpace = splitArgs.newLineIfNoSpace;
			int startIndex = 0;
			int whiteSpaceIdx = -1;
			for (int i = 0; i < text.length(); i++) {
				char ch = text.charAt(i);
				if (ch == '\n') {
					list.add(buffered + text.substring(startIndex, i));
					startIndex = i + 1;
					currentWidth = 0;
					buffered.setLength(0);
					if (keepFormatting)
						buffered.append(prevFormat);
				} else if (ch == ' ') {
					whiteSpaceIdx = i;
				} else {
					float w = getCharWidthWithSpace(fontRenderer, ch, effects);
					if (currentWidth + w > wrapWidth) {
						if (whiteSpaceIdx == -1 && newLineIfNoSpace) {
							//空白が無いので全て次の行に持っていく
							list.add("");
							currentWidth -= splitArgs.beforeWidth;
							newLineIfNoSpace = false;
							if (currentWidth + w <= wrapWidth) {
								currentWidth += w;
								continue;
							}
						}
						currentWidth = 0;
						if (whiteSpaceIdx == -1) {
							list.add(buffered + text.substring(startIndex, i) + StringUtils.join(ListUtil.descendingIteratorOf(closeMarkerStack), ""));
							startIndex = i;//i番目の文字を含む
						} else {
							list.add(buffered + text.substring(startIndex, whiteSpaceIdx) + StringUtils.join(ListUtil.descendingIteratorOf(closeMarkerStack), ""));
							if (removeWhiteSpace) {
								startIndex = whiteSpaceIdx + 1;//空白を含めない
							} else {
								startIndex = whiteSpaceIdx;
								currentWidth += getCharWidthRaw(fontRenderer, ' ');
							}
							whiteSpaceIdx = -1;
						}
						buffered.setLength(0);
						if (keepFormatting)
							buffered.append(prevFormat);
					}
					currentWidth += w;
				}
			}
			if (startIndex < text.length())
				buffered.append(text.substring(startIndex));
			splitArgs.currentWidth = currentWidth;
			splitArgs.newLineIfNoSpace = newLineIfNoSpace;
		}

		public enum Type {
			TEXT,
			SINGLE_SECTION_SIGN,
			ESCAPED_SECTION_SIGN,
			FORMATTING_CODE_UNTERMINATED_LONGKEY,
			FORMATTING_CODE_UNKNOWN,
			FORMATTING_CODE,
			FORMATTING_RANGE,
			FORMATTING_RANGE_UNTERMINATED,
		}

		private static final class SplitArgs {
			public final List<String> resultList = new ArrayList<>();
			public final MeasuringStringWidthContext context;
			public final float wrapWidth;
			public final boolean removeWhiteSpace;
			public final StringBuilder buffered = new StringBuilder();
			public final boolean keepFormatting;
			public final StringBuilder prevFormat = new StringBuilder();
			public final List<String> closeMarkerStack = new ArrayList<>();
			public float currentWidth;
			public boolean newLineIfNoSpace;
			public final float beforeWidth;
			public SplitArgs(MeasuringStringWidthContext context, float wrapWidth, boolean removeWhiteSpace, boolean keepFormatting, float currentWidth, boolean newLineIfNoSpace) {
				this.context = context;
				this.wrapWidth = wrapWidth;
				this.keepFormatting = keepFormatting;
				this.removeWhiteSpace = removeWhiteSpace;
				this.currentWidth = currentWidth;
				this.newLineIfNoSpace = newLineIfNoSpace;
				beforeWidth = currentWidth;
			}
		}
	}

	//render
	public static float renderString(FontRenderer fontRenderer, String text, float posX, float posY, boolean asShadow) {
		return renderSubString(fontRenderer, text, 0, text.length(), posX, posY, asShadow);
	}
	public static float renderRawString(FontRenderer fontRenderer, String text, float posX, float posY, boolean asShadow) {
		float x = posX;
		for (int i = 0; i < text.length(); i++) {
			x += renderChar(fontRenderer, text.charAt(i), x, posY, Collections.emptyList(), asShadow);
		}
		return x;
	}
	public static float renderSubString(FontRenderer fontRenderer, String text, int beginIndex, int endIndex, float posX, float posY, boolean asShadow) {
		if (text.isEmpty())
			return posX;
		endIndex = Math.min(endIndex, text.length());
		return FormattedStringObject.render(fontRenderer, text, beginIndex, endIndex, posX, posY, asShadow);
	}
	public static float renderChar(FontRenderer fontRenderer, char ch, float posX, float posY, Collection<IRenderingCharEffect> effects, boolean asShadow) {
		return renderChar(fontRenderer, ch, posX, posY, effects, asShadow, false, GL11.glGetInteger(GL30.GL_FRAMEBUFFER_BINDING));
	}
	private static float renderChar(FontRenderer fontRenderer, char ch, float posX, float posY, Collection<IRenderingCharEffect> effects, boolean asShadow, boolean isStringRendering, int framebufferObject) {
		if (ch == 0)
			return 0;
		float char_width;
		float next_render_x_offset;
		float min_u;
		float min_v;
		if (ch == ' ') {
			char_width = 3;
			next_render_x_offset = 4;
			min_u = 0;
			min_v = 0;
		} else {
			int index = getAsciiCharIndex(fontRenderer, ch);
			if (index != -1) {
				float cw = getCharWidthRaw(fontRenderer, ch);
				char_width = cw - 0.01f;
				next_render_x_offset = cw + 1;
				int i = index % 16 * 8;
				int j = index / 16 * 8;
				min_u = i / 128f;
				min_v = j / 128f;
				fontRenderer.renderEngine.bindTexture(fontRenderer.locationFontTexture);
			} else {
				int width_data = fontRenderer.glyphWidth[ch] & 255;
				int texture_index = ch / 256;
				float left = width_data >>> 4;
				float right = width_data & 15;
				char_width = (right + 1 - left - 0.02f) / 2;
				next_render_x_offset = (right + 1 - left) / 2 + 1;
				if (CompatOptifine.isOptifineLoaded())
					next_render_x_offset = (int) next_render_x_offset;
				float f2 = (float) (ch % 16 * 16) + left;
				float f3 = (float) ((ch & 255) / 16 * 16);
				min_u = f2 / 256f;
				min_v = f3 / 256f;
				fontRenderer.loadGlyphTexture(texture_index);
				if (asShadow) {
					posX -= 0.5f;
					posY -= 0.5f;
				}
			}
		}
		//greenとblueが逆なのはバニラが悪い
		RenderingCharContext context = new RenderingCharContext(fontRenderer, ch, char_width, CHAR_HEIGHT, asShadow, isStringRendering, min_u, min_v, posX, posY, fontRenderer.red, fontRenderer.blue, fontRenderer.green, fontRenderer.alpha, next_render_x_offset, framebufferObject);
		for (IRenderingCharEffect effect : effects) {
			effect.preRender(context);
		}

		//render!
		if (ch != ' ') {
			renderCharRaw(context.red, context.green, context.blue, context.alpha, context.minU, context.minV, context.maxU, context.maxV, context.posX + context.renderLeftTopX, context.posY + context.renderLeftTopY, context.renderLeftTopZ, context.posX + context.renderLeftBottomX, context.posY + context.renderLeftBottomY, context.renderLeftBottomZ, context.posX + context.renderRightTopX, context.posY + context.renderRightTopY, context.renderRightTopZ, context.posX + context.renderRightBottomX, context.posY + context.renderRightBottomY, context.renderRightBottomZ);
		}

		for (IRenderingCharEffect effect : effects) {
			effect.postRender(context);
		}
		return context.nextRenderXOffset;
	}
	public static void renderCharRaw(float red, float green, float blue, float alpha, float minU, float minV, float maxU, float maxV, float leftTopX, float leftTopY, float leftTopZ, float leftBottomX, float leftBottomY, float leftBottomZ, float rightTopX, float rightTopY, float rightTopZ, float rightBottomX, float rightBottomY, float rightBottomZ) {
		GlStateManager.color(red, green, blue, alpha);
		GlStateManager.glBegin(GL11.GL_TRIANGLE_STRIP);
		GlStateManager.glTexCoord2f(minU, minV);
		GlStateManager.glVertex3f(leftTopX, leftTopY, leftTopZ);
		GlStateManager.glTexCoord2f(minU, maxV);
		GlStateManager.glVertex3f(leftBottomX, leftBottomY, leftBottomZ);
		GlStateManager.glTexCoord2f(maxU, minV);
		GlStateManager.glVertex3f(rightTopX, rightTopY, rightTopZ);
		GlStateManager.glTexCoord2f(maxU, maxV);
		GlStateManager.glVertex3f(rightBottomX, rightBottomY, rightBottomZ);
		GlStateManager.glEnd();
	}
	private static float prepareAndRenderChar(RenderingStringContext context, char ch) {
		PreparingContext prepare = new PreparingContext(context.fontRenderer, ch, context.isEdit, true, context.originalText, context.tryGetRingList(), context.effects);
		boolean cancelled = false;
		for (IRenderingCharEffect effect : context.effects) {
			cancelled = effect.prepare(prepare);
			if (cancelled)
				break;
		}
		if (cancelled) {
			float width = 0;
			for (IRenderingCharEffect effect : new ArrayList<>(context.effects)) {
				width = effect.onRenderingCancelled(context, ch, width);
			}
			return width;
		}

		return renderChar(context.fontRenderer, prepare.charToRender, context.posX, context.posY, context.effects, context.asShadow, true, context.framebufferObject);
	}

	//getWidth
	public static float getStringWidthFloat(FontRenderer fontRenderer, String text) {
		return getSubStringWidthFloat(fontRenderer, text, 0, text.length());
	}
	public static float getRawStringWidthFloat(FontRenderer fontRenderer, String text) {
		float total_width = 0;
		for (int i = 0; i < text.length(); i++) {
			total_width += getCharWidthWithSpace(fontRenderer, text.charAt(i), Collections.emptyList());
		}
		return total_width;
	}
	public static float getSubStringWidthFloat(FontRenderer fontRenderer, String text, int beginIndex, int endIndex) {
		if (text.isEmpty())
			return 0;
		endIndex = Math.min(endIndex, text.length());
		MeasuringStringWidthContext context = new MeasuringStringWidthContext(fontRenderer, text, isEditMode);
		context.effects.addAll(appliedEffects);
		return FormattedStringObject.parse(text).measure(context, beginIndex, endIndex, Float.POSITIVE_INFINITY).totalWidth;
	}
	public static float getCharWidthWithSpace(FontRenderer fontRenderer, char ch, Collection<IRenderingCharEffect> effects) {
		if (ch == 0)
			return 0;
		float char_width = getCharWidthRaw(fontRenderer, ch);
		MeasuringCharWidthContext context = new MeasuringCharWidthContext(fontRenderer, ch, char_width, CHAR_HEIGHT);
		for (IRenderingCharEffect effect : effects) {
			effect.first(context);
		}
		for (IRenderingCharEffect effect : effects) {
			effect.second(context);
		}
		return context.charWidthWithSpace;
	}
	public static float getCharWidthRaw(FontRenderer fontRenderer, char ch) {
		if (ch == 0)
			return 0;
		float char_width;
		if (ch == ' ') {
			char_width = 3;
		} else {
			int index = getAsciiCharIndex(fontRenderer, ch);
			if (index != -1) {
				if (CompatOptifine.isOptifineLoaded())
					char_width = CompatFontRenderer.getCharWidthFloat(fontRenderer, ch) - 1;
				else
					char_width = fontRenderer.charWidth[index] - 1;
			} else {
				int width_data = fontRenderer.glyphWidth[ch] & 255;
				int left = width_data >>> 4;
				int right = width_data & 15;
				float w = (right + 1 - left) / 2f;
				char_width = CompatOptifine.isOptifineLoaded() ? (int) w : w;
			}
		}
		return char_width;
	}
	private static float prepareAndGetCharWidth(MeasuringStringWidthContext context, char ch) {
		PreparingContext prepare = new PreparingContext(context.fontRenderer, ch, context.isEdit, false, context.originalText, context.tryGetRingList(), context.effects);
		boolean cancelled = false;
		for (IRenderingCharEffect effect : context.effects) {
			cancelled = effect.prepare(prepare);
			if (cancelled)
				break;
		}
		if (cancelled) {
			float width = 0;
			for (IRenderingCharEffect effect : new ArrayList<>(context.effects)) {
				width = effect.onMeasuringCancelled(context, ch, width);
			}
			return width;
		}
		return getCharWidthWithSpace(context.fontRenderer, prepare.charToRender, context.effects);
	}

	//trim
	public static String trimStringToWidth(FontRenderer fontRenderer, String text, int width, boolean reverse) {
		if (reverse)
			return text.substring(getBeginIndexInclOfReversedTrimmedSubString(fontRenderer, text, text.length(), width));
		else
			return text.substring(0, getEndIndexExcOfTrimmedSubString(fontRenderer, text, 0, width));
	}
	public static String trimRawStringToWidth(FontRenderer fontRenderer, String text, int width, boolean reverse) {
		if (!reverse) {
			float total_width = 0;
			for (int i = 0; i < text.length(); i++) {
				char c0 = text.charAt(i);
				total_width += getCharWidthWithSpace(fontRenderer, c0, Collections.emptyList());
				if (total_width > width) {
					return text.substring(0, i);//今見た文字は含まれない
				}
			}
			return text;
		} else {
			float total_width = 0;
			for (int i = text.length() - 1; i >= 0; i--) {
				char c0 = text.charAt(i);
				total_width += getCharWidthWithSpace(fontRenderer, c0, Collections.emptyList());
				if (total_width > width)
					return text.substring(i + 1);//今見た文字は含まない
			}
			return text;
		}
	}
	public static int getEndIndexExcOfTrimmedSubString(FontRenderer fontRenderer, String text, int beginIndex, float width) {
		if (text.isEmpty())
			return 0;
		MeasuringStringWidthContext context = new MeasuringStringWidthContext(fontRenderer, text, isEditMode);
		context.effects.addAll(appliedEffects);
		MeasuringResult result = FormattedStringObject.parse(text).measure(context, beginIndex, text.length(), width);
		if (result.lastIndexExc == -1)
			return text.length();
		else
			return result.lastIndexExc;
	}
	public static int getBeginIndexInclOfReversedTrimmedSubString(FontRenderer fontRenderer, String text, int lastIndexExclusive, float width) {
		if (text.isEmpty())
			return 0;
		lastIndexExclusive = Math.min(lastIndexExclusive, text.length());
		MeasuringStringWidthContext context = new MeasuringStringWidthContext(fontRenderer, text, RenderFontUtil.isEditMode);
		context.effects.addAll(RenderFontUtil.appliedEffects);
		float[] widths = FormattedStringObject.getWidths(text, context, true);

		float total_width = 0;
		for (int i = lastIndexExclusive - 1; i >= 0; i--) {
			total_width += widths[i];
			if (total_width > width)
				return i + 1;//今見た文字は含まない
		}
		return 0;
	}

	//formatString
	public static String getFormatFromString(String text) {
		if (text.isEmpty())
			return text;
		FormattingContext context = new FormattingContext(text, isEditMode);
		return FormattedStringObject.getFormatFromString(context);
	}

	//remove
	public static String removeFormat(String text) {
		if (text.isEmpty())
			return text;
		FormattingContext context = new FormattingContext(text, isEditMode);
		return FormattedStringObject.removeFormat(context);
	}

	//getEffects
	public static Collection<IRenderingCharEffect> getEffects(String text) {
		if (text.isEmpty())
			return Collections.emptyList();
		return FormattedStringObject.getEffects(text);
	}

	//fix
	public static String fixString(FontRenderer fontRenderer, String text) {
		if (text.isEmpty())
			return text;
		FixingContext context = new FixingContext(fontRenderer, text, RenderFontUtil.isEditMode);
		context.effects.addAll(RenderFontUtil.appliedEffects);
		return FormattedStringObject.fix(context);
	}

	//wrap
	public static String wrapFormattedStringToWidth(FontRenderer fontRenderer, String text, float wrapWidth, boolean keepFormatting) {
		if (text.isEmpty())
			return text;
		text = fixString(fontRenderer, text);
		MeasuringStringWidthContext context = new MeasuringStringWidthContext(fontRenderer, text, isEditMode);
		context.effects.addAll(appliedEffects);
		return StringUtils.join(FormattedStringObject.split(context, wrapWidth, keepFormatting).lines, '\n');
	}

	//splitITextComponent
	private static List<ITextComponent> splitVanilla(ITextComponent textComponent, int maxTextLenght, FontRenderer fontRendererIn, boolean removeWhiteSpace, boolean forceTextColor) {
		int line_width = 0;
		ITextComponent line_text = new TextComponentString("");
		List<ITextComponent> result = new ArrayList<>();
		Deque<ITextComponent> stack = new ArrayDeque<>();
		//stackだから逆順
		for (int i = textComponent.getSiblings().size() - 1; i >= 0; i--) {
			stack.push(textComponent.getSiblings().get(i));
		}
		stack.push(textComponent);

		while (!stack.isEmpty()) {
			ITextComponent itc = stack.pop();
			String text = GuiUtilRenderComponents.removeTextColorsIfConfigured(itc.getStyle().getFormattingCode() + itc.getUnformattedComponentText(), forceTextColor);
			boolean newLine = false;

			//改行コードを含む場合は以降をTextComponentStringとして追加&改行
			if (text.contains("\n")) {
				int idx = text.indexOf('\n');
				String afterNewLine = text.substring(idx + 1);
				text = text.substring(0, idx + 1);
				ITextComponent after = new TextComponentString(afterNewLine);
				after.setStyle(itc.getStyle().createShallowCopy());
				stack.push(after);
				newLine = true;
			}

			String beforeNewLine = text.endsWith("\n") ? text.substring(0, text.length() - 1) : text;
			int width_before = fontRendererIn.getStringWidth(beforeNewLine);
			TextComponentString tc_before = new TextComponentString(beforeNewLine);
			tc_before.setStyle(itc.getStyle().createShallowCopy());

			//幅がはみ出たら
			if (line_width + width_before > maxTextLenght) {
				//幅でカット
				String before = fontRendererIn.trimStringToWidth(text, maxTextLenght - line_width, false);
				String after = before.length() < text.length() ? text.substring(before.length()) : null;

				//普通はここはtrueになる
				if (after != null && !after.isEmpty()) {
					//before側に空白があればそこを区切りにする
					int idx_whitespace = before.lastIndexOf(' ');

					if (idx_whitespace >= 0 && fontRendererIn.getStringWidth(text.substring(0, idx_whitespace)) > 0) {
						before = text.substring(0, idx_whitespace);

						if (removeWhiteSpace) {
							++idx_whitespace;
						}

						after = text.substring(idx_whitespace);
					} else if (line_width > 0 && !text.contains(" ")) {
						//行の途中から開始し、空白が無い場合は新しい行から開始
						//trueになるのはITextComponentとITextComponentの境目しかない
						before = "";
						after = text;
					}

					after = FontRenderer.getFormatFromString(before) + after; //Forge: Fix chat formatting not surviving line wrapping.

					TextComponentString tc_after = new TextComponentString(after);
					tc_after.setStyle(itc.getStyle().createShallowCopy());
					stack.push(tc_after);
				}

				width_before = fontRendererIn.getStringWidth(before);
				tc_before = new TextComponentString(before);
				tc_before.setStyle(itc.getStyle().createShallowCopy());
				newLine = true;
			}

			//普通はtrueじゃね？
			if (line_width + width_before <= maxTextLenght) {
				line_width += width_before;
				line_text.appendSibling(tc_before);
			} else {
				newLine = true;
			}

			if (newLine) {
				result.add(line_text);
				line_width = 0;
				line_text = new TextComponentString("");
			}
		}
		result.add(line_text);
		return result;
	}
	public static List<ITextComponent> split(ITextComponent textComponent, float wrapWidth, FontRenderer fontRenderer, boolean removeWhiteSpace, boolean forceTextColor) {
		List<ITextComponent> result = new ArrayList<>();
		ITextComponent line_text = new TextComponentString("");
		float last_width = 0;
		for (ITextComponent itc : textComponent) {
			Style style = itc.getStyle();
			String text = fixString(fontRenderer, GuiUtilRenderComponents.removeTextColorsIfConfigured(style.getFormattingCode() + itc.getUnformattedComponentText(), forceTextColor));
			MeasuringStringWidthContext context = new MeasuringStringWidthContext(fontRenderer, text, isEditMode);
			context.effects.addAll(appliedEffects);
			SplitResult split_result = FormattedStringObject.split(context, wrapWidth, true, removeWhiteSpace, last_width, true);
			List<String> lines = split_result.lines;
			line_text.appendSibling(new TextComponentString(lines.get(0)).setStyle(style.createShallowCopy()));
			if (lines.size() != 1) {
				result.add(line_text);
				for (int i = 1; i < lines.size() - 1; i++) {
					result.add(new TextComponentString(lines.get(i)).setStyle(style.createShallowCopy()));
				}
				line_text = new TextComponentString(lines.get(lines.size() - 1)).setStyle(style.createShallowCopy());
			}
			last_width = split_result.lastWidth;
		}
		result.add(line_text);
		return result;
	}

	//fontRendererUtils
	public static float drawString(FontRenderer fontRenderer, String text, float x, float y, int color) {
		return drawString(fontRenderer, text, x, y, color, false);
	}
	public static float drawString(FontRenderer fontRenderer, String text, float x, float y, int color, boolean withShadow) {
		//Optifineのblendがtrueになってる場合とblendFuncがわずかに異なるが無視
		//そもそもfontの時点で半透明なのはどうなのかって話でもある
		if (withShadow) {
			return drawStringWithShadow(fontRenderer, text, x, y, color);
		} else {
			GlStateManager.enableAlpha();
			setColorAsDefault(color, false, fontRenderer);
			return renderString(fontRenderer, text, x, y, false);
		}
	}
	public static float drawSubString(FontRenderer fontRenderer, String text, int beginIndex, int endIndex, float x, float y, int color) {
		return drawSubString(fontRenderer, text, beginIndex, endIndex, x, y, color, false);
	}
	public static float drawSubString(FontRenderer fontRenderer, String text, int beginIndex, int endIndex, float x, float y, int color, boolean withShadow) {
		if (withShadow) {
			return drawSubStringWithShadow(fontRenderer, text, beginIndex, endIndex, x, y, color);
		} else {
			GlStateManager.enableAlpha();
			setColorAsDefault(color, false, fontRenderer);
			return renderSubString(fontRenderer, text, beginIndex, endIndex, x, y, false);
		}
	}
	public static float drawStringWithShadow(FontRenderer fontRenderer, String text, float x, float y, int color) {
		GlStateManager.enableAlpha();
		setColorAsDefault(color, true, fontRenderer);
		float x1 = renderString(fontRenderer, text, x + 1.0F, y + 1.0F, true);

		setColorAsDefault(color, false, fontRenderer);
		float x2 = renderString(fontRenderer, text, x, y, false);
		return Math.max(x1, x2);
	}
	public static float drawRawStringWithShadow(FontRenderer fontRenderer, String text, float x, float y, int color) {
		GlStateManager.enableAlpha();
		setColorAsDefault(color, true, fontRenderer);
		float x1 = renderRawString(fontRenderer, text, x + 1.0F, y + 1.0F, true);

		setColorAsDefault(color, false, fontRenderer);
		float x2 = renderRawString(fontRenderer, text, x, y, false);
		return Math.max(x1, x2);
	}
	public static float drawSubStringWithShadow(FontRenderer fontRenderer, String text, int beginIndex, int endIndex, float x, float y, int color) {
		GlStateManager.enableAlpha();
		setColorAsDefault(color, true, fontRenderer);
		float x1 = renderSubString(fontRenderer, text, beginIndex, endIndex, x + 1.0F, y + 1.0F, true);

		setColorAsDefault(color, false, fontRenderer);
		float x2 = renderSubString(fontRenderer, text, beginIndex, endIndex, x, y, false);
		return Math.max(x1, x2);
	}
	public static Pair<Float, Float> drawSplitString(FontRenderer fontRenderer, String str, int x, int y, float wrapWidth, int textColor) {
		return drawSplitString(fontRenderer, str, x, y, wrapWidth, textColor, true);
	}
	public static Pair<Float, Float> drawSplitString(FontRenderer fontRenderer, String str, int x, int y, float wrapWidth, int textColor, boolean keepFormatting) {
		//Optifineのblendがtrueになってる場合とblendFuncがわずかに異なるが無視
		fontRenderer.textColor = textColor;
		float last_x = x;
		for (String s : listFormattedStringToWidth(fontRenderer, str, wrapWidth, keepFormatting)) {
			last_x = fontRenderer.renderStringAligned(s, x, y, (int) wrapWidth, fontRenderer.textColor, false);
			y += fontRenderer.FONT_HEIGHT;
		}
		y -= fontRenderer.FONT_HEIGHT;
		for (int i = str.length() - 1; i >= 0 && str.charAt(i) == '\n'; i--) {
			last_x = x;
			y += fontRenderer.FONT_HEIGHT;
		}
		return Pair.of(last_x, (float) y);
	}
	public static List<String> listFormattedStringToWidth(FontRenderer fontRenderer, String str, float wrapWidth) {
		return listFormattedStringToWidth(fontRenderer, str, wrapWidth, true);
	}
	public static List<String> listFormattedStringToWidth(FontRenderer fontRenderer, String str, float wrapWidth, boolean keepFormatting) {
		return Arrays.asList(wrapFormattedStringToWidth(fontRenderer, str, wrapWidth, keepFormatting).split("\n"));
	}

	//others
	public static String escape(String text) {
		return text.replace("§", "§§");
	}
	public static void setColor(float red, float green, float blue, float alpha) {
		GlStateManager.color(red, green, blue, alpha);
	}
	public static void setColorAsDefault(int color, boolean asShadow, FontRenderer fontRenderer) {
		if ((color & 0xfc000000) == 0)
			color |= 0xff000000;
		if (asShadow)
			color = (color & 16579836) >> 2 | color & -16777216;
		float red = (float) (color >> 16 & 255) / 255.0F;
		float blue = (float) (color >> 8 & 255) / 255.0F;
		float green = (float) (color & 255) / 255.0F;
		float alpha = (float) (color >> 24 & 255) / 255.0F;
		setColor(red, green, blue, alpha);
		fontRenderer.red = red;
		fontRenderer.green = green;
		fontRenderer.blue = blue;
		fontRenderer.alpha = alpha;
	}
	public static int getAsciiCharIndex(FontRenderer fontRenderer, char c) {
		if (fontRenderer.unicodeFlag)
			return -1;
		return ASCII_CHARS.indexOf(c);
	}
	public static float getOffsetBold(FontRenderer fontRenderer) {
		if (CompatOptifine.isOptifineLoaded())
			return CompatFontRenderer.getOffsetBold(fontRenderer);
		else
			return 1;
	}

	public static class MeasuringResult {
		public final float totalWidth;
		public final int lastIndexExc;

		private MeasuringResult(float totalWidth, int lastIndexExc) {
			this.totalWidth = totalWidth;
			this.lastIndexExc = lastIndexExc;
		}
	}

	public static class SplitResult {
		public final float lastWidth;
		public final List<String> lines;
		public SplitResult(float lastWidth, List<String> lines) {
			this.lastWidth = lastWidth;
			this.lines = lines;
		}
	}
}
