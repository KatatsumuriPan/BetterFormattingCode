package kpan.better_fc.asm.tf;

import kpan.better_fc.asm.core.AsmTypes;
import kpan.better_fc.asm.core.AsmUtil;
import kpan.better_fc.asm.core.MyAsmNameRemapper.MethodRemap;
import kpan.better_fc.asm.core.adapters.Instructions;
import kpan.better_fc.asm.core.adapters.Instructions.OpcodeInt;
import kpan.better_fc.asm.core.adapters.MyClassVisitor;
import kpan.better_fc.asm.core.adapters.ReplaceInstructionsAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

public class TF_CPacketChatMessage {

	private static final String TARGET = "net.minecraft.network.play.client.CPacketChatMessage";
	private static final String HOOK = AsmTypes.HOOK + "HK_" + "CPacketChatMessage";
	private static final MethodRemap readPacketData = new MethodRemap(TARGET, "readPacketData", AsmUtil.toMethodDesc(AsmTypes.VOID, "net.minecraft.network.PacketBuffer"), "func_148837_a");

	public static ClassVisitor appendVisitor(ClassVisitor cv, String className) {
		if (!TARGET.equals(className))
			return cv;
		MyClassVisitor newcv = new MyClassVisitor(cv, className) {
			@Override
			public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
				MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
				if (name.equals("<init>") && desc.equals(AsmUtil.toMethodDesc(AsmTypes.VOID, AsmTypes.STRING))) {
					mv = new ReplaceInstructionsAdapter(mv, name
							, Instructions.create()
							.intInsn(OpcodeInt.SIPUSH, 256)
							, Instructions.create()
							.invokeStatic(HOOK, "getChatMaxLength", "()I")
					).setSuccessExpected(2);
					success();
				}
				if (readPacketData.isTarget(name, desc)) {
					mv = new ReplaceInstructionsAdapter(mv, name
							, Instructions.create()
							.intInsn(OpcodeInt.SIPUSH, 256)
							, Instructions.create()
							.invokeStatic(HOOK, "getChatMaxLength", "()I")
					);
				}
				return mv;
			}
		};
		return newcv;
	}
}
