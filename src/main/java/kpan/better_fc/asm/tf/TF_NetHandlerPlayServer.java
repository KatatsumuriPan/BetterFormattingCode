package kpan.better_fc.asm.tf;

import kpan.better_fc.asm.core.AsmTypes;
import kpan.better_fc.asm.core.AsmUtil;
import kpan.better_fc.asm.core.MyAsmNameRemapper.MethodRemap;
import kpan.better_fc.asm.core.adapters.InjectInstructionsAdapter;
import kpan.better_fc.asm.core.adapters.Instructions;
import kpan.better_fc.asm.core.adapters.Instructions.OpcodeInt;
import kpan.better_fc.asm.core.adapters.MyClassVisitor;
import kpan.better_fc.asm.core.adapters.ReplaceInstructionsAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class TF_NetHandlerPlayServer {

	private static final String TARGET = "net.minecraft.network.NetHandlerPlayServer";
	private static final String HOOK = AsmTypes.HOOK + "HK_" + "NetHandlerPlayServer";

	private static final MethodRemap processChatMessage = new MethodRemap(TARGET, "processChatMessage", AsmUtil.toMethodDesc(AsmTypes.VOID, "net.minecraft.network.play.client.CPacketChatMessage"), "func_147354_a");
	private static final MethodRemap processCustomPayload = new MethodRemap(TARGET, "processCustomPayload", AsmUtil.toMethodDesc(AsmTypes.VOID, "net.minecraft.network.play.client.CPacketCustomPayload"), "func_147349_a");
	private static final MethodRemap processUpdateSign = new MethodRemap(TARGET, "processUpdateSign", AsmUtil.toMethodDesc(AsmTypes.VOID, "net.minecraft.network.play.client.CPacketUpdateSign"), "func_147343_a");
	private static final MethodRemap getTextWithoutFormattingCodes = new MethodRemap("net.minecraft.util.text.TextFormatting", "getTextWithoutFormattingCodes", AsmUtil.toMethodDesc(AsmTypes.STRING, AsmTypes.STRING), "func_110646_a");

	public static ClassVisitor appendVisitor(ClassVisitor cv, String className) {
		if (!TARGET.equals(className))
			return cv;
		MyClassVisitor newcv = new MyClassVisitor(cv, className, 3) {
			@Override
			public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
				MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
				if (processChatMessage.isTarget(name, desc)) {//chat用
					mv = InjectInstructionsAdapter.beforeAfter(mv, name
							, Instructions.create().invokeStatic(References.isAllowedCharacter)
							, Instructions.create().invokeStatic(AsmTypes.HOOK + "HK_FontRenderer", "startEditMode", AsmTypes.METHOD_VOID)
							, Instructions.create().invokeStatic(AsmTypes.HOOK + "HK_FontRenderer", "endEditMode", AsmTypes.METHOD_VOID)
					);
					success();
				}
				if (processCustomPayload.isTarget(name, desc)) {
					mv = InjectInstructionsAdapter.beforeAfter(mv, name//repair用
							, Instructions.create().invokeStatic(References.filterAllowedCharacters)
							, Instructions.create().invokeStatic(AsmTypes.HOOK + "HK_FontRenderer", "startEditMode", AsmTypes.METHOD_VOID)
							, Instructions.create().invokeStatic(AsmTypes.HOOK + "HK_FontRenderer", "endEditMode", AsmTypes.METHOD_VOID)
					);
					mv = new ReplaceInstructionsAdapter(mv, name//repair用
							, Instructions.create()
							.invokeVirtual(AsmTypes.STRING, "length", "()I")
							.intInsn(OpcodeInt.BIPUSH, 35)
							, Instructions.create()
							.invokeVirtual(AsmTypes.STRING, "length", "()I")
							.invokeStatic(HOOK, "getItemNameMaxLength", "()I")
					);
					mv = InjectInstructionsAdapter.after(mv, name//commandblock用
							, Instructions.create()
									.ldcInsn("advMode.setCommand.success")
									.insn(Opcodes.ICONST_1)
									.typeInsn(Opcodes.ANEWARRAY, AsmTypes.OBJECT)
									.insn(Opcodes.DUP)
									.insn(Opcodes.ICONST_0)
									.rep()
							, Instructions.create()
									.invokeStatic(HOOK, "escapeChatCommandText", AsmUtil.toMethodDesc(AsmTypes.STRING, AsmTypes.STRING))
					).setSuccessExpected(2);
					success();
				}
				if (processUpdateSign.isTarget(name, desc)) {
					mv = new ReplaceInstructionsAdapter(mv, name//sign用
							, Instructions.create().invokeStatic(getTextWithoutFormattingCodes)
							, Instructions.create().invokeStatic(HOOK, "formatSignText", AsmUtil.toMethodDesc(AsmTypes.STRING, AsmTypes.STRING))
					);
					success();
				}

				return mv;
			}
		};
		return newcv;
	}

}
