package kpan.better_fc.asm.tf;

import kpan.better_fc.asm.core.AsmTypes;
import kpan.better_fc.asm.core.adapters.MixinAccessorAdapter;
import org.objectweb.asm.ClassVisitor;

public class TF_FontRendererHook {

	private static final String TARGET = "bre.smoothfont.FontRendererHook";
	private static final String ACC = AsmTypes.ACC + "ACC_" + "FontRendererHook";

	public static ClassVisitor appendVisitor(ClassVisitor cv, String className) {
		if (!TARGET.equals(className))
			return cv;
		ClassVisitor newcv = new MixinAccessorAdapter(cv, className, ACC);
		return newcv;
	}
}
