package kpan.better_fc.asm.tf;

import kpan.better_fc.asm.core.AsmTypes;
import kpan.better_fc.asm.core.AsmUtil;
import kpan.better_fc.asm.core.MyAsmNameRemapper.FieldRemap;
import kpan.better_fc.asm.core.MyAsmNameRemapper.MethodRemap;
import kpan.better_fc.asm.core.adapters.Instructions;
import kpan.better_fc.asm.core.adapters.MyClassVisitor;
import kpan.better_fc.asm.core.adapters.ReplaceInstructionsAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class TF_TileEntitySignRenderer {

	private static final String TARGET = "net.minecraft.client.renderer.tileentity.TileEntitySignRenderer";
	private static final String HOOK = AsmTypes.HOOK + "HK_" + "TileEntitySignRenderer";


	private static final MethodRemap render = new MethodRemap(TARGET, "render", AsmUtil.toMethodDesc(AsmTypes.VOID, "net.minecraft.tileentity.TileEntitySign", AsmTypes.DOUBLE, AsmTypes.DOUBLE, AsmTypes.DOUBLE, AsmTypes.FLOAT, AsmTypes.INT, AsmTypes.FLOAT), "func_192841_a");
	private static final FieldRemap signText = new FieldRemap("net/minecraft/tileentity/TileEntitySign", "signText", "[Lnet/minecraft/util/text/ITextComponent;", "field_145915_a");

	public static ClassVisitor appendVisitor(ClassVisitor cv, String className) {
		if (!TARGET.equals(className))
			return cv;
		MyClassVisitor newcv = new MyClassVisitor(cv, className) {
			@Override
			public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
				MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
				if (render.isTarget(name, desc)) {
					mv = new ReplaceInstructionsAdapter(mv, name
							, target()
							, replace()
					);
					success();
				}
				return mv;
			}
		};
		return newcv;
	}

	private static Instructions target() {
		//開発環境と実環境でバイトコードが異なる
		//おそらくRFGのせいだろう
		if (AsmUtil.isDeobfEnvironment())
			return targetInDev();
		else
			return targetInRuntime();
	}
	private static Instructions targetInDev() {
		Instructions instrs = Instructions.create();
		instrs.aload(1);
		instrs.getField(signText);
		instrs.iload(16);
		instrs.insn(Opcodes.AALOAD);
		instrs.rep();
		instrs.label(48);
		for (int i = 0; i < 7; i++) {
			instrs.rep();
		}
		instrs.label(49);
		for (int i = 0; i < 11; i++) {
			instrs.rep();
		}
		instrs.label(50);
		for (int i = 0; i < 1; i++) {
			instrs.rep();
		}
		instrs.label(51);
		for (int i = 0; i < 1; i++) {
			instrs.rep();
		}
		instrs.label(52);
		for (int i = 0; i < 4; i++) {
			instrs.rep();
		}
		instrs.label(54);
		for (int i = 0; i < 11; i++) {
			instrs.rep();
		}
		instrs.label(55);
		for (int i = 0; i < 21; i++) {
			instrs.rep();
		}
		instrs.label(53);
		for (int i = 0; i < 20; i++) {
			instrs.rep();
		}
		return instrs;
	}
	private static Instructions targetInRuntime() {
		Instructions instrs = Instructions.create();
		instrs.aload(1);
		instrs.getField(signText);
		instrs.iload(16);
		instrs.insn(Opcodes.AALOAD);
		instrs.rep();
		instrs.label(47);
		instrs.rep();
		instrs.label(46);
		for (int i = 0; i < 5; i++) {
			instrs.rep();
		}
		instrs.label(49);
		for (int i = 0; i < 7; i++) {
			instrs.rep();
		}
		instrs.label(50);
		for (int i = 0; i < 11; i++) {
			instrs.rep();
		}
		instrs.label(51);
		for (int i = 0; i < 1; i++) {
			instrs.rep();
		}
		instrs.label(52);
		for (int i = 0; i < 1; i++) {
			instrs.rep();
		}
		instrs.label(53);
		for (int i = 0; i < 4; i++) {
			instrs.rep();
		}
		instrs.label(55);
		for (int i = 0; i < 11; i++) {
			instrs.rep();
		}
		instrs.label(56);
		for (int i = 0; i < 21; i++) {
			instrs.rep();
		}
		instrs.label(54);
		for (int i = 0; i < 20; i++) {
			instrs.rep();
		}
		return instrs;
	}
	private static Instructions replace() {
		Instructions instrs = Instructions.create();
		instrs.aload(1);
		instrs.aload(13);
		instrs.iload(16);
		instrs.invokeStatic(HOOK, "onRenderText", AsmUtil.toMethodDesc(AsmTypes.VOID, "net/minecraft/tileentity/TileEntitySign", "net.minecraft.client.gui.FontRenderer", AsmTypes.INT));
		return instrs;
	}

}
