package kpan.better_fc.asm.tf;

import kpan.better_fc.asm.core.AsmTypes;
import kpan.better_fc.asm.core.AsmUtil;
import kpan.better_fc.asm.core.MyAsmNameRemapper.MethodRemap;
import kpan.better_fc.asm.core.adapters.InjectInstructionsAdapter;
import kpan.better_fc.asm.core.adapters.Instructions;
import kpan.better_fc.asm.core.adapters.MyClassVisitor;
import kpan.better_fc.asm.core.adapters.ReplaceInstructionsAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

public class TF_GuiScreen {

	private static final String TARGET = "net.minecraft.client.gui.GuiScreen";
	private static final String HOOK = AsmTypes.HOOK + "HK_" + "GuiScreen";

	private static final MethodRemap handleKeyboardInput = new MethodRemap(TARGET, "handleKeyboardInput", AsmTypes.METHOD_VOID, "func_146282_l");
	private static final MethodRemap mouseClicked = new MethodRemap(TARGET, "mouseClicked", AsmUtil.toMethodDesc(AsmTypes.VOID, AsmTypes.INT, AsmTypes.INT, AsmTypes.INT), "func_73864_a");
	private static final MethodRemap keyTyped = new MethodRemap(TARGET, "keyTyped", AsmUtil.toMethodDesc(AsmTypes.VOID, AsmTypes.CHAR, AsmTypes.INT), "func_73869_a");

	public static ClassVisitor appendVisitor(ClassVisitor cv, String className) {
		if (!TARGET.equals(className))
			return cv;
		MyClassVisitor newcv = new MyClassVisitor(cv, className, 2) {
			@Override
			public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
				MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
				if (handleKeyboardInput.isTarget(name, desc)) {
					mv = new ReplaceInstructionsAdapter(mv, name
							, Instructions.create()
							.invokeVirtual(keyTyped)
							, Instructions.create()
							.invokeStatic(HOOK, "onKeyTyped", AsmUtil.toMethodDesc(AsmTypes.VOID, TARGET, AsmTypes.CHAR, AsmTypes.INT))
					);
					success();
				}
				if (mouseClicked.isTarget(name, desc)) {
					mv = InjectInstructionsAdapter.injectFirst(mv, name
							, Instructions.create()
									.invokeStatic(HOOK, "onMouseClicked", AsmUtil.toMethodDesc(AsmTypes.VOID))
					);
					success();
				}
				return mv;
			}
		};
		return newcv;
	}

}
