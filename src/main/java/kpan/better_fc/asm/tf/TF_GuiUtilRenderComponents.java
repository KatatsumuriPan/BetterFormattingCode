package kpan.better_fc.asm.tf;

import kpan.better_fc.asm.core.AsmTypes;
import kpan.better_fc.asm.core.AsmUtil;
import kpan.better_fc.asm.core.MyAsmNameRemapper.MethodRemap;
import kpan.better_fc.asm.core.adapters.ReplaceRefMethodAdapter;
import org.objectweb.asm.ClassVisitor;

public class TF_GuiUtilRenderComponents {

	private static final String TARGET = "net.minecraft.client.gui.GuiUtilRenderComponents";
	private static final String HOOK = AsmTypes.HOOK + "HK_" + "GuiUtilRenderComponents";
	private static final MethodRemap splitText = new MethodRemap(TARGET, "splitText", AsmUtil.toMethodDesc(AsmTypes.LIST, "net.minecraft.util.text.ITextComponent", AsmTypes.INT, References.FontRenderer, AsmTypes.BOOL, AsmTypes.BOOL), "func_178908_a");

	public static ClassVisitor appendVisitor(ClassVisitor cv, String className) {
		if (!TARGET.equals(className))
			return cv;
		ClassVisitor newcv = new ReplaceRefMethodAdapter(cv, HOOK, splitText);
		return newcv;
	}
}
