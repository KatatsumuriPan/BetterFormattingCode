package kpan.better_fc.asm.tf;

import kpan.better_fc.asm.core.AsmTypes;
import kpan.better_fc.asm.core.AsmUtil;
import kpan.better_fc.asm.core.MyAsmNameRemapper.MethodRemap;
import kpan.better_fc.asm.core.adapters.InjectInstructionsAdapter;
import kpan.better_fc.asm.core.adapters.Instructions;
import kpan.better_fc.asm.core.adapters.Instructions.OpcodeInt;
import kpan.better_fc.asm.core.adapters.Instructions.OpcodeJump;
import kpan.better_fc.asm.core.adapters.MyClassVisitor;
import kpan.better_fc.asm.core.adapters.ReplaceInstructionsAdapter;
import kpan.better_fc.asm.core.adapters.ReplaceRefMethodAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class TF_FontRenderer {

	private static final String TARGET = "net.minecraft.client.gui.FontRenderer";
	private static final String HOOK = AsmTypes.HOOK + "HK_" + "FontRenderer";
	private static final MethodRemap renderStringAtPos = new MethodRemap(TARGET, "renderStringAtPos", AsmUtil.toMethodDesc(AsmTypes.VOID, AsmTypes.STRING, AsmTypes.BOOL), "func_78255_a");
	private static final MethodRemap getStringWidth = new MethodRemap(TARGET, "getStringWidth", AsmUtil.toMethodDesc(AsmTypes.INT, AsmTypes.STRING), "func_78256_a");
	private static final MethodRemap trimStringToWidth = new MethodRemap(TARGET, "trimStringToWidth", AsmUtil.toMethodDesc(AsmTypes.STRING, AsmTypes.STRING, AsmTypes.INT, AsmTypes.BOOL), "func_78262_a");
	private static final MethodRemap wrapFormattedStringToWidth = new MethodRemap(TARGET, "wrapFormattedStringToWidth", AsmUtil.toMethodDesc(AsmTypes.STRING, AsmTypes.STRING, AsmTypes.INT), "func_78280_d");
	private static final MethodRemap getFormatFromString = new MethodRemap(TARGET, "getFormatFromString", AsmUtil.toMethodDesc(AsmTypes.STRING, AsmTypes.STRING), "func_78282_e");
	private static final MethodRemap getCharWidth = new MethodRemap(TARGET, "getCharWidth", AsmUtil.toMethodDesc(AsmTypes.INT, AsmTypes.CHAR), "func_78263_a");

	public static ClassVisitor appendVisitor(ClassVisitor cv, String className) {
		if (!TARGET.equals(className))
			return cv;
		ClassVisitor newcv = new MyClassVisitor(cv, className) {
			@Override
			public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
				MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
				if (renderStringAtPos.isTarget(name, desc)) {
					Label label = new Label();
					mv = InjectInstructionsAdapter.injectFirst(mv, name + "(renderStringAtPos)"
							, Instructions.create()
									.aload(0)
									.aload(1)
									.iload(2)
									.invokeStatic(HOOK, "renderStringAtPos", AsmUtil.composeRuntimeMethodDesc(AsmTypes.BOOL, TARGET, AsmTypes.STRING, AsmTypes.BOOL))
									.jumpInsn(OpcodeJump.IFEQ, label)
									.insn(Opcodes.RETURN)
									.label(label)
					);
					success();
				}
				return mv;
			}
		};
		newcv = new MyClassVisitor(newcv, className) {
			@Override
			public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
				MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
				if (getCharWidth.isTarget(name, desc)) {
					mv = new ReplaceInstructionsAdapter(mv, name + "(getCharWidth)"
							, Instructions.create()
							.iload(1)
							.intInsn(OpcodeInt.SIPUSH, '§')
							.jumpRep(OpcodeJump.IF_ICMPNE)
							.labelRep()
							.insn(Opcodes.ICONST_M1)
							.insn(Opcodes.IRETURN)
							, Instructions.create()
					).setSuccessExpectedMin(0);
					success();
				}
				return mv;
			}
		};
		newcv = new MyClassVisitor(newcv, className) {
			@Override
			public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
				MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
				if (name.equals("getCharWidthFloat") && desc.equals("(C)F")) {
					mv = new ReplaceInstructionsAdapter(mv, name + "(getCharWidthFloat)"
							, Instructions.create()
							.iload(1)
							.intInsn(OpcodeInt.SIPUSH, '§')
							.jumpRep(OpcodeJump.IF_ICMPNE)
							.labelRep()
							.ldcInsn(-1.0f)
							.insn(Opcodes.FRETURN)
							, Instructions.create()
					);
					success();
				}
				return mv;
			}
		}.setSuccessExpected(0, 1);
		newcv = new ReplaceRefMethodAdapter(newcv, HOOK, getStringWidth);
		newcv = new ReplaceRefMethodAdapter(newcv, HOOK, trimStringToWidth);
		newcv = new ReplaceRefMethodAdapter(newcv, HOOK, wrapFormattedStringToWidth);
		newcv = new ReplaceRefMethodAdapter(newcv, HOOK, getFormatFromString);
		return newcv;
	}
}
