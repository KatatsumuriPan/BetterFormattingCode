package kpan.better_fc.asm.hook;

import kpan.better_fc.asm.core.ASMTransformer;
import kpan.better_fc.util.MyReflectionHelper;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.common.asm.ASMTransformerWrapper.TransformerWrapper;

import java.util.List;
import java.util.OptionalInt;
import java.util.stream.IntStream;

public class HK_Loader {
	public static void onConstructed() {

		//うちのtransformerがSmoothFontのものよりも後に来るように設定
		//具体的には順序が逆であればスワップ
		ClassLoader classLoader = ASMTransformer.class.getClassLoader();
		List<IClassTransformer> transformers = MyReflectionHelper.getPrivateField(classLoader, "transformers");
		OptionalInt smoothfont_idx = IntStream.range(0, transformers.size())
				.filter(i -> {
					IClassTransformer transformer = transformers.get(i);
					if (transformer instanceof TransformerWrapper) {
						IClassTransformer parent = MyReflectionHelper.getPrivateField(TransformerWrapper.class, transformer, "parent");
						return parent.getClass().getName().equals("bre.smoothfont.asm.Transformer");
					}
					return false;
				}).findFirst();
		if (!smoothfont_idx.isPresent())
			return;
		int my_idx = IntStream.range(0, transformers.size())
				.filter(i -> {
					IClassTransformer transformer = transformers.get(i);
					if (transformer instanceof TransformerWrapper) {
						IClassTransformer parent = MyReflectionHelper.getPrivateField(TransformerWrapper.class, transformer, "parent");
						return parent.getClass() == ASMTransformer.class;
					}
					return false;
				}).findFirst().getAsInt();
		if (my_idx < smoothfont_idx.getAsInt()) {
			IClassTransformer tmp = transformers.get(my_idx);
			transformers.set(my_idx, transformers.get(smoothfont_idx.getAsInt()));
			transformers.set(smoothfont_idx.getAsInt(), tmp);
		}
	}
}
