package kpan.better_fc.asm.core.adapters;

import javax.annotation.Nullable;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

public abstract class AddMethodAdapter extends MyClassVisitor {

	protected final int access;
	protected final String runtimeName;
	protected final String runtimeDesc;
	@Nullable protected final String runtimeGenerics;
	@Nullable protected final String[] runtimeExceptions;

	public AddMethodAdapter(ClassVisitor cv, int access, String runtimeName, String runtimeDesc) {
		this(cv, access, runtimeName, runtimeDesc, null, null);
	}
	public AddMethodAdapter(ClassVisitor cv, int access, String runtimeName, String runtimeDesc, @Nullable String runtimeGenerics, @Nullable String[] runtimeExceptions) {
		super(cv, runtimeName + " " + runtimeDesc);
		this.access = access;
		this.runtimeName = runtimeName;
		this.runtimeDesc = runtimeDesc;
		this.runtimeGenerics = runtimeGenerics;
		this.runtimeExceptions = runtimeExceptions;
	}

	@Override
	public void visitEnd() {

		MethodVisitor mv = visitMethod(access, runtimeName, runtimeDesc, runtimeGenerics, runtimeExceptions);
		if (mv != null) {
			mv.visitCode();
			methodBody(mv);
			mv.visitMaxs(0, 0);//引数は無視され、再計算される(Write時に再計算されるのでtrace時点では0,0のまま)
			mv.visitEnd();
			success();
		}

		super.visitEnd();
	}

	protected abstract void methodBody(MethodVisitor mv);
}
