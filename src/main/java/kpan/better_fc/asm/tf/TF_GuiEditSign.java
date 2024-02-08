package kpan.better_fc.asm.tf;

import kpan.better_fc.asm.core.AsmTypes;
import kpan.better_fc.asm.core.AsmUtil;
import kpan.better_fc.asm.core.MyAsmNameRemapper.MethodRemap;
import kpan.better_fc.asm.core.adapters.InjectInstructionsAdapter;
import kpan.better_fc.asm.core.adapters.Instructions;
import kpan.better_fc.asm.core.adapters.MyClassVisitor;
import kpan.better_fc.asm.core.adapters.ReplaceRefMethodAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

public class TF_GuiEditSign {

	private static final String TARGET = "net.minecraft.client.gui.inventory.GuiEditSign";
	private static final String HOOK = AsmTypes.HOOK + "HK_" + "GuiEditSign";

	private static final MethodRemap keyTyped = new MethodRemap(TARGET, "keyTyped", AsmUtil.toMethodDesc(AsmTypes.VOID, AsmTypes.CHAR, AsmTypes.INT), "func_73869_a");
	private static final MethodRemap drawScreen = new MethodRemap(TARGET, "drawScreen", AsmUtil.toMethodDesc(AsmTypes.VOID, AsmTypes.INT, AsmTypes.INT, AsmTypes.FLOAT), "func_73863_a");
	private static final MethodRemap render = new MethodRemap("net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher", "render", AsmUtil.toMethodDesc(AsmTypes.VOID, AsmTypes.TILEENTITY, AsmTypes.DOUBLE, AsmTypes.DOUBLE, AsmTypes.DOUBLE, AsmTypes.FLOAT), "func_147549_a");

	public static ClassVisitor appendVisitor(ClassVisitor cv, String className) {
		if (!TARGET.equals(className))
			return cv;
		MyClassVisitor newcv = new MyClassVisitor(cv, className) {
			@Override
			public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
				MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
				if (drawScreen.isTarget(name, desc)) {
					mv = InjectInstructionsAdapter.beforeAfter(mv, name
							, Instructions.create()
									.invokeVirtual(render)
							, Instructions.create()
									.aload(0)
									.invokeStatic(HOOK, "preRender", AsmUtil.toMethodDesc(AsmTypes.VOID, TARGET))
							, Instructions.create()
									.invokeStatic(HOOK, "postRender", AsmUtil.toMethodDesc(AsmTypes.VOID))
					);
					success();
				}
				return mv;
			}
		};
		newcv = new ReplaceRefMethodAdapter(newcv, HOOK, keyTyped);
		return newcv;
	}

}
