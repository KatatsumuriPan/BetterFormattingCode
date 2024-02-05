package kpan.better_fc.asm.acc;


import bre.smoothfont.BoldChecker;
import bre.smoothfont.ErrorCorrector;
import bre.smoothfont.FontRasterizer;
import bre.smoothfont.FontShader;
import bre.smoothfont.FontTextureManager;
import bre.smoothfont.RenderCharReplacedChecker;
import net.minecraft.util.ResourceLocation;

public interface ACC_FontRendererHook {
	RenderCharReplacedChecker get_renderCharReplacedChecker();
	FontTextureManager get_fontTextureManager();
	boolean get_renderStringAtPosInoperative();
	BoldChecker get_boldChecker();
	FontRasterizer get_rasterizer();
	ErrorCorrector get_errCorrector();
	ErrorCorrector get_errCorrectorShadow();
	FontShader get_fontShader();
	void set_renderStringAtPosWorked(boolean value);
	float get_fontScale();
	void set_fontScale(float value);
	void set_onRenderString(boolean value);
	void set_renderCharWorked(boolean value);
	static ResourceLocation get_osFontDefaultPageLocation() { return null; }

	void resetTexEnvAndBlend();
	ResourceLocation getOsFontUnicodePageLocation(int page);
	float alignToPixel(float pos);
	float getSpaceWidth();
	int getScaleFactor();
	void disableFeatures(String reason);
	void setLodBias();
	void setLodBiasPerformance();
	void setAlphaBlend(boolean reset);
	void setTexEnv(boolean reset);
}
