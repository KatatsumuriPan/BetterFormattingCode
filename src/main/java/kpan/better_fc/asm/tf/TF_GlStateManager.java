package kpan.better_fc.asm.tf;

import kpan.better_fc.asm.core.AsmTypes;
import kpan.better_fc.asm.core.AsmUtil;
import kpan.better_fc.asm.core.MyAsmNameRemapper.MethodRemap;
import kpan.better_fc.asm.core.adapters.InjectInstructionsAdapter;
import kpan.better_fc.asm.core.adapters.Instructions;
import kpan.better_fc.asm.core.adapters.MyClassVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

public class TF_GlStateManager {

	private static final String TARGET = "net.minecraft.client.renderer.GlStateManager";
	private static final String HOOK = AsmTypes.HOOK + "HK_" + "GlStateManager";
	private static final MethodRemap viewport = new MethodRemap(TARGET, "viewport", AsmUtil.toMethodDesc(AsmTypes.VOID, AsmTypes.INT, AsmTypes.INT, AsmTypes.INT, AsmTypes.INT), "func_179083_b");

	public static ClassVisitor appendVisitor(ClassVisitor cv, String className) {
		if (!TARGET.equals(className))
			return cv;
		MyClassVisitor newcv = new MyClassVisitor(cv, className, 1) {
			@Override
			public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
				MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
				if (viewport.isTarget(name, desc)) {
					mv = InjectInstructionsAdapter.injectFirst(mv, name
							, Instructions.create()
									.iload(0)
									.iload(1)
									.iload(2)
									.iload(3)
									.invokeStatic(HOOK, "onSetViewport", AsmUtil.toMethodDesc(AsmTypes.VOID, AsmTypes.INT, AsmTypes.INT, AsmTypes.INT, AsmTypes.INT))
					);
					success();
				}
				return mv;
			}
		};
		return newcv;
	}
}
