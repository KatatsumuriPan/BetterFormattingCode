package kpan.better_fc.asm.core;

import kpan.better_fc.asm.tf.TF_CPacketChatMessage;
import kpan.better_fc.asm.tf.TF_ChatAllowedCharacters;
import kpan.better_fc.asm.tf.TF_FontRenderer;
import kpan.better_fc.asm.tf.TF_GlStateManager;
import kpan.better_fc.asm.tf.TF_GuiChat;
import kpan.better_fc.asm.tf.TF_GuiCommandBlock;
import kpan.better_fc.asm.tf.TF_GuiCreateWorld;
import kpan.better_fc.asm.tf.TF_GuiEditCommandBlockMinecart;
import kpan.better_fc.asm.tf.TF_GuiEditSign;
import kpan.better_fc.asm.tf.TF_GuiNewChat;
import kpan.better_fc.asm.tf.TF_GuiRepair;
import kpan.better_fc.asm.tf.TF_GuiScreenAddServer;
import kpan.better_fc.asm.tf.TF_GuiScreenBook;
import kpan.better_fc.asm.tf.TF_GuiUtilRenderComponents;
import kpan.better_fc.asm.tf.TF_GuiWorldEdit;
import kpan.better_fc.asm.tf.TF_ItemWrittenBook;
import kpan.better_fc.asm.tf.TF_NetHandlerPlayServer;
import kpan.better_fc.asm.tf.TF_TextFormatting;
import kpan.better_fc.asm.tf.TF_TileEntitySignRenderer;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

public class ASMTransformer implements IClassTransformer {

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
			if (bytes == null)
				return null;
			//byte配列を読み込み、利用しやすい形にする。
			ClassReader cr = new ClassReader(bytes);
			//これのvisitを呼ぶことによって情報が溜まっていく。
			ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);//maxStack,maxLocal,frameの全てを計算
			//Adapterを通して書き換え出来るようにする。
			ClassVisitor cv = cw;
			cv = TF_ChatAllowedCharacters.appendVisitor(cv, transformedName);
			cv = TF_CPacketChatMessage.appendVisitor(cv, transformedName);
			cv = TF_FontRenderer.appendVisitor(cv, transformedName);
			cv = TF_GlStateManager.appendVisitor(cv, transformedName);
			cv = TF_GuiChat.appendVisitor(cv, transformedName);
			cv = TF_GuiCommandBlock.appendVisitor(cv, transformedName);
			cv = TF_GuiCreateWorld.appendVisitor(cv, transformedName);
			cv = TF_GuiEditCommandBlockMinecart.appendVisitor(cv, transformedName);
			cv = TF_GuiEditSign.appendVisitor(cv, transformedName);
			cv = TF_GuiNewChat.appendVisitor(cv, transformedName);
			cv = TF_GuiRepair.appendVisitor(cv, transformedName);
			cv = TF_GuiScreenAddServer.appendVisitor(cv, transformedName);
			cv = TF_GuiScreenBook.appendVisitor(cv, transformedName);
			cv = TF_GuiUtilRenderComponents.appendVisitor(cv, transformedName);
			cv = TF_GuiWorldEdit.appendVisitor(cv, transformedName);
			cv = TF_ItemWrittenBook.appendVisitor(cv, transformedName);
			cv = TF_NetHandlerPlayServer.appendVisitor(cv, transformedName);
			cv = TF_TextFormatting.appendVisitor(cv, transformedName);
			cv = TF_TileEntitySignRenderer.appendVisitor(cv, transformedName);


			if (cv == cw)
				return bytes;

			//元のクラスと同様の順番でvisitメソッドを呼んでくれる
			cr.accept(cv, 0);

			byte[] new_bytes = cw.toByteArray();

			//Writer内の情報をbyte配列にして返す。
			return new_bytes;
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			throw e;
		}
	}

}
