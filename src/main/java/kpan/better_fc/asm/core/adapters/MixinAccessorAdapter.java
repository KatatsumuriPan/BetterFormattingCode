package kpan.better_fc.asm.core.adapters;

import kpan.better_fc.asm.core.AccessTransformerForMixin;
import kpan.better_fc.asm.core.AsmTypes;
import kpan.better_fc.asm.core.AsmUtil;
import kpan.better_fc.asm.core.MyAsmNameRemapper;
import kpan.better_fc.asm.core.MyAsmNameRemapper.FieldRemap;
import kpan.better_fc.asm.core.MyAsmNameRemapper.MethodRemap;
import org.apache.commons.lang3.ArrayUtils;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;

@SuppressWarnings("unused")
public class MixinAccessorAdapter extends MyClassVisitor {

	private final String deobfTargetClassName;
	private final Class<?> accessor;
	private final HashMap<String, RuntimeInfo> fieldInfoMap = new HashMap<>();
	private final HashMap<String, MethodInfo> methodInfoMap = new HashMap<>();
	public MixinAccessorAdapter(ClassVisitor cv, String targetClassName, Class<?> accessor) {
		super(cv, targetClassName);
		deobfTargetClassName = targetClassName;
		this.accessor = accessor;
	}

	@Override
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
		super.visit(version, access, name, signature, superName, ArrayUtils.add(interfaces, accessor.getName().replace('.', '/')));
	}

	@Override
	public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
		fieldInfoMap.put(name, new RuntimeInfo(desc, (access & Opcodes.ACC_STATIC) != 0));
		return super.visitField(access, name, desc, signature, value);
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		methodInfoMap.put(name, new MethodInfo(desc, (access & Opcodes.ACC_STATIC) != 0, (access & Opcodes.ACC_PRIVATE) != 0));
		return super.visitMethod(access, name, desc, signature, exceptions);
	}

	@Override
	public void visitEnd() {
		for (Method method : accessor.getMethods()) {
			String methodName = method.getName();
			if (methodName.startsWith("get_")) {
				//getter
				String deobf_name = methodName.substring("get_".length());
				Class<?> type = method.getReturnType();
				boolean is_static = Modifier.isStatic(method.getModifiers());
				if (type == void.class)
					throw new IllegalStateException("Invalid getter");
				if (method.getParameterCount() != 0)
					throw new IllegalStateException("Invalid getter");

				String srg_name = method.getAnnotation(SrgName.class) != null ? method.getAnnotation(SrgName.class).value() : deobf_name;
				String runtime_name = MyAsmNameRemapper.runtimeField(new FieldRemap(deobfTargetClassName, deobf_name, type.getName(), srg_name));
				String runtime_desc = AsmUtil.runtimeDesc(AsmUtil.toDesc(type));
				if (false) {
					RuntimeInfo fieldInfo = fieldInfoMap.get(runtime_name);
					if (fieldInfo == null)
						throw new IllegalStateException("Unknown field");
					if (!fieldInfo.runtimeDesc.equals(runtime_desc))
						throw new IllegalStateException("Unmatched field type");
					if (fieldInfo.isStatic != is_static)
						throw new IllegalStateException("Unmatched field static");
				}

				MethodVisitor mv = visitMethod(Opcodes.ACC_PUBLIC | (is_static ? Opcodes.ACC_STATIC : 0), methodName, AsmUtil.toMethodDesc(runtime_desc), null, null);
				if (mv != null) {
					mv.visitCode();
					if (is_static) {
						mv.visitFieldInsn(Opcodes.GETSTATIC, MyAsmNameRemapper.runtimeClass(deobfTargetClassName), runtime_name, runtime_desc);
					} else {
						mv.visitVarInsn(Opcodes.ALOAD, 0);
						mv.visitFieldInsn(Opcodes.GETFIELD, MyAsmNameRemapper.runtimeClass(deobfTargetClassName), runtime_name, runtime_desc);
					}
					mv.visitInsn(AsmUtil.returnOpcode(runtime_desc));
					mv.visitMaxs(0, 0);//引数は無視され、再計算される(Write時に再計算されるのでtrace時点では0,0のまま)
					mv.visitEnd();
				}

			} else if (methodName.startsWith("set_")) {
				//setter
				String deobf_name = methodName.substring("set_".length());
				Class<?> type = method.getParameterTypes()[0];
				boolean is_static = Modifier.isStatic(method.getModifiers());
				if (method.getReturnType() != void.class)
					throw new IllegalStateException("Invalid setter");
				if (method.getParameterCount() != 1)
					throw new IllegalStateException("Invalid setter");

				String srg_name = method.getAnnotation(SrgName.class) != null ? method.getAnnotation(SrgName.class).value() : null;
				String runtime_name = MyAsmNameRemapper.runtimeField(new FieldRemap(deobfTargetClassName, deobf_name, type.getName(), srg_name));
				String runtime_desc = AsmUtil.runtimeDesc(AsmUtil.toDesc(type));
				if (false) {
					RuntimeInfo fieldInfo = fieldInfoMap.get(runtime_name);
					if (fieldInfo == null)
						throw new IllegalStateException("Unknown field");
					if (!fieldInfo.runtimeDesc.equals(runtime_desc))
						throw new IllegalStateException("Unmatched field type");
					if (fieldInfo.isStatic != is_static)
						throw new IllegalStateException("Unmatched field static");
				}

				MethodVisitor mv = visitMethod(Opcodes.ACC_PUBLIC | (is_static ? Opcodes.ACC_STATIC : 0), methodName, AsmUtil.toMethodDesc(AsmTypes.VOID, runtime_desc), null, null);
				if (mv != null) {
					mv.visitCode();
					if (is_static) {
						mv.visitVarInsn(AsmUtil.loadOpcode(runtime_desc), 0);
						mv.visitFieldInsn(Opcodes.PUTSTATIC, MyAsmNameRemapper.runtimeClass(deobfTargetClassName), runtime_name, runtime_desc);
					} else {
						mv.visitVarInsn(Opcodes.ALOAD, 0);
						mv.visitVarInsn(AsmUtil.loadOpcode(runtime_desc), 1);
						mv.visitFieldInsn(Opcodes.PUTFIELD, MyAsmNameRemapper.runtimeClass(deobfTargetClassName), runtime_name, runtime_desc);
					}
					mv.visitInsn(Opcodes.RETURN);
					mv.visitMaxs(0, 0);//引数は無視され、再計算される(Write時に再計算されるのでtrace時点では0,0のまま)
					mv.visitEnd();
				}
			} else {
				//bridge
				String method_desc = AsmUtil.toDesc(method);
				boolean is_static = Modifier.isStatic(method.getModifiers());

				String runtime_name;
				if (method.getAnnotation(SrgName.class) != null) {
					String srg_name = method.getAnnotation(SrgName.class).value();
					runtime_name = MyAsmNameRemapper.runtimeMethod(new MethodRemap(deobfTargetClassName, methodName, method_desc, srg_name));
				} else {
					runtime_name = methodName;
				}

				if (methodName.equals(runtime_name)) {
					AccessTransformerForMixin.toPublic(deobfTargetClassName, methodName, method_desc);
				} else {
					String runtime_desc = AsmUtil.runtimeDesc(method_desc);
					boolean is_private = false;//TODO
					if (false) {
						MethodInfo methodInfo = methodInfoMap.get(runtime_name);
						if (methodInfo == null)
							throw new IllegalStateException("Unknown method");
						if (!methodInfo.runtimeDesc.equals(runtime_desc))
							throw new IllegalStateException("Unmatched method desc");
						if (methodInfo.isStatic != is_static)
							throw new IllegalStateException("Unmatched method static");
						is_private = methodInfo.isPrivate;
					}

					MethodVisitor mv = visitMethod(Opcodes.ACC_PUBLIC | (is_static ? Opcodes.ACC_STATIC : 0), methodName, runtime_desc, null, null);
					if (mv != null) {
						mv.visitCode();
						int offset = 0;
						//this
						if (!is_static) {
							mv.visitVarInsn(Opcodes.ALOAD, 0);
							offset = 1;
						}
						//params
						for (int i = 0; i < method.getParameterCount(); i++) {
							mv.visitVarInsn(AsmUtil.loadOpcode(AsmUtil.toDesc(method.getParameterTypes()[i])), i + offset);
						}
						//invoke
						if (is_static)
							mv.visitMethodInsn(Opcodes.INVOKESTATIC, MyAsmNameRemapper.runtimeClass(deobfTargetClassName), runtime_name, runtime_desc, false);
						else if (is_private)
							mv.visitMethodInsn(Opcodes.INVOKESPECIAL, MyAsmNameRemapper.runtimeClass(deobfTargetClassName), runtime_name, runtime_desc, false);
						else
							mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, MyAsmNameRemapper.runtimeClass(deobfTargetClassName), runtime_name, runtime_desc, false);
						//return
						mv.visitInsn(AsmUtil.returnOpcode(AsmUtil.toDesc(method.getReturnType())));

						mv.visitMaxs(0, 0);//引数は無視され、再計算される(Write時に再計算されるのでtrace時点では0,0のまま)
						mv.visitEnd();
					}
				}
			}
		}
		success();
		super.visitEnd();
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface SrgName {
		String value() default "";
	}

	private static class RuntimeInfo {
		public final String runtimeDesc;
		public final boolean isStatic;

		private RuntimeInfo(String runtimeDesc, boolean isStatic) {
			this.runtimeDesc = runtimeDesc;
			this.isStatic = isStatic;
		}
	}

	private static class MethodInfo {
		public final String runtimeDesc;
		public final boolean isStatic;
		public final boolean isPrivate;

		private MethodInfo(String runtimeDesc, boolean isStatic, boolean isPrivate) {
			this.runtimeDesc = runtimeDesc;
			this.isStatic = isStatic;
			this.isPrivate = isPrivate;
		}
	}
}
