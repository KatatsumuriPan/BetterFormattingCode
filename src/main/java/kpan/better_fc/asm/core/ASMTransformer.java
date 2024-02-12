package kpan.better_fc.asm.core;

import kpan.better_fc.asm.compat.CompatSmoothFont;
import kpan.better_fc.asm.compat.CustomAdapterForDeobfEnv;
import kpan.better_fc.asm.core.adapters.MixinAccessorAdapter;
import kpan.better_fc.asm.tf.TF_CPacketChatMessage;
import kpan.better_fc.asm.tf.TF_ChatAllowedCharacters;
import kpan.better_fc.asm.tf.TF_FontRenderer;
import kpan.better_fc.asm.tf.TF_FontRendererHook;
import kpan.better_fc.asm.tf.TF_GlStateManager;
import kpan.better_fc.asm.tf.TF_GuiChat;
import kpan.better_fc.asm.tf.TF_GuiCommandBlock;
import kpan.better_fc.asm.tf.TF_GuiCreateWorld;
import kpan.better_fc.asm.tf.TF_GuiEditCommandBlockMinecart;
import kpan.better_fc.asm.tf.TF_GuiEditSign;
import kpan.better_fc.asm.tf.TF_GuiNewChat;
import kpan.better_fc.asm.tf.TF_GuiRepair;
import kpan.better_fc.asm.tf.TF_GuiScreen;
import kpan.better_fc.asm.tf.TF_GuiScreenAddServer;
import kpan.better_fc.asm.tf.TF_GuiScreenBook;
import kpan.better_fc.asm.tf.TF_GuiUtilRenderComponents;
import kpan.better_fc.asm.tf.TF_GuiWorldEdit;
import kpan.better_fc.asm.tf.TF_ItemWrittenBook;
import kpan.better_fc.asm.tf.TF_Loader;
import kpan.better_fc.asm.tf.TF_NetHandlerPlayServer;
import kpan.better_fc.asm.tf.TF_Render;
import kpan.better_fc.asm.tf.TF_TeleportToTeam$TeamSelectionObject;
import kpan.better_fc.asm.tf.TF_TextFormatting;
import kpan.better_fc.asm.tf.TF_TileEntitySignRenderer;
import kpan.better_fc.asm.tf.gtceu.TF_TextFieldWidget2;
import kpan.better_fc.util.MyReflectionHelper;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.common.asm.ASMTransformerWrapper.TransformerWrapper;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.util.List;

public class ASMTransformer implements IClassTransformer {

	public ASMTransformer() {
		if (AsmUtil.isDeobfEnvironment()) {
			ClassLoader classLoader = ASMTransformer.class.getClassLoader();
			List<IClassTransformer> transformers = MyReflectionHelper.getPrivateField(classLoader, "transformers");
			transformers.removeIf(transformer -> {
				if (transformer instanceof TransformerWrapper) {
					IClassTransformer parent = MyReflectionHelper.getPrivateField(TransformerWrapper.class, transformer, "parent");
					return parent.getClass().getName().equals("bre.smoothfont.asm.Transformer");
				}
				return false;
			});
		}
	}

	/**
	 * クラスが最初に読み込まれた時に呼ばれる。
	 *
	 * @param name            クラスの難読化名(区切りは'.')
	 * @param transformedName クラスの易読化名
	 * @param bytes           オリジナルのクラス
	 */
	@Override
	public byte[] transform(String name, String transformedName, byte[] bytes) {
		try {
			MyAsmNameRemapper.init();
			if (AsmUtil.isDeobfEnvironment() && CompatSmoothFont.isLoaded()) {
				if (transformedName.startsWith("kpan.better_fc.asm.compat.CustomAdapterForDeobfEnv"))
					return bytes;
				bytes = CustomAdapterForDeobfEnv.transform(name, transformedName, bytes);
			}
			if (bytes == null)
				return null;
			//byte配列を読み込み、利用しやすい形にする。
			ClassReader cr = new ClassReader(bytes);
			//これのvisitを呼ぶことによって情報が溜まっていく。
			ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);//maxStack,maxLocal,frameの全てを計算
			//Adapterを通して書き換え出来るようにする。
			ClassVisitor cv = cw;
			cv = MixinAccessorAdapter.transformAccessor(cv, transformedName);
			cv = TF_ChatAllowedCharacters.appendVisitor(cv, transformedName);
			cv = TF_CPacketChatMessage.appendVisitor(cv, transformedName);
			cv = TF_FontRenderer.appendVisitor(cv, transformedName);
			cv = TF_FontRendererHook.appendVisitor(cv, transformedName);
			cv = TF_GlStateManager.appendVisitor(cv, transformedName);
			cv = TF_GuiChat.appendVisitor(cv, transformedName);
			cv = TF_GuiCommandBlock.appendVisitor(cv, transformedName);
			cv = TF_GuiCreateWorld.appendVisitor(cv, transformedName);
			cv = TF_GuiEditCommandBlockMinecart.appendVisitor(cv, transformedName);
			cv = TF_GuiEditSign.appendVisitor(cv, transformedName);
			cv = TF_GuiNewChat.appendVisitor(cv, transformedName);
			cv = TF_GuiRepair.appendVisitor(cv, transformedName);
			cv = TF_GuiScreen.appendVisitor(cv, transformedName);
			cv = TF_GuiScreenAddServer.appendVisitor(cv, transformedName);
			cv = TF_GuiScreenBook.appendVisitor(cv, transformedName);
			cv = TF_GuiUtilRenderComponents.appendVisitor(cv, transformedName);
			cv = TF_GuiWorldEdit.appendVisitor(cv, transformedName);
			cv = TF_ItemWrittenBook.appendVisitor(cv, transformedName);
			cv = TF_Loader.appendVisitor(cv, transformedName);
			cv = TF_NetHandlerPlayServer.appendVisitor(cv, transformedName);
			cv = TF_Render.appendVisitor(cv, transformedName);
			cv = TF_TeleportToTeam$TeamSelectionObject.appendVisitor(cv, transformedName);
			cv = TF_TextFormatting.appendVisitor(cv, transformedName);
			cv = TF_TileEntitySignRenderer.appendVisitor(cv, transformedName);
			//gtceu
			cv = TF_TextFieldWidget2.appendVisitor(cv, transformedName);

			if (cv == cw)
				return bytes;

			//元のクラスと同様の順番でvisitメソッドを呼んでくれる
			cr.accept(cv, 0);

			byte[] new_bytes = cw.toByteArray();

			//Writer内の情報をbyte配列にして返す。
			return new_bytes;
		} catch (Exception e) {
			System.out.println(transformedName);
			System.out.println(e.getMessage());
			e.printStackTrace();
			throw e;
		}
	}

}
