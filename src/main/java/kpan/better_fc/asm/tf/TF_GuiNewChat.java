package kpan.better_fc.asm.tf;

import kpan.better_fc.asm.core.AsmTypes;
import kpan.better_fc.asm.core.AsmUtil;
import kpan.better_fc.asm.core.MyAsmNameRemapper.MethodRemap;
import kpan.better_fc.asm.core.adapters.InjectInstructionsAdapter;
import kpan.better_fc.asm.core.adapters.Instructions;
import kpan.better_fc.asm.core.adapters.MyClassVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

public class TF_GuiNewChat {

	private static final String TARGET = "net.minecraft.client.gui.GuiNewChat";
	private static final String HOOK = AsmTypes.HOOK + "HK_" + "GuiNewChat";

	private static final MethodRemap drawChat = new MethodRemap(TARGET, "drawChat", AsmUtil.toMethodDesc(AsmTypes.VOID, AsmTypes.INT), "func_146230_a");
	private static final MethodRemap getFormattedText = new MethodRemap("net.minecraft.util.text.ITextComponent", "getFormattedText", AsmUtil.toMethodDesc(AsmTypes.STRING), "func_150254_d");

	public static ClassVisitor appendVisitor(ClassVisitor cv, String className) {
		if (!TARGET.equals(className))
			return cv;
		MyClassVisitor newcv = new MyClassVisitor(cv, className) {
			@Override
			public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
				MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
				if (drawChat.isTarget(name, desc)) {
					mv = InjectInstructionsAdapter.after(mv, name
							, Instructions.create()
									.invokeInterface(getFormattedText)
							, Instructions.create()
									.invokeStatic(HOOK, "removeLastResetCode", AsmUtil.toMethodDesc(AsmTypes.STRING, AsmTypes.STRING))
					);
					success();
				}
				return mv;
			}
		};
		return newcv;
	}

}
