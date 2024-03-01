package kpan.better_fc.api.contexts.string;

import bre.smoothfont.FontRendererHook;
import bre.smoothfont.FontTextureManager;
import bre.smoothfont.GlStateManagerHelper;
import bre.smoothfont.RenderCharReplacedChecker;
import bre.smoothfont.config.CommonConfig;
import bre.smoothfont.config.GlobalConfig;
import bre.smoothfont.util.ModLib;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import kpan.better_fc.api.IStringRenderEndListener;
import kpan.better_fc.api.RenderFontUtil;
import kpan.better_fc.api.RenderingEffects;
import kpan.better_fc.api.contexts.chara.RenderingCharContext;
import kpan.better_fc.asm.acc.ACC_FontRendererHook;
import kpan.better_fc.asm.compat.CompatSmoothFont;
import kpan.better_fc.compat.smoothfont.CompatFontRenderer_SmoothFont;
import kpan.better_fc.util.CharArrayRingList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;

public class RenderingStringContext {

	public final FontRenderer fontRenderer;

	public final String originalText;
	public final boolean isEdit;
	public final int framebufferObject;

	public final boolean asShadow;
	public final float startX;
	public final float startY;

	public float posX;
	public float posY;

	public boolean stencilColorPrepared = false;

	@Nullable
	private final CharArrayRingList ringList;

	public final RenderingEffects effects = new RenderingEffects();
	public final ArrayList<IStringRenderEndListener> listners = new ArrayList<>();

	@Nullable
	public final SmoothFontIntegration smoothFontIntegration;

	public RenderingStringContext(FontRenderer fontRenderer, String text, float startX, float startY, boolean isEdit, boolean asShadow, int framebufferObject) {
		this.fontRenderer = fontRenderer;
		originalText = text;
		this.isEdit = isEdit;
		this.startX = posX = startX;
		this.startY = posY = startY;
		this.asShadow = asShadow;
		if (isEdit)
			ringList = null;
		else
			ringList = new CharArrayRingList();
		this.framebufferObject = framebufferObject;

		if (CompatSmoothFont.isLoaded()) {
			smoothFontIntegration = new SmoothFontIntegration(this);
			smoothFontIntegration.startRender();
		} else {
			smoothFontIntegration = null;
		}
	}

	public CharArrayRingList getRingList() {
		if (isEdit)
			throw new IllegalStateException("RingList is not available in EditMode!");
		return ringList;
	}
	@Nullable
	public CharArrayRingList tryGetRingList() {
		if (isEdit)
			return null;
		else
			return ringList;
	}
	public void onRenderEnd() {
		for (IStringRenderEndListener listner : listners) {
			listner.onRenderEnd(this);
		}
		if (smoothFontIntegration != null)
			smoothFontIntegration.endRender();
	}


	public static class SmoothFontIntegration {
		private static FloatBuffer floatBuf = BufferUtils.createFloatBuffer(16);
		public final RenderingStringContext context;
		public boolean exclusionCondDefault = false;
		public boolean exclusionCondUnicode = false;
		public boolean anisotropicFilterEnabled = false;
		public boolean alignToPixelCond = false;
		public boolean fractionCoord;

		public SmoothFontIntegration(RenderingStringContext context) {
			this.context = context;

		}

		public void startRender() {
			FontRenderer fontRenderer = context.fontRenderer;
			FontRendererHook fontRendererHook = CompatFontRenderer_SmoothFont.getFontRendererHook(fontRenderer);
			ACC_FontRendererHook accFontRendererHook = (ACC_FontRendererHook) fontRendererHook;
			accFontRendererHook.set_renderCharWorked(true);
			FontTextureManager fontTextureManager = accFontRendererHook.get_fontTextureManager();
			boolean unicodeFlag = fontRenderer.unicodeFlag;


			accFontRendererHook.set_renderStringAtPosWorked(true);
			fontRendererHook.thinFontFlag = unicodeFlag;
			if (!fontRendererHook.disableFeatures) {
				RenderCharReplacedChecker renderCharReplacedChecker = accFontRendererHook.get_renderCharReplacedChecker();
				if (renderCharReplacedChecker.needToCheck() && renderCharReplacedChecker.isReplaced(fontRenderer, context.originalText)) {
					accFontRendererHook.disableFeatures("renderChar methods might be replaced.");
				} else {
					fontRendererHook.thinFontFlag = unicodeFlag || fontRendererHook.changeFont;
					fontRendererHook.shadowFlag = context.asShadow;
					if (!CommonConfig.currentConfig.performanceMode) {
						Matrix4f mtxPrj;
						Matrix4f mtxMod;
						try {
							floatBuf.clear();
							GlStateManager.getFloat(GL11.GL_PROJECTION_MATRIX, floatBuf);
							floatBuf.rewind();
							mtxPrj = new Matrix4f();
							mtxPrj.load(floatBuf);
							floatBuf.clear();
							GlStateManager.getFloat(GL11.GL_MODELVIEW_MATRIX, floatBuf);
							floatBuf.rewind();
							mtxMod = new Matrix4f();
							mtxMod.load(floatBuf);
						} catch (Exception var10) {
							if (CommonConfig.globalConfig.debugLog) {
								var10.printStackTrace();
							}

							accFontRendererHook.disableFeatures(var10.getMessage());
							return;
						}

						float fontScaleX = ModLib.roundIf(Math.abs((float) Minecraft.getMinecraft().displayWidth * mtxPrj.m00 * mtxMod.m00), 1.0E-6F);
						float fontScaleY = ModLib.roundIf(Math.abs((float) Minecraft.getMinecraft().displayHeight * mtxPrj.m11 * mtxMod.m11), 1.0E-6F);
						accFontRendererHook.set_fontScale((fontScaleX + fontScaleY) / 4.0F);
						boolean rotation;
						if (mtxMod.m01 == 0.0F && mtxMod.m10 == 0.0F) {
							rotation = false;
						} else {
							rotation = true;
						}

						if (mtxPrj.m22 == -0.001F && mtxPrj.m32 == -2.0F && mtxMod.m32 == -2000.0F) {
							fontRendererHook.orthographic = true;
						} else {
							if (mtxPrj.m33 == 0.0F) {
								fontRendererHook.orthographic = false;
								float fontScale = accFontRendererHook.get_fontScale();
								fontScale /= Math.abs(mtxMod.m32);
								accFontRendererHook.set_fontScale(fontScale);
							} else {
								fontRendererHook.orthographic = true;
							}
						}

						float fontResUnicode = (float) fontTextureManager.getUnicodeFontRes(fontRendererHook.changeFont);
						float fontResDefault = (float) fontTextureManager.getDefaultFontRes(fontRenderer.locationFontTexture, fontRendererHook.changeFont);
						if (fontRendererHook.orthographic) {
							fractionCoord = false;
							if (mtxMod.m30 * 10.0F % 5.0F != 0.0F || mtxMod.m31 * 10.0F % 5.0F != 0.0F) {
								fractionCoord = true;
							}
						} else {
							fractionCoord = true;
						}

						fontRendererHook.roundedFontScale = ModLib.roundIf(accFontRendererHook.get_fontScale(), accFontRendererHook.get_fontScale() * CommonConfig.currentConfig.fontScaleRoundingToleranceRate);
						exclusionCondDefault = !context.asShadow || !(fontResDefault >= (float) CommonConfig.currentConfig.smoothShadowThreshold);
						exclusionCondUnicode = !context.asShadow || !(fontResUnicode >= (float) CommonConfig.currentConfig.smoothShadowThreshold);
						exclusionCondDefault &= fontRendererHook.orthographic && CommonConfig.currentConfig.excludeIntMultiple && fontRendererHook.roundedFontScale % (fontResDefault / 8.0F) == 0.0F || CommonConfig.currentConfig.excludeHighMag && (double) (fontRendererHook.roundedFontScale * 8.0F) >= (double) fontResDefault * CommonConfig.currentConfig.limitMagnification;
						exclusionCondUnicode &= fontRendererHook.orthographic && CommonConfig.currentConfig.excludeIntMultiple && fontRendererHook.roundedFontScale % (fontResUnicode / 8.0F) == 0.0F || CommonConfig.currentConfig.excludeHighMag && (double) (fontRendererHook.roundedFontScale * 8.0F) >= (double) fontResUnicode * CommonConfig.currentConfig.limitMagnification;
						alignToPixelCond = fontRendererHook.changeFont && fontRendererHook.orthographic && !rotation && fontRendererHook.roundedFontScale * 2.0F == fontResDefault / 8.0F;
						if (CommonConfig.currentConfig.enableInterpolation && (!exclusionCondDefault || !exclusionCondUnicode)) {
							accFontRendererHook.get_fontShader().prepareShader(fontRendererHook, fontRendererHook.changeFont && accFontRendererHook.get_rasterizer().grayScale);
							accFontRendererHook.get_fontShader().useShader(fontRendererHook);
						}

						if (CommonConfig.currentConfig.enableMipmap) {
							accFontRendererHook.setLodBias();
						}

						anisotropicFilterEnabled = !fontRendererHook.orthographic && CommonConfig.currentConfig.enableAnisotropicFilter;
					} else if (GlobalConfig.currentConfig.enableMipmap) {
						accFontRendererHook.setLodBiasPerformance();
					}

					accFontRendererHook.setAlphaBlend(false);
					accFontRendererHook.setTexEnv(false);
					accFontRendererHook.set_onRenderString(true);
				}
			}
		}

		public void renderBold(RenderingCharContext context, float offset) {
			FontRenderer fontRenderer = context.fontRenderer;
			FontRendererHook fontRendererHook = CompatFontRenderer_SmoothFont.getFontRendererHook(fontRenderer);
			float fontScale;
			if (!CommonConfig.currentConfig.performanceMode)
				fontScale = fontRendererHook.roundedFontScale;
			else
				fontScale = ((ACC_FontRendererHook) fontRendererHook).getScaleFactor();
			if (fontScale >= 3.0F) {
				RenderFontUtil.renderCharRaw(context.red, context.green, context.blue, context.alpha,
						context.minU, context.minV, context.maxU, context.maxV,
						context.posX + context.renderLeftTopX + offset - 0.25F, context.posY + context.renderLeftTopY, context.renderLeftTopZ,
						context.posX + context.renderLeftBottomX + offset - 0.25F, context.posY + context.renderLeftBottomY, context.renderLeftBottomZ,
						context.posX + context.renderRightTopX + offset - 0.25F, context.posY + context.renderRightTopY, context.renderRightTopZ,
						context.posX + context.renderRightBottomX + offset - 0.25F, context.posY + context.renderRightBottomY, context.renderRightBottomZ);
			}
		}

		public void endRender() {
			FontRenderer fontRenderer = context.fontRenderer;
			FontRendererHook fontRendererHook = CompatFontRenderer_SmoothFont.getFontRendererHook(fontRenderer);
			ACC_FontRendererHook accFontRendererHook = (ACC_FontRendererHook) fontRendererHook;
			if (!fontRendererHook.disableFeatures) {
				accFontRendererHook.set_onRenderString(false);
				GlStateManagerHelper.restoreGlTexEnvMode();
				GlStateManagerHelper.restoreBlendFunc(false);
				GlStateManagerHelper.restoreBlendEx(false);
				GlStateManagerHelper.restoreTexLodBias();
				accFontRendererHook.get_fontShader().restoreShader();
				GlStateManager.bindTexture(0);
			}
		}

		public void preStrikethroughRender() {
			FontRenderer fontRenderer = context.fontRenderer;
			FontRendererHook fontRendererHook = CompatFontRenderer_SmoothFont.getFontRendererHook(fontRenderer);
			if (!fontRendererHook.disableFeatures) {
				ACC_FontRendererHook accFontRendererHook = (ACC_FontRendererHook) fontRendererHook;
				accFontRendererHook.get_fontShader().restoreShaderTemporarily();
			}
		}

		public void postStrikethroughRender() {
			FontRenderer fontRenderer = context.fontRenderer;
			FontRendererHook fontRendererHook = CompatFontRenderer_SmoothFont.getFontRendererHook(fontRenderer);
			if (!fontRendererHook.disableFeatures) {
				ACC_FontRendererHook accFontRendererHook = (ACC_FontRendererHook) fontRendererHook;
				accFontRendererHook.get_fontShader().resetShader(fontRendererHook);
			}
		}
		public void preUnderlineRender() {
			FontRenderer fontRenderer = context.fontRenderer;
			FontRendererHook fontRendererHook = CompatFontRenderer_SmoothFont.getFontRendererHook(fontRenderer);
			if (!fontRendererHook.disableFeatures) {
				ACC_FontRendererHook accFontRendererHook = (ACC_FontRendererHook) fontRendererHook;
				accFontRendererHook.get_fontShader().restoreShaderTemporarily();
			}
		}

		public void postUnderlineRender() {
			FontRenderer fontRenderer = context.fontRenderer;
			FontRendererHook fontRendererHook = CompatFontRenderer_SmoothFont.getFontRendererHook(fontRenderer);
			if (!fontRendererHook.disableFeatures) {
				ACC_FontRendererHook accFontRendererHook = (ACC_FontRendererHook) fontRendererHook;
				accFontRendererHook.get_fontShader().resetShader(fontRendererHook);
			}
		}
	}
}
