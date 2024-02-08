package kpan.better_fc.asm.tf;

import kpan.better_fc.asm.core.AsmTypes;
import kpan.better_fc.asm.core.adapters.InjectInstructionsAdapter;
import kpan.better_fc.asm.core.adapters.Instructions;
import kpan.better_fc.asm.core.adapters.MyClassVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

public class TF_Loader {

	private static final String TARGET = "net.minecraftforge.fml.common.Loader";
	private static final String HOOK = AsmTypes.HOOK + "HK_" + "Loader";

	public static ClassVisitor appendVisitor(ClassVisitor cv, String className) {
		if (!TARGET.equals(className))
			return cv;
		MyClassVisitor newcv = new MyClassVisitor(cv, className) {
			@Override
			public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
				MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
				if (name.equals("<init>")) {
					mv = InjectInstructionsAdapter.injectBeforeReturns(mv, name
							, Instructions.create()
									.invokeStatic(HOOK, "onConstructed", AsmTypes.METHOD_VOID)
					);
					success();
				}
				return mv;
			}
		};
		return newcv;
	}
}
