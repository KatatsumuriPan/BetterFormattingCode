package kpan.better_fc.asm.tf;

import kpan.better_fc.asm.core.AsmTypes;
import kpan.better_fc.asm.core.adapters.Instructions;
import kpan.better_fc.asm.core.adapters.Instructions.Instr;
import kpan.better_fc.asm.core.adapters.Instructions.Instr.LookupSwitch;
import kpan.better_fc.asm.core.adapters.MixinAccessorAdapter;
import kpan.better_fc.asm.core.adapters.MyClassVisitor;
import kpan.better_fc.asm.core.adapters.ReplaceInstructionsAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import java.util.Arrays;

public class TF_FontRendererHook {

	private static final String TARGET = "bre.smoothfont.FontRendererHook";
	private static final String ACC = AsmTypes.ACC + "ACC_" + "FontRendererHook";

	public static ClassVisitor appendVisitor(ClassVisitor cv, String className) {
		if (!TARGET.equals(className))
			return cv;
		ClassVisitor newcv = new MyClassVisitor(cv, className) {
			@Override
			public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
				MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
				if (name.equals("getCharWidthFloat") && desc.equals("(C)F")) {
					mv = new ReplaceInstructionsAdapter(mv, name
							, Instructions.create()
							.addInstr(Instr.lookupSwitchRep())
							, instrs -> {
						LookupSwitch ls = (LookupSwitch) instrs.get(0);
						int[] keys = ls.getKeysCopy();
						Label[] labels = ls.getLabelsCopy();
						int index = Arrays.binarySearch(keys, '§');
						labels[index] = ls.getDefaulLabel();
						return Instructions.create(new LookupSwitch(ls.getDefaulLabel(), keys, labels));
					}
					).setSuccessExpectedMin(0);
					success();
				}
				return mv;
			}
		};
		newcv = new MixinAccessorAdapter(newcv, className, ACC);
		return newcv;
	}
}
