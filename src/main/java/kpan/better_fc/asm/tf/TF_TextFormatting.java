package kpan.better_fc.asm.tf;

import kpan.better_fc.asm.core.AsmTypes;
import kpan.better_fc.asm.core.AsmUtil;
import kpan.better_fc.asm.core.MyAsmNameRemapper.MethodRemap;
import kpan.better_fc.asm.core.adapters.ReplaceRefMethodAdapter;
import org.objectweb.asm.ClassVisitor;

public class TF_TextFormatting {

	private static final String TARGET = "net.minecraft.util.text.TextFormatting";
	private static final String HOOK = AsmTypes.HOOK + "HK_" + "TextFormatting";
	private static final MethodRemap getTextWithoutFormattingCodes = new MethodRemap(TARGET, "getTextWithoutFormattingCodes", AsmUtil.toMethodDesc(AsmTypes.STRING, AsmTypes.STRING), "func_110646_a");

	public static ClassVisitor appendVisitor(ClassVisitor cv, String className) {
		if (!TARGET.equals(className))
			return cv;
		ClassVisitor newcv = new ReplaceRefMethodAdapter(cv, HOOK, getTextWithoutFormattingCodes);
		return newcv;
	}
}
