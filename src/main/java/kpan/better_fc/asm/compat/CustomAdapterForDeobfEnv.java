package kpan.better_fc.asm.compat;

import bre.smoothfont.asm.AsmHelper;
import bre.smoothfont.asm.CorePlugin;
import bre.smoothfont.asm.Transformer;
import bre.smoothfont.config.CommonConfig;
import bre.smoothfont.config.GlobalConfig;
import kpan.better_fc.asm.core.AsmTypes;
import kpan.better_fc.asm.core.AsmUtil;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AdviceAdapter;

import java.io.File;
import java.io.FileOutputStream;

public class CustomAdapterForDeobfEnv {

	public static byte[] transform(String name, String transformedName, byte[] bytes) {


		ClassReader cr;
		if (FMLLaunchHandler.side().isServer())
			return bytes;
		if (!accept(transformedName))
			return bytes;
		AsmHelper.logDebug("Internal name = " + name);
		AsmHelper.logDebug("Transformed name = " + transformedName);
		try {
			cr = new ClassReader(bytes);
		} catch (Exception e) {
			e.printStackTrace();
			cr = new ClassReader(bytes);
		}
		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		ClassVisitor cv = new CustomAdapterForDeobfEnv.ClassAdapter(cw, name, transformedName);
		cr.accept(cv, ClassReader.EXPAND_FRAMES);
		if (transformedName.equals("net.minecraft.client.gui.FontRenderer") &&
				GlobalConfig.hasDebugOption("dumpFontRenderer")) {
			try {
				FileOutputStream fos = new FileOutputStream("config" + File.separator + "smoothfont" + File.separator + "FontRenderer-orig.class");
				fos.write(bytes);
				fos.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				FileOutputStream fos = new FileOutputStream("config" + File.separator + "smoothfont" + File.separator + "FontRenderer-transformed.class");
				fos.write(cw.toByteArray());
				fos.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return cw.toByteArray();
	}
	private static boolean accept(String className) {
		for (TargetClass target : TargetClass.values()) {
			String name = target.getClassName();
			if (name.equals(className)) {
				AsmHelper.logger.info("Transform: " + className);
				return true;
			}
		}
		return false;
	}

	static String className;
	static String transformedName;

	public CustomAdapterForDeobfEnv() {
	}

	public static class GuiIngameForge {
		public GuiIngameForge() {
		}

		public static class RenderExperienceAdapter extends MethodVisitor {
			private boolean transCond = false;
			private int aload0Num = 0;
			private int popNum = 0;

			public RenderExperienceAdapter(MethodVisitor mv) {
				super(262144, mv);
			}

			@Override
			public void visitIntInsn(int opcode, int var) {
				if (opcode == 16 && var == 31) {
					transCond = true;
				}

				super.visitIntInsn(opcode, var);
			}

			@Override
			public void visitVarInsn(int opcode, int var) {
				if (transCond) {
					if (opcode == 25 && var == 0 && aload0Num < 4) {
						++aload0Num;
					}

					if (aload0Num == popNum + 1) {
						return;
					}
				}

				super.visitVarInsn(opcode, var);
			}

			@Override
			public void visitInsn(int opcode) {
				if (transCond) {
					if (opcode == 87 && popNum < 4) {
						++popNum;
						if (popNum == 4) {
							transCond = false;
							super.visitVarInsn(Opcodes.ALOAD, 0);
							super.visitFieldInsn(Opcodes.GETFIELD, "net/minecraftforge/client/GuiIngameForge", "fontrenderer", "Lnet/minecraft/client/gui/FontRenderer;");
							super.visitVarInsn(Opcodes.ALOAD, 7);
							super.visitVarInsn(Opcodes.ILOAD, 8);
							super.visitInsn(134);
							super.visitVarInsn(Opcodes.ILOAD, 9);
							super.visitInsn(134);
							super.visitMethodInsn(184, "bre/smoothfont/GuiIngameForgeHook", "renderExperienceHook", "(Lnet/minecraft/client/gui/FontRenderer;Ljava/lang/String;FF)V", false);
							AsmHelper.logDebug("Modified renderExperience method.");
							CustomAdapterForDeobfEnv.GuiIngameForgeMethods.RENDER_EXPERIENCE.setTransformed();
						}

						return;
					}

					if (aload0Num == popNum + 1) {
						return;
					}
				}

				super.visitInsn(opcode);
			}

			@Override
			public void visitFieldInsn(int opcode, String owner, String name, String desc) {
				if (!transCond || aload0Num != popNum + 1) {
					super.visitFieldInsn(opcode, owner, name, desc);
				}
			}

			@Override
			public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
				if (!transCond || aload0Num != popNum + 1) {
					super.visitMethodInsn(opcode, owner, name, desc, itf);
				}
			}
		}
	}

	public static class TextureManager {
		public TextureManager() {
		}

		public static class LoadTextureAdapter extends AdviceAdapter {
			public LoadTextureAdapter(MethodVisitor mv, int access, String name, String desc) {
				super(262144, mv, access, name, desc);
			}

			@Override
			public void onMethodExit(int opcode) {
				super.visitVarInsn(Opcodes.ALOAD, 1);
				super.visitMethodInsn(184, "bre/smoothfont/TextureManagerHook", "loadTextureExitHook", "(Lnet/minecraft/util/ResourceLocation;)V", false);
				AsmHelper.logDebug("Inserted the ExitHook in loadTexture() method.");
				CustomAdapterForDeobfEnv.TextureManagerMethods.LOAD_TEXTURE.setTransformed();
			}
		}
	}

	public static class ScaledResolution {
		public ScaledResolution() {
		}

		public static class ConstructorAdapter extends MethodVisitor {
			public ConstructorAdapter(MethodVisitor mv) {
				super(262144, mv);
			}

			@Override
			public void visitVarInsn(int opcode, int var) {
				super.visitVarInsn(opcode, var);
				if (opcode == 54 && var == 2) {
					super.visitVarInsn(Opcodes.ILOAD, 2);
					super.visitMethodInsn(184, "bre/smoothfont/ScaledResolutionHook", "modifyFlag", "(Z)Z", false);
					super.visitVarInsn(54, 2);
					AsmHelper.logDebug("Inserted the hook in ScaledResolution.<init>.");
					CustomAdapterForDeobfEnv.ScaledResolutionMethods.INIT.setTransformed();
				}

			}
		}
	}

	public static class FontRenderer {
		public FontRenderer() {
		}

		public static class GetCharWidthFloatAdapter extends AdviceAdapter {
			boolean ldcDefaultGlyphChars = false;
			int ldcDefaultGlyphCharsCounter = 0;

			public GetCharWidthFloatAdapter(MethodVisitor mv, int access, String name, String desc) {
				super(262144, mv, access, name, desc);
			}

			@Override
			public void visitLdcInsn(Object cst) {
				if (cst instanceof String) {
					String str = (String) cst;
					if (str.startsWith("ÀÁÂ")) {
						++ldcDefaultGlyphCharsCounter;
						if (ldcDefaultGlyphCharsCounter == 1) {
							ldcDefaultGlyphChars = true;
							super.visitVarInsn(Opcodes.ALOAD, 0);
							super.visitFieldInsn(Opcodes.GETFIELD, "net/minecraft/client/gui/FontRenderer", "fontRendererHook", "Lbre/smoothfont/FontRendererHook;");
							return;
						}
					}
				}

				super.visitLdcInsn(cst);
			}

			@Override
			public void visitMethodInsn(int opcode, String owner, String name, String desc) {
				if (ldcDefaultGlyphChars && opcode == 182 && name.equals("indexOf")) {
					ldcDefaultGlyphChars = false;
					super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "bre/smoothfont/FontRendererHook", "getCharWidthFloatGetCharIndexHook", "(C)I");
					AsmHelper.logDebug("Inserted getCharWidthFloatGetCharIndexHook() in getCharWidthFloat().");
					CustomAdapterForDeobfEnv.FontRendererMethods.GET_CHAR_WIDTH_FLOAT.setTransformed();
				} else {
					super.visitMethodInsn(opcode, owner, name, desc);
				}
			}
		}

		public static class BindTextureAdapter extends AdviceAdapter {
			public BindTextureAdapter(MethodVisitor mv, int access, String name, String desc) {
				super(262144, mv, access, name, desc);
			}

			@Override
			public void onMethodEnter() {
				super.visitVarInsn(Opcodes.ALOAD, 0);
				super.visitFieldInsn(Opcodes.GETFIELD, "net/minecraft/client/gui/FontRenderer", "fontRendererHook", "Lbre/smoothfont/FontRendererHook;");
				super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "bre/smoothfont/FontRendererHook", "bindTextureEnterHook", "()V", false);
				AsmHelper.logDebug("Inserted the EnterHook in bindTexture method.");
				CustomAdapterForDeobfEnv.FontRendererMethods.BIND_TEXTURE.setTransformed();
			}
		}

		public static class SizeStringToWidthAdapter extends AdviceAdapter {
			public SizeStringToWidthAdapter(MethodVisitor mv, int access, String name, String desc) {
				super(262144, mv, access, name, desc);
			}

			@Override
			public void onMethodEnter() {
				Label L1 = new Label();
				super.visitVarInsn(Opcodes.ALOAD, 0);
				super.visitFieldInsn(Opcodes.GETFIELD, "net/minecraft/client/gui/FontRenderer", "fontRendererHook", "Lbre/smoothfont/FontRendererHook;");
				super.visitFieldInsn(Opcodes.GETFIELD, "bre/smoothfont/FontRendererHook", "enableHookSizeStringToWidth", "Z");
				super.visitJumpInsn(Opcodes.IFEQ, L1);
				super.visitVarInsn(Opcodes.ALOAD, 0);
				super.visitFieldInsn(Opcodes.GETFIELD, "net/minecraft/client/gui/FontRenderer", "fontRendererHook", "Lbre/smoothfont/FontRendererHook;");
				super.visitFieldInsn(Opcodes.GETFIELD, "bre/smoothfont/FontRendererHook", "disableFeatures", "Z");
				super.visitJumpInsn(Opcodes.IFNE, L1);
				super.visitVarInsn(Opcodes.ALOAD, 0);
				super.visitFieldInsn(Opcodes.GETFIELD, "net/minecraft/client/gui/FontRenderer", "fontRendererHook", "Lbre/smoothfont/FontRendererHook;");
				super.visitVarInsn(Opcodes.ALOAD, 1);
				super.visitVarInsn(Opcodes.ILOAD, 2);
				super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "bre/smoothfont/FontRendererHook", "sizeStringToWidthFloatHook", "(Ljava/lang/String;I)I", false);
				super.visitInsn(Opcodes.IRETURN);
				super.visitLabel(L1);
				AsmHelper.logDebug("Inserted the EnterHook in sizeStringToWidth method.");
				CustomAdapterForDeobfEnv.FontRendererMethods.SIZE_STRING_TO_WIDTH.setTransformed();
			}
		}

		public static class SetUnicodeFlagAdapter extends AdviceAdapter {
			public SetUnicodeFlagAdapter(MethodVisitor mv, int access, String name, String desc) {
				super(262144, mv, access, name, desc);
			}

			@Override
			public void onMethodExit(int opcode) {
				super.visitVarInsn(Opcodes.ALOAD, 0);
				super.visitVarInsn(Opcodes.ALOAD, 0);
				super.visitFieldInsn(Opcodes.GETFIELD, "net/minecraft/client/gui/FontRenderer", "fontRendererHook", "Lbre/smoothfont/FontRendererHook;");
				super.visitVarInsn(Opcodes.ILOAD, 1);
				super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "bre/smoothfont/FontRendererHook", "setUnicodeFlagHook", "(Z)Z", false);
				super.visitFieldInsn(Opcodes.PUTFIELD, "net/minecraft/client/gui/FontRenderer", "unicodeFlag", "Z");
				AsmHelper.logDebug("Inserted the ExitHook in setUnicodeFlag method.");
				CustomAdapterForDeobfEnv.FontRendererMethods.SET_UNICODE_FLAG.setTransformed();
			}
		}

		public static class TrimStringToWidthAdapter extends AdviceAdapter {
			public TrimStringToWidthAdapter(MethodVisitor mv, int access, String name, String desc) {
				super(262144, mv, access, name, desc);
			}

			@Override
			public void onMethodEnter() {
				Label L1 = new Label();
				super.visitVarInsn(Opcodes.ALOAD, 0);
				super.visitFieldInsn(Opcodes.GETFIELD, "net/minecraft/client/gui/FontRenderer", "fontRendererHook", "Lbre/smoothfont/FontRendererHook;");
				super.visitFieldInsn(Opcodes.GETFIELD, "bre/smoothfont/FontRendererHook", "enableHookTrimStringToWidth", "Z");
				super.visitJumpInsn(Opcodes.IFEQ, L1);
				super.visitVarInsn(Opcodes.ALOAD, 0);
				super.visitFieldInsn(Opcodes.GETFIELD, "net/minecraft/client/gui/FontRenderer", "fontRendererHook", "Lbre/smoothfont/FontRendererHook;");
				super.visitFieldInsn(Opcodes.GETFIELD, "bre/smoothfont/FontRendererHook", "disableFeatures", "Z");
				super.visitJumpInsn(Opcodes.IFNE, L1);
				super.visitVarInsn(Opcodes.ALOAD, 0);
				super.visitFieldInsn(Opcodes.GETFIELD, "net/minecraft/client/gui/FontRenderer", "fontRendererHook", "Lbre/smoothfont/FontRendererHook;");
				super.visitVarInsn(Opcodes.ALOAD, 1);
				super.visitVarInsn(Opcodes.ILOAD, 2);
				super.visitVarInsn(Opcodes.ILOAD, 3);
				super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "bre/smoothfont/FontRendererHook", "trimStringToWidthFloatHook", "(Ljava/lang/String;IZ)Ljava/lang/String;", false);
				super.visitInsn(Opcodes.ARETURN);
				super.visitLabel(L1);
				AsmHelper.logDebug("Inserted the EnterHook in trimStringToWidth method.");
				CustomAdapterForDeobfEnv.FontRendererMethods.TRIM_STRING_TO_WIDTH.setTransformed();
			}
		}

		public static class GetCharWidthAdapter extends AdviceAdapter {
			private boolean transformed1 = false;
			private boolean transformed2 = false;
			boolean ldcDefaultGlyphChars = false;
			int ldcDefaultGlyphCharsCounter = 0;

			public GetCharWidthAdapter(MethodVisitor mv, int access, String name, String desc) {
				super(262144, mv, access, name, desc);
			}

			@Override
			public void onMethodEnter() {
				Label L1 = new Label();
				super.visitVarInsn(Opcodes.ALOAD, 0);
				super.visitFieldInsn(Opcodes.GETFIELD, "net/minecraft/client/gui/FontRenderer", "fontRendererHook", "Lbre/smoothfont/FontRendererHook;");
				super.visitFieldInsn(Opcodes.GETFIELD, "bre/smoothfont/FontRendererHook", "enableHookGetCharWidth", "Z");
				super.visitJumpInsn(Opcodes.IFEQ, L1);
				super.visitVarInsn(Opcodes.ALOAD, 0);
				super.visitFieldInsn(Opcodes.GETFIELD, "net/minecraft/client/gui/FontRenderer", "fontRendererHook", "Lbre/smoothfont/FontRendererHook;");
				super.visitFieldInsn(Opcodes.GETFIELD, "bre/smoothfont/FontRendererHook", "disableFeatures", "Z");
				super.visitJumpInsn(Opcodes.IFNE, L1);
				super.visitVarInsn(Opcodes.ALOAD, 0);
				super.visitFieldInsn(Opcodes.GETFIELD, "net/minecraft/client/gui/FontRenderer", "fontRendererHook", "Lbre/smoothfont/FontRendererHook;");
				super.visitVarInsn(Opcodes.ILOAD, 1);
				super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "bre/smoothfont/FontRendererHook", "getCharWidthHook", "(C)I", false);
				super.visitInsn(Opcodes.IRETURN);
				super.visitLabel(L1);
				AsmHelper.logDebug("Inserted the EnterHook in getCharWidth method.");
				transformed1 = true;
			}

			@Override
			public void onMethodExit(int opcode) {
				if (transformed1 && (transformed2 || CorePlugin.optifineExist)) {
					CustomAdapterForDeobfEnv.FontRendererMethods.GET_CHAR_WIDTH.setTransformed();
				}

			}

			@Override
			public void visitLdcInsn(Object cst) {
				if (cst instanceof String) {
					String str = (String) cst;
					if (str.startsWith("ÀÁÂ")) {
						++ldcDefaultGlyphCharsCounter;
						if (ldcDefaultGlyphCharsCounter == 1) {
							ldcDefaultGlyphChars = true;
							super.visitVarInsn(Opcodes.ALOAD, 0);
							super.visitFieldInsn(Opcodes.GETFIELD, "net/minecraft/client/gui/FontRenderer", "fontRendererHook", "Lbre/smoothfont/FontRendererHook;");
							return;
						}
					}
				}

				super.visitLdcInsn(cst);
			}

			@Override
			public void visitMethodInsn(int opcode, String owner, String name, String desc) {
				if (ldcDefaultGlyphChars && opcode == 182 && name.equals("indexOf")) {
					ldcDefaultGlyphChars = false;
					super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "bre/smoothfont/FontRendererHook", "getCharWidthGetCharIndexHook", "(C)I");
					AsmHelper.logDebug("Inserted getCharWidthGetCharIndexHook() in getCharWidth().");
					transformed2 = true;
				} else {
					super.visitMethodInsn(opcode, owner, name, desc);
				}
			}
		}

		public static class GetStringWidthAdapter extends AdviceAdapter {
			public GetStringWidthAdapter(MethodVisitor mv, int access, String name, String desc) {
				super(262144, mv, access, name, desc);
			}

			@Override
			public void onMethodEnter() {
				Label L1 = new Label();
				super.visitVarInsn(Opcodes.ALOAD, 0);
				super.visitFieldInsn(Opcodes.GETFIELD, "net/minecraft/client/gui/FontRenderer", "fontRendererHook", "Lbre/smoothfont/FontRendererHook;");
				super.visitFieldInsn(Opcodes.GETFIELD, "bre/smoothfont/FontRendererHook", "enableHookGetStringWidth", "Z");
				super.visitJumpInsn(Opcodes.IFEQ, L1);
				super.visitVarInsn(Opcodes.ALOAD, 0);
				super.visitFieldInsn(Opcodes.GETFIELD, "net/minecraft/client/gui/FontRenderer", "fontRendererHook", "Lbre/smoothfont/FontRendererHook;");
				super.visitFieldInsn(Opcodes.GETFIELD, "bre/smoothfont/FontRendererHook", "disableFeatures", "Z");
				super.visitJumpInsn(Opcodes.IFNE, L1);
				super.visitVarInsn(Opcodes.ALOAD, 0);
				super.visitFieldInsn(Opcodes.GETFIELD, "net/minecraft/client/gui/FontRenderer", "fontRendererHook", "Lbre/smoothfont/FontRendererHook;");
				super.visitVarInsn(Opcodes.ALOAD, 1);
				super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "bre/smoothfont/FontRendererHook", "getStringWidthFloatHook", "(Ljava/lang/String;)I", false);
				super.visitInsn(Opcodes.IRETURN);
				super.visitLabel(L1);
				AsmHelper.logDebug("Inserted the EnterHook in getStringWidth method.");
				CustomAdapterForDeobfEnv.FontRendererMethods.GET_STRING_WIDTH.setTransformed();
			}
		}

		public static class RenderStringAdapter extends AdviceAdapter {
			private boolean transformed1 = false;

			public RenderStringAdapter(MethodVisitor mv, int access, String name, String desc) {
				super(262144, mv, access, name, desc);
			}

			@Override
			public void onMethodEnter() {
				super.visitVarInsn(Opcodes.ALOAD, 0);
				super.visitFieldInsn(Opcodes.GETFIELD, "net/minecraft/client/gui/FontRenderer", "fontRendererHook", "Lbre/smoothfont/FontRendererHook;");
				super.visitVarInsn(Opcodes.ALOAD, 1);
				super.visitVarInsn(Opcodes.ILOAD, 4);
				super.visitVarInsn(Opcodes.ILOAD, 5);
				super.visitVarInsn(Opcodes.ALOAD, 0);
				super.visitFieldInsn(Opcodes.GETFIELD, "net/minecraft/client/gui/FontRenderer", "unicodeFlag", "Z");
				super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "bre/smoothfont/FontRendererHook", "renderStringHook", "(Ljava/lang/String;IZZ)I", false);
				super.visitVarInsn(54, 4);
				AsmHelper.logDebug("Inserted the EnterHook in renderString method.");
				transformed1 = true;
			}

			@Override
			public void onMethodExit(int opcode) {
				super.visitVarInsn(Opcodes.ALOAD, 0);
				super.visitFieldInsn(Opcodes.GETFIELD, "net/minecraft/client/gui/FontRenderer", "fontRendererHook", "Lbre/smoothfont/FontRendererHook;");
				super.visitVarInsn(Opcodes.ALOAD, 1);
				super.visitVarInsn(Opcodes.ALOAD, 0);
				super.visitFieldInsn(Opcodes.GETFIELD, "net/minecraft/client/gui/FontRenderer", "unicodeFlag", "Z");
				super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "bre/smoothfont/FontRendererHook", "renderStringExitHook", "(Ljava/lang/String;Z)V", false);
				AsmHelper.logDebug("Inserted the ExitHook in renderString method.");
				if (transformed1) {
					CustomAdapterForDeobfEnv.FontRendererMethods.RENDER_STRING.setTransformed();
				}

			}
		}

		public static class RenderStringAlignedAdapter extends MethodVisitor {
			private int step = 0;

			public RenderStringAlignedAdapter(MethodVisitor mv) {
				super(262144, mv);
			}

			@Override
			public void visitVarInsn(int opcode, int var) {
				if (step == 0 && opcode == Opcodes.ISTORE && var == 2) {
					++step;
				} else if (step == 1 && opcode == Opcodes.ALOAD && var == 0) {
					super.visitVarInsn(Opcodes.ALOAD, 0);
					super.visitFieldInsn(Opcodes.GETFIELD, "net/minecraft/client/gui/FontRenderer", "fontRendererHook", "Lbre/smoothfont/FontRendererHook;");
					super.visitVarInsn(Opcodes.ALOAD, 1);
					super.visitVarInsn(Opcodes.ILOAD, 2);
					super.visitVarInsn(Opcodes.ILOAD, 3);
					super.visitVarInsn(Opcodes.ILOAD, 5);
					super.visitVarInsn(Opcodes.ILOAD, 6);
					super.visitVarInsn(Opcodes.ALOAD, 0);
					super.visitFieldInsn(Opcodes.GETFIELD, "net/minecraft/client/gui/FontRenderer", "unicodeFlag", "Z");
					super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "bre/smoothfont/FontRendererHook", "renderStringAlignedHook", "(Ljava/lang/String;IIIZZ)I", false);
					super.visitInsn(Opcodes.IRETURN);
					++step;
					AsmHelper.logDebug("Inserted hook in renderStringAligned method.");
					CustomAdapterForDeobfEnv.FontRendererMethods.RENDER_STRING_ALIGNED.setTransformed();
				}

				super.visitVarInsn(opcode, var);
			}
		}

		public static class DoDrawAdapter extends AdviceAdapter {
			private boolean transformed1 = false;
			boolean targetFound = false;
			boolean skip = false;

			public DoDrawAdapter(MethodVisitor mv, int access, String name, String desc) {
				super(262144, mv, access, name, desc);
			}

			@Override
			public void onMethodEnter() {
				super.visitVarInsn(Opcodes.ALOAD, 0);
				super.visitFieldInsn(Opcodes.GETFIELD, "net/minecraft/client/gui/FontRenderer", "fontRendererHook", "Lbre/smoothfont/FontRendererHook;");
				super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "bre/smoothfont/FontRendererHook", "doDrawEnterHook", "()V", false);
				AsmHelper.logDebug("Inserted the EnterHook in doDraw method.");
				transformed1 = true;
			}

			@Override
			public void visitInsn(int opcode) {
				if (opcode == 89) {
					targetFound = true;
				}

				if (targetFound && skip) {
					if (opcode == Opcodes.FADD) {
						targetFound = false;
						skip = false;
						super.visitVarInsn(Opcodes.ALOAD, 0);
						super.visitFieldInsn(Opcodes.GETFIELD, "net/minecraft/client/gui/FontRenderer", "fontRendererHook", "Lbre/smoothfont/FontRendererHook;");
						super.visitVarInsn(Opcodes.FLOAD, 1);
						super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "bre/smoothfont/FontRendererHook", "doDrawHook", "(F)F", false);
						super.visitInsn(98);
						AsmHelper.logDebug("Modified doDraw method.");
						if (transformed1) {
							CustomAdapterForDeobfEnv.FontRendererMethods.DO_DRAW.setTransformed();
						}
					}

				} else {
					super.visitInsn(opcode);
				}
			}

			@Override
			public void visitVarInsn(int opcode, int var) {
				if (targetFound && opcode == 23 && var == 1) {
					skip = true;
				} else {
					super.visitVarInsn(opcode, var);
				}
			}
		}

		public static class RenderStringAtPosAdapter extends AdviceAdapter {
			private boolean transformed1 = false;
			private boolean transformed2 = false;
			private boolean transformed3 = false;
			private boolean transformed4 = false;
			int unicodeFlag = 0;
			boolean flag1 = false;
			int posX = 0;
			boolean ldcDefaultGlyphChars = false;
			int ldcDefaultGlyphCharsCounter = 0;

			public RenderStringAtPosAdapter(MethodVisitor mv, int access, String name, String desc) {
				super(262144, mv, access, name, desc);
			}

			@Override
			public void onMethodEnter() {
				super.visitVarInsn(Opcodes.ALOAD, 0);
				super.visitFieldInsn(Opcodes.GETFIELD, "net/minecraft/client/gui/FontRenderer", "fontRendererHook", "Lbre/smoothfont/FontRendererHook;");
				super.visitVarInsn(Opcodes.ALOAD, 1);
				super.visitVarInsn(Opcodes.ALOAD, 0);
				super.visitFieldInsn(Opcodes.GETFIELD, "net/minecraft/client/gui/FontRenderer", "unicodeFlag", "Z");
				super.visitVarInsn(Opcodes.ILOAD, 2);
				super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "bre/smoothfont/FontRendererHook", "renderStringAtPosEnterHook", "(Ljava/lang/String;ZZ)V", false);
				AsmHelper.logDebug("Inserted the EnterHook in renderStringAtPos method.");
				transformed1 = true;
			}

			@Override
			public void onMethodExit(int opcode) {
				super.visitVarInsn(Opcodes.ALOAD, 0);
				super.visitFieldInsn(Opcodes.GETFIELD, "net/minecraft/client/gui/FontRenderer", "fontRendererHook", "Lbre/smoothfont/FontRendererHook;");
				super.visitVarInsn(Opcodes.ALOAD, 0);
				super.visitFieldInsn(Opcodes.GETFIELD, "net/minecraft/client/gui/FontRenderer", "unicodeFlag", "Z");
				super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "bre/smoothfont/FontRendererHook", "renderStringAtPosExitHook", "(Z)V", false);
				AsmHelper.logDebug("Inserted the ExitHook in renderStringAtPos method.");
				if (transformed1 && transformed2 && transformed3 && transformed4) {
					CustomAdapterForDeobfEnv.FontRendererMethods.RENDER_STRING_AT_POS.setTransformed();
				}

			}

			@Override
			public void visitFieldInsn(int opcode, String owner, String name, String desc) {
				if (opcode == 180 && "unicodeFlag".equals(AsmHelper.mapFieldName(owner, name, desc))) {
					super.visitFieldInsn(Opcodes.GETFIELD, "net/minecraft/client/gui/FontRenderer", "fontRendererHook", "Lbre/smoothfont/FontRendererHook;");
					super.visitFieldInsn(Opcodes.GETFIELD, "bre/smoothfont/FontRendererHook", "thinFontFlag", "Z");
					++unicodeFlag;
					if (unicodeFlag == 2) {
						AsmHelper.logDebug("Replaced unicodeFlag(x2) in renderStringAtPos().");
						transformed3 = true;
					}

				} else {
					super.visitFieldInsn(opcode, owner, name, desc);
					if (opcode == 180 && "field_78302_t".equals(AsmHelper.mapFieldName(owner, name, desc))) {
						flag1 = true;
					}

					if (flag1 && opcode == 181 && "posX".equals(AsmHelper.mapFieldName(owner, name, desc))) {
						++posX;
						if (posX == 1) {
							super.visitVarInsn(Opcodes.ALOAD, 0);
							super.visitFieldInsn(Opcodes.GETFIELD, "net/minecraft/client/gui/FontRenderer", "fontRendererHook", "Lbre/smoothfont/FontRendererHook;");
							super.visitInsn(4);
							super.visitFieldInsn(Opcodes.PUTFIELD, "bre/smoothfont/FontRendererHook", "boldFlag", "Z");
						}

						if (posX == 3) {
							super.visitVarInsn(Opcodes.ALOAD, 0);
							super.visitFieldInsn(Opcodes.GETFIELD, "net/minecraft/client/gui/FontRenderer", "fontRendererHook", "Lbre/smoothfont/FontRendererHook;");
							super.visitInsn(3);
							super.visitFieldInsn(Opcodes.PUTFIELD, "bre/smoothfont/FontRendererHook", "boldFlag", "Z");
							flag1 = false;
							AsmHelper.logDebug("Inserted boldFlag related code in renderStringAtPos().");
							transformed2 = true;
						}
					}

				}
			}

			@Override
			public void visitLdcInsn(Object cst) {
				if (cst instanceof String) {
					String str = (String) cst;
					if (str.startsWith("ÀÁÂ")) {
						++ldcDefaultGlyphCharsCounter;
						if (ldcDefaultGlyphCharsCounter == 1) {
							ldcDefaultGlyphChars = true;
							super.visitVarInsn(Opcodes.ALOAD, 0);
							super.visitFieldInsn(Opcodes.GETFIELD, "net/minecraft/client/gui/FontRenderer", "fontRendererHook", "Lbre/smoothfont/FontRendererHook;");
							return;
						}
					}
				}

				super.visitLdcInsn(cst);
			}

			@Override
			public void visitMethodInsn(int opcode, String owner, String name, String desc) {
				if (ldcDefaultGlyphChars && opcode == 182 && name.equals("indexOf")) {
					ldcDefaultGlyphChars = false;
					super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "bre/smoothfont/FontRendererHook", "renderStringAtPosGetCharIndexHook", "(C)I");
					AsmHelper.logDebug("Inserted renderStringAtPosGetCharIndexHook() in renderStringAtPos().");
					transformed4 = true;
				} else {
					super.visitMethodInsn(opcode, owner, name, desc);
				}
			}
		}

		public static class DrawStringAdapter extends MethodVisitor {
			private int step = 0;

			public DrawStringAdapter(MethodVisitor mv) {
				super(262144, mv);
			}

			@Override
			public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
				super.visitMethodInsn(opcode, owner, name, desc, itf);
				if (step == 0 && opcode == Opcodes.INVOKEVIRTUAL) {
					super.visitVarInsn(Opcodes.ALOAD, 0);
					super.visitFieldInsn(Opcodes.GETFIELD, "net/minecraft/client/gui/FontRenderer", "fontRendererHook", "Lbre/smoothfont/FontRendererHook;");
					super.visitVarInsn(Opcodes.ALOAD, 1);
					super.visitVarInsn(Opcodes.FLOAD, 2);
					super.visitVarInsn(Opcodes.FLOAD, 3);
					super.visitVarInsn(Opcodes.ILOAD, 4);
					super.visitVarInsn(Opcodes.ILOAD, 5);
					super.visitVarInsn(Opcodes.ALOAD, 0);
					super.visitFieldInsn(Opcodes.GETFIELD, "net/minecraft/client/gui/FontRenderer", "unicodeFlag", "Z");
					super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "bre/smoothfont/FontRendererHook", "drawStringHook", "(Ljava/lang/String;FFIZZ)I", false);
					super.visitInsn(Opcodes.IRETURN);
					++step;
					AsmHelper.logDebug("Inserted hook in drawString method.");
					CustomAdapterForDeobfEnv.FontRendererMethods.DRAW_STRING.setTransformed();
				}

			}
		}

		public static class RenderUnicodeCharAdapter extends AdviceAdapter {
			public RenderUnicodeCharAdapter(MethodVisitor mv, int access, String name, String desc) {
				super(262144, mv, access, name, desc);
			}

			@Override
			public void onMethodEnter() {
				Label L1 = new Label();
				super.visitVarInsn(Opcodes.ALOAD, 0);
				super.visitFieldInsn(Opcodes.GETFIELD, "net/minecraft/client/gui/FontRenderer", "fontRendererHook", "Lbre/smoothfont/FontRendererHook;");
				super.visitFieldInsn(Opcodes.GETFIELD, "bre/smoothfont/FontRendererHook", "disableFeatures", "Z");
				super.visitJumpInsn(Opcodes.IFNE, L1);
				super.visitVarInsn(Opcodes.ALOAD, 0);
				super.visitFieldInsn(Opcodes.GETFIELD, "net/minecraft/client/gui/FontRenderer", "fontRendererHook", "Lbre/smoothfont/FontRendererHook;");
				super.visitVarInsn(Opcodes.ILOAD, 1);
				super.visitVarInsn(Opcodes.ILOAD, 2);
				super.visitVarInsn(Opcodes.ALOAD, 0);
				super.visitFieldInsn(Opcodes.GETFIELD, "net/minecraft/client/gui/FontRenderer", "glyphWidth", "[B");
				super.visitVarInsn(Opcodes.ALOAD, 0);
				super.visitFieldInsn(Opcodes.GETFIELD, "net/minecraft/client/gui/FontRenderer", "posX", "F");
				super.visitVarInsn(Opcodes.ALOAD, 0);
				super.visitFieldInsn(Opcodes.GETFIELD, "net/minecraft/client/gui/FontRenderer", "posY", "F");
				super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "bre/smoothfont/FontRendererHook", "renderUnicodeCharHook", "(CZ[BFF)F", false);
				super.visitInsn(Opcodes.FRETURN);
				super.visitLabel(L1);
				AsmHelper.logDebug("Inserted the EnterHook in renderUnicodeChar() method.");
				CustomAdapterForDeobfEnv.FontRendererMethods.RENDER_UNICODE_CHAR.setTransformed();
			}
		}

		public static class RenderDefaultCharAdapter extends AdviceAdapter {
			public RenderDefaultCharAdapter(MethodVisitor mv, int access, String name, String desc) {
				super(262144, mv, access, name, desc);
			}

			@Override
			public void onMethodEnter() {
				Label L1 = new Label();
				super.visitVarInsn(Opcodes.ALOAD, 0);
				super.visitFieldInsn(Opcodes.GETFIELD, "net/minecraft/client/gui/FontRenderer", "fontRendererHook", "Lbre/smoothfont/FontRendererHook;");
				super.visitFieldInsn(Opcodes.GETFIELD, "bre/smoothfont/FontRendererHook", "disableFeatures", "Z");
				super.visitJumpInsn(Opcodes.IFNE, L1);
				super.visitVarInsn(Opcodes.ALOAD, 0);
				super.visitFieldInsn(Opcodes.GETFIELD, "net/minecraft/client/gui/FontRenderer", "fontRendererHook", "Lbre/smoothfont/FontRendererHook;");
				super.visitVarInsn(Opcodes.ILOAD, 1);
				super.visitVarInsn(Opcodes.ILOAD, 2);
				super.visitVarInsn(Opcodes.ALOAD, 0);
				super.visitFieldInsn(Opcodes.GETFIELD, "net/minecraft/client/gui/FontRenderer", "posX", "F");
				super.visitVarInsn(Opcodes.ALOAD, 0);
				super.visitFieldInsn(Opcodes.GETFIELD, "net/minecraft/client/gui/FontRenderer", "posY", "F");
				super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "bre/smoothfont/FontRendererHook", "renderDefaultCharHook", "(IZFF)F", false);
				super.visitInsn(Opcodes.FRETURN);
				super.visitLabel(L1);
				AsmHelper.logDebug("Inserted the EnterHook in renderDefaultChar method.");
				CustomAdapterForDeobfEnv.FontRendererMethods.RENDER_DEFAULT_CHAR.setTransformed();
			}
		}

		public static class RenderCharAdapter extends AdviceAdapter {
			private boolean transformed1 = false;
			private boolean transformed2 = false;
			boolean ldcDefaultGlyphChars = false;
			int ldcDefaultGlyphCharsCounter = 0;

			public RenderCharAdapter(MethodVisitor mv, int access, String name, String desc) {
				super(262144, mv, access, name, desc);
			}

			@Override
			public void onMethodEnter() {
				Label L1 = new Label();
				super.visitVarInsn(Opcodes.ALOAD, 0);
				super.visitFieldInsn(Opcodes.GETFIELD, "net/minecraft/client/gui/FontRenderer", "fontRendererHook", "Lbre/smoothfont/FontRendererHook;");
				super.visitFieldInsn(Opcodes.GETFIELD, "bre/smoothfont/FontRendererHook", "enableHookRenderChar", "Z");
				super.visitJumpInsn(Opcodes.IFEQ, L1);
				super.visitVarInsn(Opcodes.ALOAD, 0);
				super.visitFieldInsn(Opcodes.GETFIELD, "net/minecraft/client/gui/FontRenderer", "fontRendererHook", "Lbre/smoothfont/FontRendererHook;");
				super.visitFieldInsn(Opcodes.GETFIELD, "bre/smoothfont/FontRendererHook", "disableFeatures", "Z");
				super.visitJumpInsn(Opcodes.IFNE, L1);
				super.visitVarInsn(Opcodes.ALOAD, 0);
				super.visitFieldInsn(Opcodes.GETFIELD, "net/minecraft/client/gui/FontRenderer", "fontRendererHook", "Lbre/smoothfont/FontRendererHook;");
				super.visitVarInsn(Opcodes.ILOAD, 1);
				super.visitVarInsn(Opcodes.ILOAD, 2);
				super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "bre/smoothfont/FontRendererHook", "renderCharHook", "(CZ)F", false);
				super.visitVarInsn(56, 3);
				super.visitVarInsn(Opcodes.FLOAD, 3);
				super.visitInsn(Opcodes.FCONST_0);
				super.visitInsn(Opcodes.FCMPL);
				super.visitJumpInsn(155, L1);
				super.visitVarInsn(Opcodes.FLOAD, 3);
				super.visitInsn(Opcodes.FRETURN);
				super.visitLabel(L1);
				AsmHelper.logDebug("Inserted the EnterHook in renderChar() method.");
				transformed1 = true;
			}

			@Override
			public void onMethodExit(int opcode) {
				if (transformed1 && transformed2) {
					CustomAdapterForDeobfEnv.FontRendererMethods.RENDER_CHAR.setTransformed();
				}

			}

			@Override
			public void visitLdcInsn(Object cst) {
				if (cst instanceof String) {
					String str = (String) cst;
					if (str.startsWith("ÀÁÂ")) {
						++ldcDefaultGlyphCharsCounter;
						if (ldcDefaultGlyphCharsCounter == 1) {
							ldcDefaultGlyphChars = true;
							super.visitVarInsn(Opcodes.ALOAD, 0);
							super.visitFieldInsn(Opcodes.GETFIELD, "net/minecraft/client/gui/FontRenderer", "fontRendererHook", "Lbre/smoothfont/FontRendererHook;");
							return;
						}
					}
				}

				super.visitLdcInsn(cst);
			}

			@Override
			public void visitMethodInsn(int opcode, String owner, String name, String desc) {
				if (ldcDefaultGlyphChars && opcode == 182 && name.equals("indexOf")) {
					ldcDefaultGlyphChars = false;
					super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "bre/smoothfont/FontRendererHook", "renderCharGetCharIndexHook", "(C)I");
					AsmHelper.logDebug("Inserted renderCharGetCharIndexHook() in renderChar().");
					transformed2 = true;
				} else {
					super.visitMethodInsn(opcode, owner, name, desc);
				}
			}
		}

		public static class ReadGlyphSizesAdapter extends AdviceAdapter {
			public ReadGlyphSizesAdapter(MethodVisitor mv, int access, String name, String desc) {
				super(262144, mv, access, name, desc);
			}

			@Override
			public void onMethodExit(int opcode) {
				super.visitVarInsn(Opcodes.ALOAD, 0);
				super.visitFieldInsn(Opcodes.GETFIELD, "net/minecraft/client/gui/FontRenderer", "fontRendererHook", "Lbre/smoothfont/FontRendererHook;");
				super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "bre/smoothfont/FontRendererHook", "readGlyphSizesExitHook", "()V", false);
				AsmHelper.logDebug("Inserted the ExitHook in readGlyphSizes method.");
				CustomAdapterForDeobfEnv.FontRendererMethods.READ_GLYPH_SIZES.setTransformed();
			}
		}

		public static class ReadFontTextureAdapter extends AdviceAdapter {
			public ReadFontTextureAdapter(MethodVisitor mv, int access, String name, String desc) {
				super(262144, mv, access, name, desc);
			}

			@Override
			public void onMethodExit(int opcode) {
				super.visitVarInsn(Opcodes.ALOAD, 0);
				super.visitFieldInsn(Opcodes.GETFIELD, "net/minecraft/client/gui/FontRenderer", "fontRendererHook", "Lbre/smoothfont/FontRendererHook;");
				super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "bre/smoothfont/FontRendererHook", "readFontTextureExitHook", "()V", false);
				AsmHelper.logDebug("Inserted the ExitHook in readFontTexture method.");
				CustomAdapterForDeobfEnv.FontRendererMethods.READ_FONT_TEXTURE.setTransformed();
			}
		}

		public static class ConstructorAdapter extends AdviceAdapter {
			private boolean transformed1 = false;

			public ConstructorAdapter(MethodVisitor mv, int access, String name, String desc) {
				super(262144, mv, access, name, desc);
			}

			@Override
			public void onMethodEnter() {
				super.visitVarInsn(Opcodes.ALOAD, 0);
				super.visitTypeInsn(187, "bre/smoothfont/FontRendererHook");
				super.visitInsn(89);
				super.visitVarInsn(Opcodes.ALOAD, 0);
				super.visitMethodInsn(183, "bre/smoothfont/FontRendererHook", "<init>", "(Lnet/minecraft/client/gui/FontRenderer;)V", false);
				super.visitFieldInsn(Opcodes.PUTFIELD, "net/minecraft/client/gui/FontRenderer", "fontRendererHook", "Lbre/smoothfont/FontRendererHook;");
				AsmHelper.logDebug("Inserted the EnterHook in constructor.");
				transformed1 = true;
			}

			@Override
			public void onMethodExit(int opcode) {
				super.visitVarInsn(Opcodes.ALOAD, 0);
				super.visitFieldInsn(Opcodes.GETFIELD, "net/minecraft/client/gui/FontRenderer", "fontRendererHook", "Lbre/smoothfont/FontRendererHook;");
				super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "bre/smoothfont/FontRendererHook", "fontRendererExitHook", "()V", false);
				AsmHelper.logDebug("Inserted the ExitHook in constructor.");
				if (transformed1) {
					CustomAdapterForDeobfEnv.FontRendererMethods.INIT.setTransformed();
				}

			}
		}
	}

	public static class ClassAdapter extends ClassVisitor {
		public ClassAdapter(ClassVisitor cv) {
			super(262144, cv);
			AsmHelper.logDebug("Reset transforming completion flags");
			CustomAdapterForDeobfEnv.FontRendererMethods.init();
			CustomAdapterForDeobfEnv.ScaledResolutionMethods.init();
			CustomAdapterForDeobfEnv.TextureManagerMethods.init();
			CustomAdapterForDeobfEnv.GuiIngameForgeMethods.init();
			CustomAdapterForDeobfEnv.GlStateManagerMethods.init();
		}

		public ClassAdapter(ClassVisitor cv, String className, String transformedName) {
			this(cv);
			CustomAdapterForDeobfEnv.className = className;
			CustomAdapterForDeobfEnv.transformedName = transformedName;
		}

		@Override
		public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
			if (CorePlugin.optifineExist && CustomAdapterForDeobfEnv.transformedName.equals(TargetClass.FONT_RENDERER.getClassName()) && "charWidth".equals(AsmHelper.mapFieldName(TargetClass.FONT_RENDERER.getClassName(), name, desc))) {
				AsmHelper.logInfo("OptiFine (int)charWidth[] was found.");
				CorePlugin.optifineNoCharWidthInt = false;
			}

			return super.visitField(access, name, desc, signature, value);
		}

		@Override
		public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
			String targetClassName;
			if (CustomAdapterForDeobfEnv.transformedName.equals(TargetClass.FONT_RENDERER.getClassName())) {
				targetClassName = TargetClass.FONT_RENDERER.getClassName();
				if ("<init>".equals(AsmHelper.mapMethodName(targetClassName, name, desc))) {
					return new CustomAdapterForDeobfEnv.FontRenderer.ConstructorAdapter(super.visitMethod(access, name, desc, signature, exceptions), access, name, desc);
				}

				if ("readFontTexture".equals(AsmHelper.mapMethodName(targetClassName, name, desc))) {
					return new CustomAdapterForDeobfEnv.FontRenderer.ReadFontTextureAdapter(super.visitMethod(access, name, desc, signature, exceptions), access, name, desc);
				}

				if ("readGlyphSizes".equals(AsmHelper.mapMethodName(targetClassName, name, desc))) {
					return new CustomAdapterForDeobfEnv.FontRenderer.ReadGlyphSizesAdapter(super.visitMethod(access, name, desc, signature, exceptions), access, name, desc);
				}

				if ("renderChar".equals(AsmHelper.mapMethodName(targetClassName, name, desc))) {
					return new CustomAdapterForDeobfEnv.FontRenderer.RenderCharAdapter(super.visitMethod(access, name, desc, signature, exceptions), access, name, desc);
				}

				if ("renderDefaultChar".equals(AsmHelper.mapMethodName(targetClassName, name, desc))) {
					return new CustomAdapterForDeobfEnv.FontRenderer.RenderDefaultCharAdapter(super.visitMethod(access, name, desc, signature, exceptions), access, name, desc);
				}

				if ("renderUnicodeChar".equals(AsmHelper.mapMethodName(targetClassName, name, desc))) {
					return new CustomAdapterForDeobfEnv.FontRenderer.RenderUnicodeCharAdapter(super.visitMethod(access, name, desc, signature, exceptions), access, name, desc);
				}

				if ("drawString".equals(AsmHelper.mapMethodName(targetClassName, name, desc)) && desc.equals(AsmUtil.toMethodDesc(AsmTypes.INT, AsmTypes.STRING, AsmTypes.FLOAT, AsmTypes.FLOAT, AsmTypes.INT, AsmTypes.BOOL))) {
					return new CustomAdapterForDeobfEnv.FontRenderer.DrawStringAdapter(super.visitMethod(access, name, desc, signature, exceptions));
				}

				if ("renderStringAtPos".equals(AsmHelper.mapMethodName(targetClassName, name, desc))) {
					return new CustomAdapterForDeobfEnv.FontRenderer.RenderStringAtPosAdapter(super.visitMethod(access, name, desc, signature, exceptions), access, name, desc);
				}

				if ("doDraw".equals(AsmHelper.mapMethodName(targetClassName, name, desc))) {
					return new CustomAdapterForDeobfEnv.FontRenderer.DoDrawAdapter(super.visitMethod(access, name, desc, signature, exceptions), access, name, desc);
				}

				if ("renderStringAligned".equals(AsmHelper.mapMethodName(targetClassName, name, desc))) {
					return new CustomAdapterForDeobfEnv.FontRenderer.RenderStringAlignedAdapter(super.visitMethod(access, name, desc, signature, exceptions));
				}

				if ("renderString".equals(AsmHelper.mapMethodName(targetClassName, name, desc))) {
					return new CustomAdapterForDeobfEnv.FontRenderer.RenderStringAdapter(super.visitMethod(access, name, desc, signature, exceptions), access, name, desc);
				}

				if ("getStringWidth".equals(AsmHelper.mapMethodName(targetClassName, name, desc))) {
					return new CustomAdapterForDeobfEnv.FontRenderer.GetStringWidthAdapter(super.visitMethod(access, name, desc, signature, exceptions), access, name, desc);
				}

				if ("getCharWidth".equals(AsmHelper.mapMethodName(targetClassName, name, desc))) {
					return new CustomAdapterForDeobfEnv.FontRenderer.GetCharWidthAdapter(super.visitMethod(access, name, desc, signature, exceptions), access, name, desc);
				}

				if ("trimStringToWidth".equals(AsmHelper.mapMethodName(targetClassName, name, desc)) && desc.equals(AsmUtil.toMethodDesc(AsmTypes.STRING, AsmTypes.STRING, AsmTypes.INT, AsmTypes.BOOL))) {
					return new CustomAdapterForDeobfEnv.FontRenderer.TrimStringToWidthAdapter(super.visitMethod(access, name, desc, signature, exceptions), access, name, desc);
				}

				if ("setUnicodeFlag".equals(AsmHelper.mapMethodName(targetClassName, name, desc))) {
					return new CustomAdapterForDeobfEnv.FontRenderer.SetUnicodeFlagAdapter(super.visitMethod(access, name, desc, signature, exceptions), access, name, desc);
				}

				if ("sizeStringToWidth".equals(AsmHelper.mapMethodName(targetClassName, name, desc))) {
					return new CustomAdapterForDeobfEnv.FontRenderer.SizeStringToWidthAdapter(super.visitMethod(access, name, desc, signature, exceptions), access, name, desc);
				}

				if ("bindTexture".equals(AsmHelper.mapMethodName(targetClassName, name, desc))) {
					return new CustomAdapterForDeobfEnv.FontRenderer.BindTextureAdapter(super.visitMethod(access, name, desc, signature, exceptions), access, name, desc);
				}

				if ("getCharWidthFloat".equals(AsmHelper.mapMethodName(targetClassName, name, desc))) {
					return new CustomAdapterForDeobfEnv.FontRenderer.GetCharWidthFloatAdapter(super.visitMethod(access, name, desc, signature, exceptions), access, name, desc);
				}
			}

			if (CommonConfig.globalConfig.tweakScaledResolution && CustomAdapterForDeobfEnv.transformedName.equals(TargetClass.SCALED_RESOLUTION.getClassName())) {
				targetClassName = TargetClass.SCALED_RESOLUTION.getClassName();
				if ("<init>".equals(AsmHelper.mapMethodName(targetClassName, name, desc))) {
					return new CustomAdapterForDeobfEnv.ScaledResolution.ConstructorAdapter(super.visitMethod(access, name, desc, signature, exceptions));
				}
			}

			if (CommonConfig.globalConfig.tweakLoadTexture && CustomAdapterForDeobfEnv.transformedName.equals(TargetClass.TEXTURE_MANAGER.getClassName())) {
				targetClassName = TargetClass.TEXTURE_MANAGER.getClassName();
				if ("loadTexture".equals(AsmHelper.mapMethodName(targetClassName, name, desc))) {
					return new CustomAdapterForDeobfEnv.TextureManager.LoadTextureAdapter(super.visitMethod(access, name, desc, signature, exceptions), access, name, desc);
				}
			}

			if (CommonConfig.globalConfig.tweakRenderExperience && CustomAdapterForDeobfEnv.transformedName.equals(TargetClass.GUI_INGAME_FORGE.getClassName())) {
				targetClassName = TargetClass.GUI_INGAME_FORGE.getClassName();
				if ("renderExperience".equals(AsmHelper.mapMethodName(targetClassName, name, desc))) {
					return new CustomAdapterForDeobfEnv.GuiIngameForge.RenderExperienceAdapter(super.visitMethod(access, name, desc, signature, exceptions));
				}
			}

			return super.visitMethod(access, name, desc, signature, exceptions);
		}

		@Override
		public void visitEnd() {
			if (CustomAdapterForDeobfEnv.transformedName.equals("net.minecraft.client.gui.FontRenderer")) {
				super.visitField(1, "fontRendererHook", "Lbre/smoothfont/FontRendererHook;", (String) null, (Object) null).visitEnd();
			}

			if (CustomAdapterForDeobfEnv.transformedName.equals(TargetClass.GL_STATE_MANAGER.getClassName())) {
				MethodVisitor mv1 = super.visitMethod(9, "getBlendState", "()Z", (String) null, (String[]) null);
				mv1.visitFieldInsn(178, "net/minecraft/client/renderer/GlStateManager", "blendState", "Lnet/minecraft/client/renderer/GlStateManager$BlendState;");
				mv1.visitFieldInsn(Opcodes.GETFIELD, "net/minecraft/client/renderer/GlStateManager$BlendState", "blend", "Lnet/minecraft/client/renderer/GlStateManager$BooleanState;");
				mv1.visitFieldInsn(Opcodes.GETFIELD, "net/minecraft/client/renderer/GlStateManager$BooleanState", "currentState", "Z");
				mv1.visitInsn(Opcodes.IRETURN);
				mv1.visitEnd();
				AsmHelper.logDebug("Added getBlendState() to GlStateManager class.");
				CustomAdapterForDeobfEnv.GlStateManagerMethods.GET_BLEND_STATE.setTransformed();
				MethodVisitor mv2 = super.visitMethod(9, "getBlendSrcFactor", "()I", (String) null, (String[]) null);
				mv2.visitFieldInsn(178, "net/minecraft/client/renderer/GlStateManager", "blendState", "Lnet/minecraft/client/renderer/GlStateManager$BlendState;");
				mv2.visitFieldInsn(Opcodes.GETFIELD, "net/minecraft/client/renderer/GlStateManager$BlendState", "srcFactor", "I");
				mv2.visitInsn(Opcodes.IRETURN);
				mv2.visitEnd();
				AsmHelper.logDebug("Added getBlendSrcFactor() to GlStateManager class.");
				CustomAdapterForDeobfEnv.GlStateManagerMethods.GET_BLEND_SRC_FACTOR.setTransformed();
				MethodVisitor mv3 = super.visitMethod(9, "getBlendDstFactor", "()I", (String) null, (String[]) null);
				mv3.visitFieldInsn(178, "net/minecraft/client/renderer/GlStateManager", "blendState", "Lnet/minecraft/client/renderer/GlStateManager$BlendState;");
				mv3.visitFieldInsn(Opcodes.GETFIELD, "net/minecraft/client/renderer/GlStateManager$BlendState", "dstFactor", "I");
				mv3.visitInsn(Opcodes.IRETURN);
				mv3.visitEnd();
				AsmHelper.logDebug("Added getBlendDstFactor() to GlStateManager class.");
				CustomAdapterForDeobfEnv.GlStateManagerMethods.GET_BLEND_DST_FACTOR.setTransformed();
			}

			super.visitEnd();
			int var4;
			boolean transformedAll;
			int var10;
			if (CustomAdapterForDeobfEnv.transformedName.equals(TargetClass.FONT_RENDERER.getClassName())) {
				transformedAll = true;
				CustomAdapterForDeobfEnv.FontRendererMethods[] var7 = CustomAdapterForDeobfEnv.FontRendererMethods.values();
				var10 = var7.length;

				for (var4 = 0; var4 < var10; ++var4) {
					CustomAdapterForDeobfEnv.FontRendererMethods methods = var7[var4];
					if (CorePlugin.optifineExist || !methods.name().equals("GET_CHAR_WIDTH_FLOAT")) {
						AsmHelper.logInfo(methods.name() + ":" + methods.getTransformed());
						transformedAll &= methods.getTransformed();
					}
				}

				if (transformedAll) {
					AsmHelper.logInfo("** FontRenderer was transformed successfully.");
				} else {
					Transformer.transformingErrorId |= 1;
				}

				assert transformedAll : "FontRenderer was not completely transformed.";
			}

			if (CustomAdapterForDeobfEnv.transformedName.equals(TargetClass.SCALED_RESOLUTION.getClassName())) {
				if (CommonConfig.globalConfig.tweakScaledResolution) {
					transformedAll = true;
					CustomAdapterForDeobfEnv.ScaledResolutionMethods[] var8 = CustomAdapterForDeobfEnv.ScaledResolutionMethods.values();
					var10 = var8.length;

					for (var4 = 0; var4 < var10; ++var4) {
						CustomAdapterForDeobfEnv.ScaledResolutionMethods methods = var8[var4];
						AsmHelper.logInfo(methods.name() + ":" + methods.getTransformed());
						transformedAll &= methods.getTransformed();
					}

					if (transformedAll) {
						AsmHelper.logInfo("** ScaledResolution was transformed successfully.");
					} else {
						Transformer.transformingErrorId |= 2;
					}

					assert transformedAll : "ScaledResolution was not completely transformed.";
				} else {
					AsmHelper.logInfo("** ScaledResolution transforming is disabled in config.");
				}
			}

			if (CustomAdapterForDeobfEnv.transformedName.equals(TargetClass.TEXTURE_MANAGER.getClassName())) {
				if (CommonConfig.globalConfig.tweakLoadTexture) {
					transformedAll = true;
					CustomAdapterForDeobfEnv.TextureManagerMethods[] var9 = CustomAdapterForDeobfEnv.TextureManagerMethods.values();
					var10 = var9.length;

					for (var4 = 0; var4 < var10; ++var4) {
						CustomAdapterForDeobfEnv.TextureManagerMethods methods = var9[var4];
						AsmHelper.logInfo(methods.name() + ":" + methods.getTransformed());
						transformedAll &= methods.getTransformed();
					}

					if (transformedAll) {
						AsmHelper.logInfo("** TextureManager was transformed successfully.");
					} else {
						Transformer.transformingErrorId |= 4;
					}

					assert transformedAll : "TextureManager was not completely transformed.";
				} else {
					AsmHelper.logInfo("** TextureManager transforming is disabled in config.");
				}
			}

			if (CustomAdapterForDeobfEnv.transformedName.equals(TargetClass.GUI_INGAME_FORGE.getClassName())) {
				if (CommonConfig.globalConfig.tweakRenderExperience) {
					transformedAll = true;
					CustomAdapterForDeobfEnv.GuiIngameForgeMethods[] var11 = CustomAdapterForDeobfEnv.GuiIngameForgeMethods.values();
					var10 = var11.length;

					for (var4 = 0; var4 < var10; ++var4) {
						CustomAdapterForDeobfEnv.GuiIngameForgeMethods methods = var11[var4];
						AsmHelper.logInfo(methods.name() + ":" + methods.getTransformed());
						transformedAll &= methods.getTransformed();
					}

					if (transformedAll) {
						AsmHelper.logInfo("** GuiIngameForge was transformed successfully.");
					} else {
						Transformer.transformingErrorId |= 8;
					}

					assert transformedAll : "GuiIngameForge was not completely transformed.";
				} else {
					AsmHelper.logInfo("** GuiIngameForge transforming is disabled in config.");
				}
			}

			if (CustomAdapterForDeobfEnv.transformedName.equals(TargetClass.GL_STATE_MANAGER.getClassName())) {
				transformedAll = true;
				CustomAdapterForDeobfEnv.GlStateManagerMethods[] var12 = CustomAdapterForDeobfEnv.GlStateManagerMethods.values();
				var10 = var12.length;

				for (var4 = 0; var4 < var10; ++var4) {
					CustomAdapterForDeobfEnv.GlStateManagerMethods methods = var12[var4];
					AsmHelper.logInfo(methods.name() + ":" + methods.getTransformed());
					transformedAll &= methods.getTransformed();
				}

				if (transformedAll) {
					AsmHelper.logInfo("** GlStateManager was transformed successfully.");
				} else {
					Transformer.transformingErrorId |= 16;
				}

				assert transformedAll : "GlStateManager was not completely transformed.";
			}

		}
	}

	private enum GlStateManagerMethods {
		GET_BLEND_STATE,
		GET_BLEND_SRC_FACTOR,
		GET_BLEND_DST_FACTOR;

		private boolean transformed = false;

		GlStateManagerMethods() {
		}

		public static void init() {
			CustomAdapterForDeobfEnv.GlStateManagerMethods[] var0 = values();
			int var1 = var0.length;

			for (int var2 = 0; var2 < var1; ++var2) {
				CustomAdapterForDeobfEnv.GlStateManagerMethods methods = var0[var2];
				methods.transformed = false;
			}

		}

		public void setTransformed() {
			transformed = true;
		}

		public boolean getTransformed() {
			return transformed;
		}
	}

	private enum GuiIngameForgeMethods {
		RENDER_EXPERIENCE;

		private boolean transformed = false;

		GuiIngameForgeMethods() {
		}

		public static void init() {
			CustomAdapterForDeobfEnv.GuiIngameForgeMethods[] var0 = values();
			int var1 = var0.length;

			for (int var2 = 0; var2 < var1; ++var2) {
				CustomAdapterForDeobfEnv.GuiIngameForgeMethods methods = var0[var2];
				methods.transformed = false;
			}

		}

		public void setTransformed() {
			transformed = true;
		}

		public boolean getTransformed() {
			return transformed;
		}
	}

	private enum TextureManagerMethods {
		LOAD_TEXTURE;

		private boolean transformed = false;

		TextureManagerMethods() {
		}

		public static void init() {
			CustomAdapterForDeobfEnv.TextureManagerMethods[] var0 = values();
			int var1 = var0.length;

			for (int var2 = 0; var2 < var1; ++var2) {
				CustomAdapterForDeobfEnv.TextureManagerMethods methods = var0[var2];
				methods.transformed = false;
			}

		}

		public void setTransformed() {
			transformed = true;
		}

		public boolean getTransformed() {
			return transformed;
		}
	}

	private enum ScaledResolutionMethods {
		INIT;

		private boolean transformed = false;

		ScaledResolutionMethods() {
		}

		public static void init() {
			CustomAdapterForDeobfEnv.ScaledResolutionMethods[] var0 = values();
			int var1 = var0.length;

			for (int var2 = 0; var2 < var1; ++var2) {
				CustomAdapterForDeobfEnv.ScaledResolutionMethods methods = var0[var2];
				methods.transformed = false;
			}

		}

		public void setTransformed() {
			transformed = true;
		}

		public boolean getTransformed() {
			return transformed;
		}
	}

	private enum FontRendererMethods {
		INIT,
		READ_FONT_TEXTURE,
		READ_GLYPH_SIZES,
		RENDER_CHAR,
		RENDER_DEFAULT_CHAR,
		RENDER_UNICODE_CHAR,
		DRAW_STRING,
		RENDER_STRING_AT_POS,
		DO_DRAW,
		RENDER_STRING_ALIGNED,
		RENDER_STRING,
		GET_STRING_WIDTH,
		GET_CHAR_WIDTH,
		TRIM_STRING_TO_WIDTH,
		SET_UNICODE_FLAG,
		SIZE_STRING_TO_WIDTH,
		BIND_TEXTURE,
		GET_CHAR_WIDTH_FLOAT;

		private boolean transformed = false;

		FontRendererMethods() {
		}

		public static void init() {
			CustomAdapterForDeobfEnv.FontRendererMethods[] var0 = values();
			int var1 = var0.length;

			for (int var2 = 0; var2 < var1; ++var2) {
				CustomAdapterForDeobfEnv.FontRendererMethods methods = var0[var2];
				methods.transformed = false;
			}

		}

		public void setTransformed() {
			transformed = true;
		}

		public boolean getTransformed() {
			return transformed;
		}
	}

	protected enum TargetClass {
		FONT_RENDERER("net.minecraft.client.gui.FontRenderer"),
		SCALED_RESOLUTION("net.minecraft.client.gui.ScaledResolution"),
		TEXTURE_MANAGER("net.minecraft.client.renderer.texture.TextureManager"),
		GUI_INGAME_FORGE("net.minecraftforge.client.GuiIngameForge"),
		GL_STATE_MANAGER("net.minecraft.client.renderer.GlStateManager");

		private String className;

		TargetClass(String name) {
			className = name;
		}

		public String getClassName() {
			return className;
		}
	}
}
