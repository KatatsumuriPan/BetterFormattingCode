package kpan.better_fc.asm.tf.gtceu;

import kpan.better_fc.asm.core.AsmTypes;
import kpan.better_fc.asm.core.AsmUtil;
import kpan.better_fc.asm.core.adapters.Instructions;
import kpan.better_fc.asm.core.adapters.MyClassVisitor;
import kpan.better_fc.asm.core.adapters.ReplaceInstructionsAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

public class TF_ClipboardBehavior {

	private static final String TARGET = "gregtech.common.items.behaviors.ClipboardBehavior";
	private static final String HOOK = AsmTypes.HOOK + "gtceu/" + "HK_" + "ClipboardBehavior";

	private static final String TEXTFIELDWIDGET2 = "gregtech.api.gui.widgets.TextFieldWidget2";

	public static ClassVisitor appendVisitor(ClassVisitor cv, String className) {
		if (!TARGET.equals(className))
			return cv;
		MyClassVisitor newcv = new MyClassVisitor(cv, className) {
			@Override
			public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
				MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
				if (name.equals("createUI") && desc.equals(AsmUtil.toMethodDesc("gregtech.api.gui.ModularUI", "gregtech.api.items.gui.PlayerInventoryHolder", AsmTypes.PLAYER))) {
					mv = new ReplaceInstructionsAdapter(mv, name + " title"
							, Instructions.create()
							.bipush(25)
							.labelRep()
							.invokeVirtual(TEXTFIELDWIDGET2, "setMaxLength", AsmUtil.toMethodDesc(TEXTFIELDWIDGET2, AsmTypes.INT))
							, Instructions.create()
							.invokeStatic(HOOK, "getTitleMaxLength", AsmUtil.toMethodDesc(AsmTypes.INT))
							.invokeVirtual(TEXTFIELDWIDGET2, "setMaxLength", AsmUtil.toMethodDesc(TEXTFIELDWIDGET2, AsmTypes.INT))
					);
					mv = new ReplaceInstructionsAdapter(mv, name + " line"
							, Instructions.create()
							.bipush(23)
							.labelRep()
							.invokeVirtual(TEXTFIELDWIDGET2, "setMaxLength", AsmUtil.toMethodDesc(TEXTFIELDWIDGET2, AsmTypes.INT))
							, Instructions.create()
							.invokeStatic(HOOK, "getLineMaxLength", AsmUtil.toMethodDesc(AsmTypes.INT))
							.invokeVirtual(TEXTFIELDWIDGET2, "setMaxLength", AsmUtil.toMethodDesc(TEXTFIELDWIDGET2, AsmTypes.INT))
					);
					success();
				}
				return mv;
			}
		};
		return newcv;
	}

}
