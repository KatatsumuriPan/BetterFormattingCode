package kpan.better_fc.asm.core.adapters;

import kpan.better_fc.asm.core.AsmTypes.MethodDesc;
import kpan.better_fc.asm.core.AsmUtil;
import kpan.better_fc.asm.core.MyAsmNameRemapper;
import kpan.better_fc.asm.core.MyAsmNameRemapper.MethodRemap;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

@SuppressWarnings("unused")
public class AddRefMethodAdapter extends AddMethodAdapter {

	private final String runtimeTarget;
	private final String refMethodName;
	private final String runtimeRefMethodOwner;
	private final String runtimeReturnType;
	private final String[] runtimeParams;

	public AddRefMethodAdapter(ClassVisitor cv, String runtimeRefMethodOwner, int access, MethodRemap method) {
		super(cv, access, MyAsmNameRemapper.runtimeMethod(method), AsmUtil.runtimeDesc(method.deobfMethodDesc));
		refMethodName = method.mcpMethodName;
		runtimeTarget = MyAsmNameRemapper.runtimeClass(method.deobfOwner);
		this.runtimeRefMethodOwner = runtimeRefMethodOwner.replace('.', '/');
		MethodDesc md = MethodDesc.fromMethodDesc(AsmUtil.runtimeDesc(method.deobfMethodDesc));
		runtimeReturnType = md.returnDesc;
		runtimeParams = md.paramsDesc;
	}

	@Override
	protected void methodBody(MethodVisitor mv) {

		//java7との互換性のため、文字列switchを使用していない
		//this
		mv.visitVarInsn(Opcodes.ALOAD, 0);
		//params
		for (int i = 0; i < runtimeParams.length; i++) {
			mv.visitVarInsn(AsmUtil.loadOpcode(runtimeParams[i]), i + 1);
		}

		//invoke
		mv.visitMethodInsn(Opcodes.INVOKESTATIC, runtimeRefMethodOwner, refMethodName, AsmUtil.toMethodDesc(runtimeReturnType, runtimeTarget, runtimeParams), false);

		//return
		mv.visitInsn(AsmUtil.returnOpcode(runtimeReturnType));
	}

}
