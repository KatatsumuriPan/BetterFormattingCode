package kpan.better_fc.api;

import bre.smoothfont.FontProperty;
import bre.smoothfont.FontRasterizer;
import bre.smoothfont.FontRendererHook;
import bre.smoothfont.FontTexture;
import bre.smoothfont.FontTextureManager;
import bre.smoothfont.FontUtils;
import bre.smoothfont.GlyphImage;
import bre.smoothfont.config.CommonConfig;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import javax.annotation.Nullable;
import kpan.better_fc.api.contexts.chara.MeasuringCharWidthContext;
import kpan.better_fc.api.contexts.chara.PreparingContext;
import kpan.better_fc.api.contexts.chara.RenderingCharContext;
import kpan.better_fc.api.contexts.string.FixingContext;
import kpan.better_fc.api.contexts.string.FormattingContext;
import kpan.better_fc.api.contexts.string.GetEffectsContext;
import kpan.better_fc.api.contexts.string.MeasuringStringWidthContext;
import kpan.better_fc.api.contexts.string.RenderingStringContext;
import kpan.better_fc.asm.acc.ACC_FontRendererHook;
import kpan.better_fc.asm.compat.CompatOptifine;
import kpan.better_fc.asm.compat.CompatSmoothFont;
import kpan.better_fc.compat.CompatFontRenderer;
import kpan.better_fc.compat.CompatLineBreak;
import kpan.better_fc.compat.smoothfont.CompatFontRenderer_SmoothFont;
import kpan.better_fc.util.CharArrayRingList;
import kpan.better_fc.util.ListUtil;
import kpan.better_fc.util.StringReader;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiUtilRenderComponents;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

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

	public static RenderingEffects getValidDisplayEffects() {
		if (validDisplay == null)
			validDisplay = new ArrayList<>(getEffects(getValidDisplayStr()).getEffects());
		RenderingEffects effects = new RenderingEffects();
		effects.addAll(validDisplay);
		return effects;
	}
	public static RenderingEffects getInvalidDisplayEffects() {
		if (invalidDisplay == null)
			invalidDisplay = new ArrayList<>(getEffects(getInvalidDisplayStr()).getEffects());
		RenderingEffects effects = new RenderingEffects();
		effects.addAll(invalidDisplay);
		return effects;
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
			String option = stringReader.readStr(formattingCode.getArgStringLength(stringReader));
			return FormattedStringObject.formattingCode(begin, stringReader.getCursor(), pair, option);
		}
		private static boolean isRangeEnd(StringReader stringReader, @Nullable String rangeEnd) {
			return rangeEnd != null && stringReader.canRead(rangeEnd.length()) && stringReader.peekStr(rangeEnd.length()).equals(rangeEnd);
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
					int begin = Math.max(beginIndex, wholeBeginIndex);
					if (begin < endIndexExcl) {
						context.effects.beginBatchStencilColor(context);
						for (int i = begin; i < endIndexExcl; i++) {
							if (i >= wholeEndIndexExcl) {
								context.effects.endBatchStencilColor(context);
								return;
							}
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
						context.effects.endBatchStencilColor(context);
					}
				}
				case SINGLE_SECTION_SIGN -> {
					if (beginIndex >= wholeEndIndexExcl)
						return;
					GlStateManager.color(context.fontRenderer.red, context.fontRenderer.green, context.fontRenderer.blue, context.fontRenderer.alpha);
					//§の次が無い
					if (context.isEdit)
						context.posX += renderChar(context, '§', context.posX, context.posY, getInvalidDisplayEffects());
					else
						context.posX += renderChar(context, '§', context.posX, context.posY, RenderingEffects.EMPTY);
				}
				case ESCAPED_SECTION_SIGN -> {
					if (beginIndex >= wholeEndIndexExcl)
						return;
					if (context.isEdit) {
						GlStateManager.color(context.fontRenderer.red, context.fontRenderer.green, context.fontRenderer.blue, context.fontRenderer.alpha);
						context.posX += renderChar(context, '§', context.posX, context.posY, getValidDisplayEffects());
						if (beginIndex + 1 >= wholeEndIndexExcl)
							return;
						context.posX += renderChar(context, '§', context.posX, context.posY, getValidDisplayEffects());
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
								context.posX += renderChar(context, context.originalText.charAt(i), context.posX, context.posY, getValidDisplayEffects());
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
					Collection<IRenderingCharEffect> effects_before = new ArrayList<>(context.effects.getEffects());
					if (context.isEdit) {
						GlStateManager.color(context.fontRenderer.red, context.fontRenderer.green, context.fontRenderer.blue, context.fontRenderer.alpha);
						for (int i = Math.max(beginIndex, wholeBeginIndex); i < beginIndex + 1 + markerNum; i++) {
							if (i >= wholeEndIndexExcl)
								return;
							context.posX += renderChar(context, context.originalText.charAt(i), context.posX, context.posY, getValidDisplayEffects());
						}
					} else {
						if (type == Type.FORMATTING_RANGE_UNTERMINATED) {
							GlStateManager.color(context.fontRenderer.red, context.fontRenderer.green, context.fontRenderer.blue, context.fontRenderer.alpha);
							for (int i = Math.max(beginIndex, wholeBeginIndex); i < beginIndex + 1 + markerNum; i++) {
								if (i >= wholeEndIndexExcl)
									return;
								context.posX += renderChar(context, context.originalText.charAt(i), context.posX, context.posY, getValidDisplayEffects());
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
								context.posX += renderChar(context, context.originalText.charAt(i), context.posX, context.posY, getValidDisplayEffects());
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
				context.posX += renderChar(context, context.originalText.charAt(i), context.posX, context.posY, getInvalidDisplayEffects());
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
						w = getCharWidthWithSpace(context.fontRenderer, '§', RenderingEffects.EMPTY);
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
					Collection<IRenderingCharEffect> effects_before = new ArrayList<>(context.effects.getEffects());
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
						w = getCharWidthWithSpace(context.fontRenderer, '§', RenderingEffects.EMPTY);
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
					Collection<IRenderingCharEffect> effects_before = new ArrayList<>(context.effects.getEffects());
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

		public static RenderingEffects getEffects(String text) {
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
					Collection<IRenderingCharEffect> effects_before = new ArrayList<>(context.effects.getEffects());
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
			boolean cancelled = context.effects.prepare(prepare);
			if (cancelled) {
				context.effects.onFixingCancelled(context, ch);
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
					Collection<IRenderingCharEffect> effects_before = new ArrayList<>(context.effects.getEffects());
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
							splitString(splitArgs, "§{" + TextFormatting.RESET + getValidDisplayStr() + "§" + text + "}", RenderingEffects.EMPTY);
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
		private static void splitString(SplitArgs splitArgs, String text, RenderingEffects effects) {
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
			int breakableIdx = -1;
			IntSet breakIndices = CompatLineBreak.phraseIndices(text);
			for (int i = 0; i < text.length(); i++) {
				char ch = text.charAt(i);
				if (ch == '\n') {
					list.add(buffered + text.substring(startIndex, i));
					startIndex = i + 1;
					breakableIdx = -1;
					currentWidth = 0;
					buffered.setLength(0);
					if (keepFormatting)
						buffered.append(prevFormat);
				} else if (ch == ' ') {
					if (i > startIndex)
						breakableIdx = i;
				} else {
					if (i > startIndex && CompatLineBreak.canBreak(text.charAt(i - 1), ch, i, breakIndices))
						breakableIdx = i;
					float w = getCharWidthWithSpace(fontRenderer, ch, effects);
					if (currentWidth + w <= wrapWidth) {
						currentWidth += w;
						continue;
					}
					if (breakableIdx == -1 && newLineIfNoSpace) {
						//改行できる場所が無いので全て次の行に持っていく
						list.add("");
						currentWidth -= splitArgs.beforeWidth;
						newLineIfNoSpace = false;
						if (currentWidth + w <= wrapWidth) {
							currentWidth += w;
							continue;
						}
					}
					if (breakableIdx == -1) {
						list.add(buffered + text.substring(startIndex, i) + StringUtils.join(ListUtil.descendingIteratorOf(closeMarkerStack), ""));
						startIndex = i;//i番目の文字を含む
						currentWidth = w;
					} else {
						list.add(buffered + text.substring(startIndex, breakableIdx) + StringUtils.join(ListUtil.descendingIteratorOf(closeMarkerStack), ""));
						if (removeWhiteSpace && text.charAt(breakableIdx) == ' ') {
							startIndex = breakableIdx + 1;//空白を含めない
							currentWidth = 0;
							i = breakableIdx;//読み込み位置は空白の直後(i++含めて)に戻す
						} else {
							startIndex = breakableIdx;
							currentWidth = 0;
							i = breakableIdx - 1;//読み込み位置は改行位置(i++含めて)に戻す
						}
						breakableIdx = -1;
					}
					buffered.setLength(0);
					if (keepFormatting)
						buffered.append(prevFormat);
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
		RenderingStringContext context = new RenderingStringContext(fontRenderer, text, posX, posY, true, asShadow, GL11.glGetInteger(GL30.GL_FRAMEBUFFER_BINDING));
		for (int i = 0; i < text.length(); i++) {
			context.posX += renderChar(context, context.originalText.charAt(i), context.posX, context.posY, RenderingEffects.EMPTY);
		}
		context.onRenderEnd();
		return context.posX;
	}
	public static float renderSubString(FontRenderer fontRenderer, String text, int beginIndex, int endIndex, float posX, float posY, boolean asShadow) {
		if (text.isEmpty())
			return posX;
		endIndex = Math.min(endIndex, text.length());
		return FormattedStringObject.render(fontRenderer, text, beginIndex, endIndex, posX, posY, asShadow);
	}
	//	public static float renderChar(FontRenderer fontRenderer, char ch, float posX, float posY, Collection<IRenderingCharEffect> effects, boolean asShadow) {
//		return renderChar(fontRenderer, ch, posX, posY, effects, asShadow, false, GL11.glGetInteger(GL30.GL_FRAMEBUFFER_BINDING));
//	}
	private static float renderChar(RenderingStringContext stringContext, char ch, float posX, float posY, RenderingEffects effects) {
		if (ch == 0)
			return 0;
		RenderingCharContext charContext;
		if (CompatSmoothFont.isLoaded()) {
			charContext = SmoothFontIntegration.createContextSmoothFont(stringContext, ch, posX, posY);
			if (charContext == null)
				return 0;
		} else {
			charContext = createContextNormal(stringContext, ch, posX, posY);
		}

		effects.preRender(charContext);

		//render!
		if (!RenderFontUtil.isSpace(ch)) {
			renderCharRaw(charContext.red, charContext.green, charContext.blue, charContext.alpha,//rgba
					charContext.minU, charContext.minV, charContext.maxU, charContext.maxV,//uv
					charContext.posX + charContext.renderLeftTopX, charContext.posY + charContext.renderLeftTopY, charContext.renderLeftTopZ,//leftTop
					charContext.posX + charContext.renderLeftBottomX, charContext.posY + charContext.renderLeftBottomY, charContext.renderLeftBottomZ,//leftBottom
					charContext.posX + charContext.renderRightTopX, charContext.posY + charContext.renderRightTopY, charContext.renderRightTopZ,//rightTop
					charContext.posX + charContext.renderRightBottomX, charContext.posY + charContext.renderRightBottomY, charContext.renderRightBottomZ);//rightBottom
		}

		effects.postRender(charContext);

		return charContext.nextRenderXOffset;
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
		boolean cancelled = context.effects.prepare(prepare);
		if (cancelled)
			return context.effects.onRenderingCancelled(context, ch);

		return renderChar(context, prepare.charToRender, context.posX, context.posY, context.effects);
	}
	private static RenderingCharContext createContextNormal(RenderingStringContext context, char ch, float posX, float posY) {
		FontRenderer fontRenderer = context.fontRenderer;
		float char_rendering_width;
		float next_render_x_offset;
		float min_u;
		float min_v;
		if (RenderFontUtil.isSpace(ch)) {
			next_render_x_offset = CompatFontRenderer.getSpaceWidth(fontRenderer);
			if (!CompatOptifine.isLoaded())
				next_render_x_offset = (int) next_render_x_offset;
			char_rendering_width = next_render_x_offset - 1;
			min_u = 0;
			min_v = 0;
		} else {
			int index = getAsciiCharIndex(fontRenderer, ch);
			if (index != -1) {
				float cw = getCharWidthRaw(fontRenderer, ch);
				char_rendering_width = cw - 0.01f;
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
				char_rendering_width = (right + 1 - left - 0.02f) / 2;
				next_render_x_offset = (right + 1 - left) / 2 + 1;
				if (!CompatOptifine.isLoaded())
					next_render_x_offset = (int) next_render_x_offset;
				float f2 = (float) (ch % 16 * 16) + left;
				float f3 = (float) ((ch & 255) / 16 * 16);
				min_u = f2 / 256f;
				min_v = f3 / 256f;
				fontRenderer.loadGlyphTexture(texture_index);
				if (context.asShadow) {
					posX -= 0.5f;
					posY -= 0.5f;
				}
			}
		}
		float char_rendering_height = CHAR_HEIGHT;
		float max_u = min_u + char_rendering_width / 128f;
		float max_v = min_v + char_rendering_height / 128f;
		//greenとblueが逆なのはバニラが悪い
		return new RenderingCharContext(context, ch, char_rendering_width, char_rendering_height, min_u, min_v, max_u, max_v, posX, posY, posY + RenderFontUtil.FONT_HEIGHT / 2, fontRenderer.red, fontRenderer.blue, fontRenderer.green, fontRenderer.alpha, next_render_x_offset);
	}

	//getWidth
	public static float getStringWidthFloat(FontRenderer fontRenderer, String text) {
		return getSubStringWidthFloat(fontRenderer, text, 0, text.length());
	}
	public static float getRawStringWidthFloat(FontRenderer fontRenderer, String text) {
		float total_width = 0;
		for (int i = 0; i < text.length(); i++) {
			total_width += getCharWidthWithSpace(fontRenderer, text.charAt(i), RenderingEffects.EMPTY);
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
	public static float getCharWidthWithSpace(FontRenderer fontRenderer, char ch, RenderingEffects effects) {
		if (ch == 0)
			return 0;
		float char_width = getCharWidthRaw(fontRenderer, ch);
		MeasuringCharWidthContext context = new MeasuringCharWidthContext(fontRenderer, ch, char_width, CHAR_HEIGHT);
		effects.measure(context);
		return context.charWidthWithSpace;
	}
	/***
	 * Get char width without space(=fontRenderer.getCharWidth()-1)
	 */
	public static float getCharWidthRaw(FontRenderer fontRenderer, char ch) {
		if (ch == 0)
			return 0;
		float char_width;
		if (RenderFontUtil.isSpace(ch)) {
			char_width = CompatFontRenderer.getSpaceWidth(fontRenderer) - 1;
		} else {
			char_width = CompatFontRenderer.getCharWidthFloat(fontRenderer, ch) - 1;
		}
		return char_width;
	}
	private static float prepareAndGetCharWidth(MeasuringStringWidthContext context, char ch) {
		PreparingContext prepare = new PreparingContext(context.fontRenderer, ch, context.isEdit, false, context.originalText, context.tryGetRingList(), context.effects);
		boolean cancelled = context.effects.prepare(prepare);
		if (cancelled)
			return context.effects.onMeasuringCancelled(context, ch);
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
				total_width += getCharWidthWithSpace(fontRenderer, c0, RenderingEffects.EMPTY);
				if (total_width > width) {
					return text.substring(0, i);//今見た文字は含まれない
				}
			}
			return text;
		} else {
			float total_width = 0;
			for (int i = text.length() - 1; i >= 0; i--) {
				char c0 = text.charAt(i);
				total_width += getCharWidthWithSpace(fontRenderer, c0, RenderingEffects.EMPTY);
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
	public static RenderingEffects getEffects(String text) {
		if (text.isEmpty())
			return new RenderingEffects();
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

	//getColor
	public static int getColor(FontRenderer fontRenderer, RenderingEffects effects) {
		return getColor(fontRenderer, effects.getEffects());
	}
	public static int getColor(FontRenderer fontRenderer, Collection<IRenderingCharEffect> effects) {
		int color = -1;
		for (IRenderingCharEffect effect : effects) {
			if (effect instanceof IRenderingEffectColor c) {
				if (c instanceof IRenderingEffectSingleColor)
					color = ((IRenderingEffectSingleColor) c).getColor(fontRenderer);
				else
					color = -1;
			}
		}
		return color;
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
	public static float getOffsetBold(FontRenderer fontRenderer, char ch) {
		return CompatFontRenderer.getOffsetBold(fontRenderer, ch);
	}
	public static boolean isSpace(char ch) {
		return ch == ' ' || ch == '\u00a0';//nbsp
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

	private static class SmoothFontIntegration {

		@Nullable
		public static RenderingCharContext createContextSmoothFont(RenderingStringContext context, char ch, float posX, float posY) {
			RenderingStringContext.SmoothFontIntegration smoothFontIntegration = context.smoothFontIntegration;
			FontRenderer fontRenderer = context.fontRenderer;
			boolean asShadow = context.asShadow;
			FontRendererHook fontRendererHook = CompatFontRenderer_SmoothFont.getFontRendererHook(fontRenderer);
			ACC_FontRendererHook accFontRendererHook = (ACC_FontRendererHook) fontRendererHook;
			FontTextureManager fontTextureManager = accFontRendererHook.get_fontTextureManager();
			float centerY = posY + RenderFontUtil.FONT_HEIGHT / 2;
			int id = RenderFontUtil.getAsciiCharIndex(fontRenderer, ch);
			boolean isAscii = id >= 0;

			if (RenderFontUtil.isSpace(ch)) {
				float next_render_x_offset = CompatFontRenderer.getSpaceWidth(fontRenderer);
				next_render_x_offset = applyDoDrawHook(fontRendererHook, next_render_x_offset);
				float char_rendering_width = next_render_x_offset - 1;
				float min_u = 0;
				float min_v = 0;
				float char_rendering_height = CHAR_HEIGHT;
				float max_u = min_u + char_rendering_width / 128f;
				float max_v = min_v + char_rendering_height / 128f;
				//greenとblueが逆なのはバニラが悪い
				return new RenderingCharContext(context, ch, char_rendering_width, char_rendering_height, min_u, min_v, max_u, max_v, posX, posY, posY + RenderFontUtil.FONT_HEIGHT / 2, fontRenderer.red, fontRenderer.blue, fontRenderer.green, fontRenderer.alpha, next_render_x_offset);
			}

			float renderXOffset = 0;
			float renderYOffset = 0;
			float next_render_x_offset;
			float leftPx;
			float texWidthPx;
			float factor;
			FontTexture texture;
			FontRasterizer rasterizer = accFontRendererHook.get_rasterizer();
			boolean asBold = accFontRendererHook.get_boldChecker().isBold(posX, id, asShadow, !isAscii);//TODO
			boolean mcDefaultFontFlag;
			if (isAscii) {
				//renderDefaultCharHook
				accFontRendererHook.get_renderCharReplacedChecker().renderDefaultCharWorked = true;
				ResourceLocation curResLoc = fontRendererHook.changeFont ? ACC_FontRendererHook.get_osFontDefaultPageLocation() : fontRenderer.locationFontTexture;
				texture = fontTextureManager.bindTexture(curResLoc);
				factor = texture.actualFontRes / 16.0F;
				float width;
				if (fontRendererHook.changeFont) {
					mcDefaultFontFlag = false;
//					if (accFontRendererHook.get_renderStringAtPosInoperative()) {
					if (asBold) {
						posX -= 0.5F;
					}

					if (asShadow) {
						posX -= 0.5F;
						posY -= 0.5F;
						centerY -= 0.5F;
					}
//					}

					GlyphImage gi = rasterizer.getGlyphImage(256);
					switch (fontRendererHook.precisionMode) {
						case 0:
							width = rasterizer.charWidthFloat[id];
							break;
						case 1:
						default:
							width = rasterizer.charWidthFloat[id];
							if (CommonConfig.currentConfig.widthErrorCorrection) {
								if (asShadow) {
									renderXOffset += accFontRendererHook.get_errCorrectorShadow().getCorrectedPosX(posX, posY, width, gi.fontRes, asBold, false) - posX;
								} else {
									renderXOffset += accFontRendererHook.get_errCorrector().getCorrectedPosX(posX, posY, width, gi.fontRes, asBold, false) - posX;
								}
							}
							break;
						case 2:
							if (fontRendererHook.optifineCharWidthFloat != null) {
								width = rasterizer.charWidthFloat[id];
							} else {
								width = (float) rasterizer.charWidthInt[id];
							}
					}

					if (CommonConfig.currentConfig.fontAlignBaseline) {
						renderYOffset += gi.baselineGap + rasterizer.sizeAdjPosY;
					} else {
						int fontId = rasterizer.fontId[ch];
						FontProperty fontProp = rasterizer.fontProp[0][fontId];
						renderYOffset += gi.baselineGap + fontProp.ascentGap;
					}

					texWidthPx = gi.drawingChWidth[id] * texture.scaleFactor;
					renderXOffset -= gi.fontOriginPosX;
				} else {
					mcDefaultFontFlag = true;
					if (fontRendererHook.optifineCharWidthFloat != null) {
						width = fontRendererHook.optifineCharWidthFloat[id];
					} else {
						width = (float) fontRendererHook.mcCharWidth[id];
					}

					texWidthPx = (width - 0.01F) * 2.0F * factor;
				}

				accFontRendererHook.resetTexEnvAndBlend();
				if (!CommonConfig.currentConfig.performanceMode) {
					fontTextureManager.setAnisotropicFilter(curResLoc, smoothFontIntegration.anisotropicFilterEnabled);
					if (!smoothFontIntegration.exclusionCondDefault) {
						accFontRendererHook.get_fontShader().setShaderParams(fontRendererHook, false);
						fontTextureManager.setTexParams(curResLoc, FontRendererHook.texFilterSettingId);
					} else {
						fontTextureManager.setTexParamsNearest(curResLoc);
					}
				} else {
					fontTextureManager.setTexParams(curResLoc, FontRendererHook.texFilterSettingId);
				}
				next_render_x_offset = width;
				leftPx = 0;
			} else {
				//これが無いとつじつまが合わない
				{
					if (asBold) {
						posX -= 0.5F;
					}
					if (asShadow) {
						posX -= 0.5F;
						posY -= 0.5F;
						centerY -= 0.5F;
					}
				}
				byte[] glyphWidth = fontRenderer.glyphWidth;
				accFontRendererHook.get_renderCharReplacedChecker().renderUnicodeCharWorked = true;
				mcDefaultFontFlag = false;
				//italic省略はさせません
//			if (!CommonConfig.currentConfig.performanceMode && CommonConfig.currentConfig.disableSmallItalic && fontScale < 1.05F) {
//				italic = false;
//			}

				int page = ch / 256;
				ResourceLocation unicodePageLocation = fontRendererHook.changeFont ? accFontRendererHook.getOsFontUnicodePageLocation(page) : fontRendererHook.getUnicodePageLocation(page);
				float width;
				float left;
				float right;
				if (fontRendererHook.changeFont) {
					width = rasterizer.glyphWidthFloat8[ch];
					if (width == 0.0F) {
						return null;
					}

					texture = fontTextureManager.bindTexture(unicodePageLocation);
					factor = texture.actualFontRes / 16.0F;
//					if (accFontRendererHook.get_renderStringAtPosInoperative()) {
					fontRendererHook.boldFlag = accFontRendererHook.get_boldChecker().isBold(posX, ch, asShadow, true);
//					}

					left = 0.0F;
					GlyphImage gi = rasterizer.getGlyphImage(page);
					switch (fontRendererHook.precisionMode) {
						case 0:
							break;
						case 1:
						default:
							if (CommonConfig.currentConfig.widthErrorCorrection) {
								if (asShadow) {
									renderXOffset += accFontRendererHook.get_errCorrectorShadow().getCorrectedPosX(posX, posY, width, gi.fontRes, asBold, true) - posX;
								} else {
									renderXOffset += accFontRendererHook.get_errCorrector().getCorrectedPosX(posX, posY, width, gi.fontRes, asBold, true) - posX;
								}
							}
							break;
						case 2:
							right = (float) ((rasterizer.glyphWidthByte[ch] & 15) + 1);
							width = (right - left) / 2.0F;
					}

					if (CommonConfig.currentConfig.fontAlignBaseline) {
						renderYOffset += gi.baselineGap + rasterizer.sizeAdjPosY;
					} else {
						int fontId = rasterizer.fontId[ch];
						FontProperty fontProp = rasterizer.fontProp[1][fontId];
						renderYOffset += gi.baselineGap + fontProp.ascentGap;
					}

					texWidthPx = gi.drawingChWidth[ch % 256] * texture.scaleFactor;
					renderXOffset -= gi.fontOriginPosX;
				} else {
					if (glyphWidth[ch] == 0) {
						return null;
					}

					texture = fontTextureManager.bindTexture(unicodePageLocation);
					factor = texture.actualFontRes / 16.0F;
					left = (float) ((glyphWidth[ch] & 240) >>> 4);
					right = (float) ((glyphWidth[ch] & 15) + 1);
					width = (right - left) / 2.0F + 1.0F;
					texWidthPx = (right - left - 0.02F) * factor;
				}

				leftPx = left * factor;
				accFontRendererHook.resetTexEnvAndBlend();
				if (!CommonConfig.currentConfig.performanceMode) {
					fontTextureManager.setAnisotropicFilter(unicodePageLocation, smoothFontIntegration.anisotropicFilterEnabled);
					if (!smoothFontIntegration.exclusionCondUnicode) {
						accFontRendererHook.get_fontShader().setShaderParams(fontRendererHook, true);
						fontTextureManager.setTexParams(unicodePageLocation, FontRendererHook.texFilterSettingId);
					} else {
						fontTextureManager.setTexParamsNearest(unicodePageLocation);
					}
				} else {
					fontTextureManager.setTexParams(unicodePageLocation, FontRendererHook.texFilterSettingId);
				}

				next_render_x_offset = width;
			}

			float char_rendering_width;
			float char_rendering_height;
			float min_u, max_u;
			float min_v, max_v;
			{
				//renderCharCommon
				factor *= 2;
				float chImageSize = texture.chImageSizePx;
				float borderWidth = texture.borderWidthPx;
				int texSize = texture.texSizePx;
				float boxSize = chImageSize + borderWidth * 2.0F;
				float texX = (float) (ch % 16) * boxSize + borderWidth + leftPx;
				float texY = (float) ((ch & 255) / 16) * boxSize + borderWidth;

				min_u = (texX - borderWidth) / (float) texSize;
				max_u = (texX + texWidthPx + borderWidth) / (float) texSize;
				min_v = (texY - borderWidth) / (float) texSize;
				max_v = (texY + chImageSize + borderWidth) / (float) texSize;
				char_rendering_width = (texWidthPx + borderWidth * 2) / factor;
				char_rendering_height = (chImageSize + borderWidth * 2) / factor;

				renderXOffset -= borderWidth / factor;
				renderYOffset -= borderWidth / factor;
				if (!CommonConfig.currentConfig.performanceMode) {
					if (smoothFontIntegration.alignToPixelCond) {
						if (asShadow && accFontRendererHook.get_fontScale() == 1.0F && !mcDefaultFontFlag) {
							posY += 0.5F;
						}

						posY = accFontRendererHook.alignToPixel(posY);
					}

					//RenderingStringContextへ移動
//					if (asBold && fontRendererHook.roundedFontScale >= 3.0F) {
//						fontRendererHook.renderChar(posX - 0.25F, posY, texX, texY, texWidthPx, it, texSize, chImageSize, borderWidth, factor);
//					}
				} else if (asBold) {
					//RenderingStringContextへ移動
//					int scaleFactor = accFontRendererHook.getScaleFactor();
//					if (scaleFactor >= 3) {
//						fontRendererHook.renderChar(posX - 0.25F, posY, texX, texY, texWidthPx, it, texSize, chImageSize, borderWidth, factor);
//					}
				}
			}

			//renderChar
			if (asShadow) {
				posX += FontRendererHook.shadowAdjustVal;
				posY += FontRendererHook.shadowAdjustVal;
				centerY += FontRendererHook.shadowAdjustVal;
			}

			next_render_x_offset = applyDoDrawHook(fontRendererHook, next_render_x_offset);

			//greenとblueが逆なのはバニラが悪い
			RenderingCharContext charContext = new RenderingCharContext(context, ch, char_rendering_width, char_rendering_height, min_u, min_v, max_u, max_v, posX, posY, centerY, fontRenderer.red, fontRenderer.blue, fontRenderer.green, fontRenderer.alpha, next_render_x_offset);
			charContext.renderLeftTopX += renderXOffset;
			charContext.renderLeftBottomX += renderXOffset;
			charContext.renderRightTopX += renderXOffset;
			charContext.renderRightBottomX += renderXOffset;
			charContext.renderLeftTopY += renderYOffset;
			charContext.renderLeftBottomY += renderYOffset;
			charContext.renderRightTopY += renderYOffset;
			charContext.renderRightBottomY += renderYOffset;
			return charContext;
		}

		private static float applyDoDrawHook(FontRendererHook fontRendererHook, float xOffset) {
			if (fontRendererHook.changeFont) {
				switch (fontRendererHook.precisionMode) {
					case 0:
						break;//nothing to do
					case 1:
						xOffset = (float) FontUtils.toNormalWidth(xOffset);
						break;
					case 2:
						xOffset = (float) ((int) xOffset);
						break;
				}
			}
			return xOffset;
		}

	}
}
