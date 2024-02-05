package kpan.better_fc.asm.tf;

import kpan.better_fc.asm.core.AsmTypes;
import kpan.better_fc.asm.core.AsmUtil;
import kpan.better_fc.asm.core.MyAsmNameRemapper.FieldRemap;
import kpan.better_fc.asm.core.MyAsmNameRemapper.MethodRemap;
import kpan.better_fc.asm.core.adapters.Instructions;
import kpan.better_fc.asm.core.adapters.Instructions.OpcodeInt;
import kpan.better_fc.asm.core.adapters.MyClassVisitor;
import kpan.better_fc.asm.core.adapters.ReplaceInstructionsAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class TF_GuiEditCommandBlockMinecart {

	private static final String TARGET = "net.minecraft.client.gui.inventory.GuiEditCommandBlockMinecart";
	private static final String HOOK = AsmTypes.HOOK + "HK_" + "GuiEditCommandBlockMinecart";

	private static final FieldRemap commandField = new FieldRemap(TARGET, "commandField", References.GuiTextField, "field_184088_a");
	private static final MethodRemap initGui = new MethodRemap(TARGET, "initGui", AsmUtil.toMethodDesc(AsmTypes.VOID), "func_73866_w_");

	public static ClassVisitor appendVisitor(ClassVisitor cv, String className) {
		if (!TARGET.equals(className))
			return cv;
		MyClassVisitor newcv = new MyClassVisitor(cv, className) {
			@Override
			public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
				MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
				if (initGui.isTarget(name, desc)) {
					mv = new ReplaceInstructionsAdapter(mv, name + " NEW"
							, Instructions.create()
							.typeInsn(Opcodes.NEW, References.GuiTextField)
							.insn(Opcodes.DUP)
							.insn(Opcodes.ICONST_2)
							, Instructions.create()
							.typeInsn(Opcodes.NEW, References.ModifiedGuiTextField)
							.insn(Opcodes.DUP)
							.insn(Opcodes.ICONST_2)
					);
					mv = new ReplaceInstructionsAdapter(mv, name + " init"
							, Instructions.create()
							.invokeSpecial(References.init)
							.putField(commandField)
							, Instructions.create()
							.invokeStatic(HOOK, "getSectionSignMode", AsmUtil.toMethodDesc(References.EnumSectionSignMode))
							.invokeSpecial(References.ModifiedGuiTextField, "<init>", AsmUtil.toMethodDesc(AsmTypes.VOID, AsmTypes.INT, References.FontRenderer, AsmTypes.INT, AsmTypes.INT, AsmTypes.INT, AsmTypes.INT, References.EnumSectionSignMode))
							.putField(commandField)
					);
					mv = new ReplaceInstructionsAdapter(mv, name + " setMaxStringLength"
							, Instructions.create()
							.getField(commandField)
							.intInsn(OpcodeInt.SIPUSH, 32500)
							.invokeVirtual(References.setMaxStringLength)
							, Instructions.create()
							.getField(commandField)
							.invokeStatic(HOOK, "getCommandMaxLength", AsmUtil.toMethodDesc(AsmTypes.INT))
							.invokeVirtual(References.setMaxStringLength)
					);
					success();
				}
				return mv;
			}
		};
		return newcv;
	}

}
