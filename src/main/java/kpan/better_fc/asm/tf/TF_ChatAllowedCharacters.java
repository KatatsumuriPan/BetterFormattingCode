package kpan.better_fc.asm.tf;

import kpan.better_fc.asm.core.AsmTypes;
import kpan.better_fc.asm.core.AsmUtil;
import kpan.better_fc.asm.core.MyAsmNameRemapper.MethodRemap;
import kpan.better_fc.asm.core.adapters.ReplaceRefMethodAdapter;
import org.objectweb.asm.ClassVisitor;

public class TF_ChatAllowedCharacters {

	private static final String TARGET = "net.minecraft.util.ChatAllowedCharacters";
	private static final String HOOK = AsmTypes.HOOK + "HK_" + "ChatAllowedCharacters";
	private static final MethodRemap isAllowedCharacter = new MethodRemap(TARGET, "isAllowedCharacter", AsmUtil.toMethodDesc(AsmTypes.BOOL, AsmTypes.CHAR), "func_71566_a");

	public static ClassVisitor appendVisitor(ClassVisitor cv, String className) {
		if (!TARGET.equals(className))
			return cv;
		ClassVisitor newcv = new ReplaceRefMethodAdapter(cv, HOOK, isAllowedCharacter);
		return newcv;
	}
}
