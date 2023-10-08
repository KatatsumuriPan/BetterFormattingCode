package kpan.better_fc.asm.tf;

import kpan.better_fc.asm.core.AsmTypes;
import kpan.better_fc.asm.core.AsmUtil;
import kpan.better_fc.asm.core.MyAsmNameRemapper.MethodRemap;
import kpan.better_fc.asm.core.adapters.Instructions;
import kpan.better_fc.asm.core.adapters.MyClassVisitor;
import kpan.better_fc.asm.core.adapters.ReplaceInstructionsAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class TF_Render {

	private static final String TARGET = "net.minecraft.client.renderer.entity.Render";
	private static final String HOOK = AsmTypes.HOOK + "HK_" + "Render";
	private static final MethodRemap getTeamColor = new MethodRemap(TARGET, "getTeamColor", AsmUtil.toMethodDesc(AsmTypes.INT, AsmTypes.ENTITY), "func_188298_c");
	private static final MethodRemap getColorCode = new MethodRemap(References.FontRenderer, "getColorCode", AsmUtil.toMethodDesc(AsmTypes.INT, AsmTypes.CHAR), "func_175064_b");

	public static ClassVisitor appendVisitor(ClassVisitor cv, String className) {
		if (!TARGET.equals(className))
			return cv;
		MyClassVisitor newcv = new MyClassVisitor(cv, className) {
			@Override
			public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
				MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
				if (getTeamColor.isTarget(name, desc)) {
					mv = new ReplaceInstructionsAdapter(mv, name
							, Instructions.create()
							.aload(4)
							.insn(Opcodes.ICONST_1)
							.invokeVirtual(AsmTypes.STRING, "charAt", AsmUtil.toMethodDesc(AsmTypes.CHAR, AsmTypes.INT))
							.invokeVirtual(getColorCode)
							, Instructions.create()
							.aload(4)
							.invokeStatic(HOOK, "getColor", AsmUtil.toMethodDesc(AsmTypes.INT, References.FontRenderer, AsmTypes.STRING))
					);
					success();
				}
				return mv;
			}
		};
		return newcv;
	}
}
