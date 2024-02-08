package kpan.better_fc.asm.tf;

import kpan.better_fc.asm.core.AsmTypes;
import kpan.better_fc.asm.core.AsmUtil;
import kpan.better_fc.asm.core.MyAsmNameRemapper.MethodRemap;
import kpan.better_fc.asm.core.adapters.InjectInstructionsAdapter;
import kpan.better_fc.asm.core.adapters.Instructions;
import kpan.better_fc.asm.core.adapters.Instructions.Instr;
import kpan.better_fc.asm.core.adapters.Instructions.OpcodeInt;
import kpan.better_fc.asm.core.adapters.Instructions.OpcodeMethod;
import kpan.better_fc.asm.core.adapters.MyClassVisitor;
import kpan.better_fc.asm.core.adapters.ReplaceInstructionsAdapter;
import kpan.better_fc.asm.core.adapters.ReplaceMethodAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import scala.tools.asm.Opcodes;

public class TF_GuiScreenBook {

	private static final String TARGET = "net.minecraft.client.gui.GuiScreenBook";
	private static final String HOOK = AsmTypes.HOOK + "HK_" + "GuiScreenBook";
	private static final MethodRemap keyTypedInBook = new MethodRemap(TARGET, "keyTypedInBook", AsmUtil.toMethodDesc(AsmTypes.VOID, AsmTypes.CHAR, AsmTypes.INT), "func_146463_b");
	private static final MethodRemap keyTypedInTitle = new MethodRemap(TARGET, "keyTypedInTitle", AsmUtil.toMethodDesc(AsmTypes.VOID, AsmTypes.CHAR, AsmTypes.INT), "func_146460_c");
	private static final MethodRemap pageInsertIntoCurrent = new MethodRemap(TARGET, "pageInsertIntoCurrent", AsmUtil.toMethodDesc(AsmTypes.VOID, AsmTypes.STRING), "func_146459_b");
	private static final MethodRemap drawScreen = new MethodRemap(TARGET, "drawScreen", AsmUtil.toMethodDesc(AsmTypes.VOID, AsmTypes.INT, AsmTypes.INT, AsmTypes.FLOAT), "func_73863_a");
	private static final MethodRemap getWordWrappedHeight = new MethodRemap(References.FontRenderer, "getWordWrappedHeight", AsmUtil.toMethodDesc(AsmTypes.INT, AsmTypes.STRING, AsmTypes.INT), "func_78267_b");
	private static final MethodRemap super_drawScreen = new MethodRemap("net/minecraft/client/gui/GuiScreen", "drawScreen", AsmUtil.toMethodDesc(AsmTypes.VOID, AsmTypes.INT, AsmTypes.INT, AsmTypes.FLOAT), "func_73863_a");

	public static ClassVisitor appendVisitor(ClassVisitor cv, String className) {
		if (!TARGET.equals(className))
			return cv;
		MyClassVisitor newcv = new MyClassVisitor(cv, className) {
			@Override
			public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
				MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
				if (keyTypedInBook.isTarget(name, desc)) {
					mv = InjectInstructionsAdapter.beforeAfter(mv, name
							, Instructions.create()
									.invokeStatic(References.isAllowedCharacter)
							, Instructions.create()
									.invokeStatic(HOOK, "preRender", AsmTypes.METHOD_VOID)
							, Instructions.create()
									.invokeStatic(HOOK, "postRender", AsmTypes.METHOD_VOID)
					);
					success();
				}
				if (keyTypedInTitle.isTarget(name, desc)) {
					mv = InjectInstructionsAdapter.beforeAfter(mv, name
							, Instructions.create()
									.invokeStatic(References.isAllowedCharacter)
							, Instructions.create()
									.invokeStatic(HOOK, "preRender", AsmTypes.METHOD_VOID)
							, Instructions.create()
									.invokeStatic(HOOK, "postRender", AsmTypes.METHOD_VOID)
					);
					mv = new ReplaceInstructionsAdapter(mv, name
							, Instructions.create()
							.intInsn(OpcodeInt.BIPUSH, 16)
							, Instructions.create()
							.invokeStatic(HOOK, "getTitleMaxLength", AsmUtil.toMethodDesc(AsmTypes.INT))
					);
					success();
				}
				if (pageInsertIntoCurrent.isTarget(name, desc)) {
					mv = InjectInstructionsAdapter.beforeAfter(mv, name + " render"
							, Instructions.create()
									.invokeVirtual(getWordWrappedHeight)
							, Instructions.create()
									.invokeStatic(HOOK, "preRender", AsmTypes.METHOD_VOID)
							, Instructions.create()
									.invokeStatic(HOOK, "postRender", AsmTypes.METHOD_VOID)
					);
					mv = new ReplaceInstructionsAdapter(mv, name + " getWordWrappedMaxHeight"
							, Instructions.create()
							.intInsn(OpcodeInt.SIPUSH, 128)
							, Instructions.create()
							.invokeStatic(HOOK, "getWordWrappedMaxHeight", "()I")
					);
					mv = new ReplaceInstructionsAdapter(mv, name + " getTextMaxLength"
							, Instructions.create()
							.intInsn(OpcodeInt.SIPUSH, 256)
							, Instructions.create()
							.invokeStatic(HOOK, "getTextMaxLength", "()I")
					);
					success();
				}
				return mv;
			}
		}.setSuccessExpected(3);
		newcv = new ReplaceMethodAdapter(newcv, drawScreen) {
			@Override
			protected void methodBody(MethodVisitor mv) {

				mv.visitVarInsn(Opcodes.ALOAD, 0);
				mv.visitVarInsn(Opcodes.ILOAD, 1);
				mv.visitVarInsn(Opcodes.ILOAD, 2);
				mv.visitVarInsn(Opcodes.FLOAD, 3);
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, HOOK, "drawScreen", AsmUtil.runtimeDesc(AsmUtil.toMethodDesc(AsmTypes.VOID, TARGET, AsmTypes.INT, AsmTypes.INT, AsmTypes.FLOAT)), false);

				mv.visitVarInsn(Opcodes.ALOAD, 0);
				mv.visitVarInsn(Opcodes.ILOAD, 1);
				mv.visitVarInsn(Opcodes.ILOAD, 2);
				mv.visitVarInsn(Opcodes.FLOAD, 3);
				Instr.methodInsn(OpcodeMethod.SPECIAL, super_drawScreen).visit(mv, null);

				mv.visitInsn(Opcodes.RETURN);
			}
		};
		return newcv;
	}
}
