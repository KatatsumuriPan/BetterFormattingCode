package kpan.better_fc.asm.hook;

import kpan.better_fc.asm.core.ASMTransformer;
import kpan.better_fc.asm.core.AsmUtil;
import kpan.better_fc.util.MyReflectionHelper;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.common.asm.ASMTransformerWrapper.TransformerWrapper;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.IntStream;

public class HK_Loader {
	public static void onConstructed() {
		ClassLoader classLoader = ASMTransformer.class.getClassLoader();
		List<IClassTransformer> transformers = MyReflectionHelper.getPrivateField(classLoader, "transformers");


		//うちのtransformerがBetterLineBreakのものよりも後に来るように設定
		//具体的には順序が逆であればスワップ
		int betterLineBreakIdx = indexOf(transformers, transformer -> {
			if (transformer instanceof TransformerWrapper) {
				IClassTransformer parent = MyReflectionHelper.getPrivateField(TransformerWrapper.class, transformer, "parent");
				return parent.getClass().getName().equals("kpan.b_line_break.asm.core.ASMTransformer");
			}
			return false;
		});
		if (betterLineBreakIdx != -1) {
			int my_idx = indexOf(transformers, transformer -> {
				if (transformer instanceof TransformerWrapper) {
					IClassTransformer parent = MyReflectionHelper.getPrivateField(TransformerWrapper.class, transformer, "parent");
					return parent.getClass() == ASMTransformer.class;
				}
				return false;
			});
			if (my_idx < betterLineBreakIdx) {
				IClassTransformer tmp = transformers.get(my_idx);
				transformers.set(my_idx, transformers.get(betterLineBreakIdx));
				transformers.set(betterLineBreakIdx, tmp);
			}
		}

		//うちのtransformerがSmoothFontのものよりも後に来るように設定
		//具体的には順序が逆であればスワップ
		//加えて、開発環境ならSmoothFontのtransformerを削除
		int smoothfont_idx = indexOf(transformers, transformer -> {
			if (transformer instanceof TransformerWrapper) {
				IClassTransformer parent = MyReflectionHelper.getPrivateField(TransformerWrapper.class, transformer, "parent");
				return parent.getClass().getName().equals("bre.smoothfont.asm.Transformer");
			}
			return false;
		});
		if (smoothfont_idx == -1)
			return;
		if (AsmUtil.isDeobfEnvironment()) {
			transformers.remove(smoothfont_idx);
			return;
		}
		int my_idx = indexOf(transformers, transformer -> {
			if (transformer instanceof TransformerWrapper) {
				IClassTransformer parent = MyReflectionHelper.getPrivateField(TransformerWrapper.class, transformer, "parent");
				return parent.getClass() == ASMTransformer.class;
			}
			return false;
		});
		if (my_idx < smoothfont_idx) {
			IClassTransformer tmp = transformers.get(my_idx);
			transformers.set(my_idx, transformers.get(smoothfont_idx));
			transformers.set(smoothfont_idx, tmp);
		}
	}

	private static <T> int indexOf(List<T> list, Predicate<T> predicate) {
		return IntStream.range(0, list.size()).filter(i -> predicate.test(list.get(i))).findFirst().orElse(-1);
	}
}
