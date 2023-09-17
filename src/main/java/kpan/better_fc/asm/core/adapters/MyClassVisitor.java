package kpan.better_fc.asm.core.adapters;

import kpan.better_fc.asm.core.AsmUtil;
import org.objectweb.asm.ClassVisitor;

public class MyClassVisitor extends ClassVisitor {

	public final String nameForDebug;
	private int successed = 0;
	private int successExpectedMinInclusive;
	private int successExpectedMaxInclusive;

	public MyClassVisitor(ClassVisitor cv, String nameForDebug) { this(cv, nameForDebug, 1); }
	public MyClassVisitor(ClassVisitor cv, String nameForDebug, int successExpected) { this(cv, nameForDebug, successExpected, successExpected); }
	public MyClassVisitor(ClassVisitor cv, String nameForDebug, int successExpectedMinInclusive, int successExpectedMaxInclusive) {
		super(AsmUtil.ASM_VER, cv);
		this.nameForDebug = nameForDebug;
		this.successExpectedMinInclusive = Math.max(successExpectedMinInclusive, 0);
		this.successExpectedMaxInclusive = Math.max(successExpectedMaxInclusive, 0);
	}
	@SuppressWarnings("unused")
	public void setSuccessExpectedMin(int minInclusive) { successExpectedMinInclusive = Math.max(minInclusive, 0); }
	@SuppressWarnings("unused")
	public void setSuccessExpectedMax(int maxInclusive) { successExpectedMaxInclusive = Math.max(maxInclusive, 0); }
	protected void success() {
		successed++;
	}

	@Override
	public void visitEnd() {
		super.visitEnd();
		if (successed < successExpectedMinInclusive || successed > successExpectedMaxInclusive) {
			if (successExpectedMinInclusive == successExpectedMaxInclusive)
				throw new RuntimeException("transform failed:" + nameForDebug + "\nexpected:" + successExpectedMinInclusive + "\nactual:" + successed);
			else if (successExpectedMaxInclusive == Integer.MAX_VALUE)
				throw new RuntimeException("transform failed:" + nameForDebug + "\nexpected: " + successExpectedMinInclusive + "~\nactual:" + successed);
			else
				throw new RuntimeException("transform failed:" + nameForDebug + "\nexpected: " + successExpectedMinInclusive + "~" + successExpectedMaxInclusive + "\nactual:" + successed);
		}
	}
}
