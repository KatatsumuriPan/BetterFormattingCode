package kpan.better_fc.implementations;

import kpan.better_fc.api.FormattingCodesRegistry;
import kpan.better_fc.implementations.formattingcode.vanilla.FormattingCodeColor;
import kpan.better_fc.implementations.formattingcode.vanilla.FormattingCodeFancyStyle;
import kpan.better_fc.implementations.formattingcode.vanilla.FormattingCodeReset;
import kpan.better_fc.implementations.formattingcode.vanilla.RenderingEffectBold;
import kpan.better_fc.implementations.formattingcode.vanilla.RenderingEffectItalic;
import kpan.better_fc.implementations.formattingcode.vanilla.RenderingEffectRandomChar;
import kpan.better_fc.implementations.formattingcode.vanilla.RenderingEffectStrikeThrough;
import kpan.better_fc.implementations.formattingcode.vanilla.RenderingEffectUnderline;
import net.minecraft.util.text.TextFormatting;

public class FormattingCodeInit {

	public static void init() {
		for (int i = 0; i < 10; i++) {
			//noinspection DataFlowIssue
			FormattingCodesRegistry.register(new FormattingCodeColor(TextFormatting.fromColorIndex(i)), (char) ('0' + i));
		}
		for (int i = 10; i < 16; i++) {
			//noinspection DataFlowIssue
			FormattingCodeColor fcc = new FormattingCodeColor(TextFormatting.fromColorIndex(i));
			FormattingCodesRegistry.register(fcc, (char) ('a' + i - 10), (char) ('A' + i - 10));
		}
		FormattingCodesRegistry.register(FormattingCodeReset.INSTANCE, 'r');
		FormattingCodesRegistry.register(new FormattingCodeFancyStyle(RenderingEffectRandomChar.INSTANCE, "§k"), 'k', 'K');
		FormattingCodesRegistry.register(new FormattingCodeFancyStyle(RenderingEffectBold.INSTANCE, "§l"), 'l', 'L');
		FormattingCodesRegistry.register(new FormattingCodeFancyStyle(RenderingEffectStrikeThrough.INSTANCE, "§m"), 'm', 'M');
		FormattingCodesRegistry.register(new FormattingCodeFancyStyle(RenderingEffectUnderline.INSTANCE, "§n"), 'n', 'N');
		FormattingCodesRegistry.register(new FormattingCodeFancyStyle(RenderingEffectItalic.INSTANCE, "§o"), 'o', 'O');
	}
}
