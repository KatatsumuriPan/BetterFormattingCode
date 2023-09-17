package kpan.better_fc.implementations.formattingcode.vanilla;

import kpan.better_fc.api.IRenderingEffectFancyStyle;
import kpan.better_fc.api.contexts.chara.PreparingContext;
import net.minecraft.client.gui.FontRenderer;

import java.util.Random;

public class RenderingEffectRandomChar implements IRenderingEffectFancyStyle {
	public static final String RANDOM_CHARS = "ÀÁÂÈÊËÍÓÔÕÚßãõğİıŒœŞşŴŵžȇ\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000ÇüéâäàåçêëèïîìÄÅÉæÆôöòûùÿÖÜø£Ø×ƒáíóúñÑªº¿®¬½¼¡«»░▒▓│┤╡╢╖╕╣║╗╝╜╛┐└┴┬├─┼╞╟╚╔╩╦╠═╬╧╨╤╥╙╘╒╓╫╪┘┌█▄▌▐▀αβΓπΣσμτΦΘΩδ∞∅∈∩≡±≥≤⌠⌡÷≈°∙·√ⁿ²■\u0000";

	public static final RenderingEffectRandomChar INSTANCE = new RenderingEffectRandomChar();

	private RenderingEffectRandomChar() { }

	@Override
	public boolean prepare(PreparingContext context) {
		if (context.charToRender == '\n')
			return false;
		FontRenderer fontRenderer = context.fontRenderer;
		Random random = fontRenderer.fontRandom;
		//EditMode時には空白の変換を行わない(sizeStringToWidthの精度を高めるため)
		if (!context.isEdit || context.charToRender != ' ' && random.nextInt(20) == 0) {
			int index = RANDOM_CHARS.indexOf(context.charToRender);
			if (index != -1) {
				float char_width = fontRenderer.getCharWidth(context.charToRender);
				char new_char;
				do {
					int i = random.nextInt(RANDOM_CHARS.length());
					new_char = RANDOM_CHARS.charAt(i);
				}
				while (char_width != fontRenderer.getCharWidth(new_char));
				context.charToRender = new_char;
			}
		}
		return false;
	}
	@Override
	public int priority() { return 100000; }

}
