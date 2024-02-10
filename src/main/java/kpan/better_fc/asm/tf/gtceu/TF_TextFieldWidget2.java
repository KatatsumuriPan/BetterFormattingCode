package kpan.better_fc.asm.tf.gtceu;

import kpan.better_fc.asm.core.AsmTypes;
import kpan.better_fc.asm.core.AsmUtil;
import kpan.better_fc.asm.core.adapters.InjectInstructionsAdapter;
import kpan.better_fc.asm.core.adapters.Instructions;
import kpan.better_fc.asm.core.adapters.Instructions.OpcodeField;
import kpan.better_fc.asm.core.adapters.Instructions.OpcodeMethod;
import kpan.better_fc.asm.core.adapters.MyClassVisitor;
import kpan.better_fc.asm.core.adapters.ReplaceInstructionsAdapter;
import kpan.better_fc.asm.core.adapters.ReplaceMethodAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class TF_TextFieldWidget2 {

	private static final String TARGET = "gregtech.api.gui.widgets.TextFieldWidget2";
	private static final String HOOK = AsmTypes.HOOK + "gtceu/" + "HK_" + "TextFieldWidget2";

	public static ClassVisitor appendVisitor(ClassVisitor cv, String className) {
		if (!TARGET.equals(className))
			return cv;
		MyClassVisitor newcv = new MyClassVisitor(cv, className) {
			@Override
			public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
				MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
				if (name.equals("getTextX") && desc.equals(AsmUtil.toMethodDesc(AsmTypes.INT))) {
					mv = InjectInstructionsAdapter.injectFirst(mv, name + " start"
							, Instructions.create()
									.aload(0)
									.invokeStatic(HOOK, "start", AsmUtil.toMethodDesc(AsmTypes.VOID, TARGET))
					);
					mv = InjectInstructionsAdapter.injectBeforeReturns(mv, name + " end"
							, Instructions.create()
									.aload(0)
									.invokeStatic(HOOK, "end", AsmUtil.toMethodDesc(AsmTypes.VOID, TARGET))
					);
					success();
				}
				if (name.equals("drawInBackground") && desc.equals(AsmUtil.toMethodDesc(AsmTypes.VOID, AsmTypes.INT, AsmTypes.INT, AsmTypes.FLOAT, "gregtech.api.gui.IRenderContext"))) {
					mv = InjectInstructionsAdapter.injectFirst(mv, name + " start"
							, Instructions.create()
									.aload(0)
									.invokeStatic(HOOK, "start", AsmUtil.toMethodDesc(AsmTypes.VOID, TARGET))
					);
					mv = InjectInstructionsAdapter.injectBeforeReturns(mv, name + " end"
							, Instructions.create()
									.aload(0)
									.invokeStatic(HOOK, "end", AsmUtil.toMethodDesc(AsmTypes.VOID, TARGET))
					);
					mv = new ReplaceInstructionsAdapter(mv, name + " selectionBox"
							, targetSelectionBox()
							, Instructions.create()
							.aload(0)
							.aload(10)
							.aload(0)
							.fieldInsn(OpcodeField.GET, TARGET, "cursorPos", AsmTypes.INT)
							.aload(0)
							.fieldInsn(OpcodeField.GET, TARGET, "cursorPos2", AsmTypes.INT)
							.aload(0)
							.fieldInsn(OpcodeField.GET, TARGET, "scale", AsmTypes.FLOAT)
							.aload(0)
							.invokeVirtual(TARGET, "getTextX", AsmUtil.toMethodDesc(AsmTypes.INT))
							.invokeStatic(HOOK, "drawSelectionBox", AsmUtil.toMethodDesc(AsmTypes.VOID, TARGET, AsmTypes.STRING, AsmTypes.INT, AsmTypes.INT, AsmTypes.FLOAT, AsmTypes.INT))
					);
					mv = new ReplaceInstructionsAdapter(mv, name + " cursor"
							, targetCursor()
							, Instructions.create()
							.aload(0)
							.aload(10)
							.aload(0)
							.fieldInsn(OpcodeField.GET, TARGET, "cursorPos", AsmTypes.INT)
							.aload(0)
							.fieldInsn(OpcodeField.GET, TARGET, "scale", AsmTypes.FLOAT)
							.aload(0)
							.invokeVirtual(TARGET, "getTextX", AsmUtil.toMethodDesc(AsmTypes.INT))
							.invokeStatic(HOOK, "drawCursor", AsmUtil.toMethodDesc(AsmTypes.VOID, TARGET, AsmTypes.STRING, AsmTypes.INT, AsmTypes.FLOAT, AsmTypes.INT))
					);
					success();
				}
				if (name.equals("mouseClicked") && desc.equals(AsmUtil.toMethodDesc(AsmTypes.BOOL, AsmTypes.INT, AsmTypes.INT, AsmTypes.INT))) {
					mv = InjectInstructionsAdapter.injectFirst(mv, name + " start"
							, Instructions.create()
									.aload(0)
									.invokeStatic(HOOK, "start", AsmUtil.toMethodDesc(AsmTypes.VOID, TARGET))
					);
					mv = InjectInstructionsAdapter.injectBeforeReturns(mv, name + " end"
							, Instructions.create()
									.aload(0)
									.invokeStatic(HOOK, "end", AsmUtil.toMethodDesc(AsmTypes.VOID, TARGET))
					);
					success();
				}
				return mv;
			}
		}.setSuccessExpected(3);
		newcv = new ReplaceMethodAdapter(newcv, "getCursorPosFromMouse", AsmUtil.toMethodDesc(AsmTypes.INT, AsmTypes.INT)) {
			@Override
			protected void methodBody(MethodVisitor mv) {
				mv.visitVarInsn(Opcodes.ALOAD, 0);
				mv.visitVarInsn(Opcodes.ILOAD, 1);
				mv.visitVarInsn(Opcodes.ALOAD, 0);
				mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TARGET.replace('.', '/'), "getTextX", AsmUtil.toMethodDesc(AsmTypes.INT), false);
				mv.visitVarInsn(Opcodes.ALOAD, 0);
				mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TARGET.replace('.', '/'), "getRenderText", AsmUtil.toMethodDesc(AsmTypes.STRING), false);
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, HOOK, "getCursorPosFromMouse", AsmUtil.toMethodDesc(AsmTypes.INT, TARGET, AsmTypes.INT, AsmTypes.INT, AsmTypes.STRING), false);
				mv.visitInsn(Opcodes.IRETURN);
			}
		};
		return newcv;
	}

	private static Instructions targetSelectionBox() {
		return Instructions.create()
				.aload(5)
				.aload(10)
				.iconst0()
				.aload(0)
				.aload(0)
				.rep()//GETFIELD gregtech/api/gui/widgets/TextFieldWidget2.cursorPos : I
				.aload(0)
				.rep()//GETFIELD gregtech/api/gui/widgets/TextFieldWidget2.cursorPos2 : I
				.labelRep()//L16
				.methodRep(OpcodeMethod.STATIC, "java/lang/Math", "min")//INVOKESTATIC java/lang/Math.min (II)I
				.rep()//INVOKEVIRTUAL gregtech/api/gui/widgets/TextFieldWidget2.toRenderTextIndex (I)I
				.rep()//INVOKEVIRTUAL java/lang/String.substring (II)Ljava/lang/String;
				.rep()//INVOKEVIRTUAL net/minecraft/client/gui/FontRenderer.getStringWidth (Ljava/lang/String;)I
				.insn(Opcodes.I2F)
				.aload(0)
				.rep()//GETFIELD gregtech/api/gui/widgets/TextFieldWidget2.scale : F
				.insn(Opcodes.FMUL)
				.iload(7)
				.insn(Opcodes.I2F)
				.insn(Opcodes.FADD)
				.fstore(11)
				.labelRep()//L17
				.aload(0)
				.rep()//INVOKEVIRTUAL gregtech/api/gui/widgets/TextFieldWidget2.getSelectedText ()Ljava/lang/String;
				.astore(12)
				.labelRep()//L18
				.aload(5)
				.aload(12)
				.rep()//INVOKEVIRTUAL net/minecraft/client/gui/FontRenderer.getStringWidth (Ljava/lang/String;)I
				.insn(Opcodes.I2F)
				.fstore(13)
				.labelRep()//L19
				.aload(0)
				.fload(11)
				.fload(9)
				.insn(Opcodes.FMUL)
				.iload(6)
				.insn(Opcodes.I2F)
				.fload(13)
				.rep()//INVOKESPECIAL gregtech/api/gui/widgets/TextFieldWidget2.drawSelectionBox (FFF)V
				;
	}

	private static Instructions targetCursor() {
		return Instructions.create()
				.aload(10)
				.iconst0()
				.aload(0)
				.aload(0)
				.rep()//GETFIELD gregtech/api/gui/widgets/TextFieldWidget2.cursorPos : I
				.rep()//INVOKEVIRTUAL gregtech/api/gui/widgets/TextFieldWidget2.toRenderTextIndex (I)I
				.rep()//INVOKEVIRTUAL java/lang/String.substring (II)Ljava/lang/String;
				.astore(11)
				.labelRep()//L30
				.aload(5)
				.aload(11)
				.rep()//INVOKEVIRTUAL net/minecraft/client/gui/FontRenderer.getStringWidth (Ljava/lang/String;)I
				.insn(Opcodes.I2F)
				.aload(0)
				.rep()//GETFIELD gregtech/api/gui/widgets/TextFieldWidget2.scale : F
				.insn(Opcodes.FMUL)
				.iload(7)
				.insn(Opcodes.I2F)
				.insn(Opcodes.FADD)
				.fstore(12)
				.labelRep()//L31
				.fload(12)
				.fload(9)
				.insn(Opcodes.FMUL)
				.fstore(12)
				.labelRep()//L3
				.aload(0)
				.fload(12)
				.iload(6)
				.insn(Opcodes.I2F)
				.rep()//INVOKESPECIAL gregtech/api/gui/widgets/TextFieldWidget2.drawCursor (FF)V
				;
	}

}
