package kpan.better_fc.asm.tf;

import kpan.better_fc.asm.core.AsmTypes;
import kpan.better_fc.asm.core.AsmUtil;
import kpan.better_fc.asm.core.MyAsmNameRemapper.MethodRemap;
import kpan.better_fc.asm.core.adapters.Instructions;
import kpan.better_fc.asm.core.adapters.Instructions.OpcodeInt;
import kpan.better_fc.asm.core.adapters.MyClassVisitor;
import kpan.better_fc.asm.core.adapters.ReplaceInstructionsAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

public class TF_ItemWrittenBook {

	private static final String TARGET = "net.minecraft.item.ItemWrittenBook";
	private static final String HOOK = AsmTypes.HOOK + "HK_" + "ItemWrittenBook";
	private static final MethodRemap validBookTagContents = new MethodRemap(TARGET, "validBookTagContents", AsmUtil.toMethodDesc(AsmTypes.BOOL, AsmTypes.NBTTAGCOMPOUND), "func_77828_a");

	public static ClassVisitor appendVisitor(ClassVisitor cv, String className) {
		if (!TARGET.equals(className))
			return cv;
		MyClassVisitor newcv = new MyClassVisitor(cv, className) {
			@Override
			public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
				MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
				if (validBookTagContents.isTarget(name, desc)) {
					mv = new ReplaceInstructionsAdapter(mv, name
							, Instructions.create()
							.intInsn(OpcodeInt.BIPUSH, 32)
							, Instructions.create()
							.invokeStatic(HOOK, "getTitleMaxLength", AsmUtil.toMethodDesc(AsmTypes.INT))
					);
					success();
				}
				return mv;
			}
		};
		return newcv;
	}
}
