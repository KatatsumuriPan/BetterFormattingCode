package kpan.better_fc.asm.tf;

import kpan.better_fc.asm.core.AsmTypes;
import kpan.better_fc.asm.core.AsmUtil;
import kpan.better_fc.asm.core.MyAsmNameRemapper.FieldRemap;
import kpan.better_fc.asm.core.MyAsmNameRemapper.MethodRemap;
import kpan.better_fc.asm.core.adapters.InjectInstructionsAdapter;
import kpan.better_fc.asm.core.adapters.Instructions;
import kpan.better_fc.asm.core.adapters.MyClassVisitor;
import kpan.better_fc.asm.core.adapters.ReplaceInstructionsAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class TF_GuiWorldEdit {

	private static final String TARGET = "net.minecraft.client.gui.GuiWorldEdit";
	private static final String HOOK = AsmTypes.HOOK + "HK_" + "GuiWorldEdit";

	private static final FieldRemap nameEdit = new FieldRemap(TARGET, "nameEdit", References.GuiTextField, "field_184859_f");
	private static final MethodRemap initGui = new MethodRemap(TARGET, "initGui", AsmUtil.toMethodDesc(AsmTypes.VOID), "func_73866_w_");
	private static final MethodRemap actionPerformed = new MethodRemap(TARGET, "actionPerformed", AsmUtil.toMethodDesc(AsmTypes.VOID, "net.minecraft.client.gui.GuiButton"), "func_146284_a");

	public static ClassVisitor appendVisitor(ClassVisitor cv, String className) {
		if (!TARGET.equals(className))
			return cv;
		MyClassVisitor newcv = new MyClassVisitor(cv, className) {
			@Override
			public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
				MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
				if (initGui.isTarget(name, desc)) {
					mv = new ReplaceInstructionsAdapter(mv, name
							, Instructions.create()
							.typeInsn(Opcodes.NEW, References.GuiTextField)
							, Instructions.create()
							.typeInsn(Opcodes.NEW, References.ModifiedGuiTextField)
					);
					mv = new ReplaceInstructionsAdapter(mv, name
							, Instructions.create()
							.invokeSpecial(References.init)
							, Instructions.create()
							.invokeStatic(HOOK, "getSectionSignMode", AsmUtil.toMethodDesc(References.EnumSectionSignMode))
							.invokeSpecial(References.ModifiedGuiTextField, "<init>", AsmUtil.toMethodDesc(AsmTypes.VOID, AsmTypes.INT, References.FontRenderer, AsmTypes.INT, AsmTypes.INT, AsmTypes.INT, AsmTypes.INT, References.EnumSectionSignMode))
					);
					mv = InjectInstructionsAdapter.injectBeforeReturns(mv, name + " setMaxStringLength"
							, Instructions.create()
									.aload(0)
									.getField(nameEdit)
									.invokeStatic(HOOK, "getWorldNameMaxLength", AsmUtil.toMethodDesc(AsmTypes.INT))
									.invokeVirtual(References.setMaxStringLength)
					);
					mv = InjectInstructionsAdapter.before(mv, name + " removeLastResetCode"
							, Instructions.create()
									.invokeVirtual(References.setText)
							, Instructions.create()
									.invokeStatic(HOOK, "removeLastResetCode", AsmUtil.toMethodDesc(AsmTypes.STRING, AsmTypes.STRING))
					);
					success();
				}
				if (actionPerformed.isTarget(name, desc)) {
					mv = InjectInstructionsAdapter.after(mv, name + " appendResetCode"
							, Instructions.create()
									.invokeVirtual(References.getText)
							, Instructions.create()
									.invokeStatic(HOOK, "appendResetCode", AsmUtil.toMethodDesc(AsmTypes.STRING, AsmTypes.STRING))
					);
					success();
				}
				return mv;
			}
		}.setSuccessExpected(2);
		return newcv;
	}

}
