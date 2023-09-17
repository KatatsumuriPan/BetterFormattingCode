package kpan.better_fc.asm.core.adapters;

import kpan.better_fc.asm.core.AsmUtil;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Opcodes;

import javax.annotation.Nullable;

@SuppressWarnings("unused")
public class AddFieldAdapter extends MyClassVisitor {

	protected final int access;
	protected final String runtimeName;
	protected final String runtimeDesc;
	@Nullable
	protected final String runtimeGenerics;
	@Nullable
	protected final Object initValue;

	public AddFieldAdapter(ClassVisitor cv, String runtimeName, String deobfDesc) {
		this(cv, Opcodes.ACC_PUBLIC, runtimeName, deobfDesc, null, null);
	}
	public AddFieldAdapter(ClassVisitor cv, int access, String runtimeName, String deobfDesc) {
		this(cv, access, runtimeName, deobfDesc, null, null);
	}
	public AddFieldAdapter(ClassVisitor cv, int access, String runtimeName, String deobfDesc, Object initValue) {
		this(cv, access, runtimeName, deobfDesc, null, initValue);
	}
	public AddFieldAdapter(ClassVisitor cv, int access, String runtimeName, String deobfDesc, @Nullable String deobfGenerics, @Nullable Object initValue) {
		super(cv, runtimeName + " " + deobfDesc);
		this.access = access;
		this.runtimeName = runtimeName;
		runtimeDesc = AsmUtil.runtimeDesc(AsmUtil.toDesc(deobfDesc));
		runtimeGenerics = AsmUtil.runtimeDesc(deobfGenerics);
		this.initValue = initValue;
	}

	@Override
	public void visitEnd() {
		FieldVisitor fv = visitField(access, runtimeName, runtimeDesc, runtimeGenerics, initValue);
		if (fv != null)
			fv.visitEnd();
		success();
		super.visitEnd();
	}

}
