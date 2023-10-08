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

public class TF_TeleportToTeam$TeamSelectionObject {

	private static final String TARGET = "net.minecraft.client.gui.spectator.categories.TeleportToTeam$TeamSelectionObject";
	private static final String HOOK = AsmTypes.HOOK + "HK_" + "TeleportToTeam$TeamSelectionObject";
	private static final MethodRemap renderIcon = new MethodRemap(TARGET, "renderIcon", AsmUtil.toMethodDesc(AsmTypes.VOID, AsmTypes.FLOAT, AsmTypes.INT), "func_178663_a");
	private static final MethodRemap getColorCode = new MethodRemap(References.FontRenderer, "getColorCode", AsmUtil.toMethodDesc(AsmTypes.INT, AsmTypes.CHAR), "func_175064_b");

	public static ClassVisitor appendVisitor(ClassVisitor cv, String className) {
		if (!TARGET.equals(className))
			return cv;
		MyClassVisitor newcv = new MyClassVisitor(cv, className) {
			@Override
			public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
				MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
				if (renderIcon.isTarget(name, desc)) {
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
