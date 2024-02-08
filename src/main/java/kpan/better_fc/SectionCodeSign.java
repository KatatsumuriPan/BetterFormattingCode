package kpan.better_fc;

import kpan.better_fc.config.ConfigHolder;
import kpan.better_fc.util.MyReflectionHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;

public class SectionCodeSign {

	private static long lastTypedTime = 0;

	public static void onKeyTyped(GuiScreen guiScreen, char typedChar, int keyCode) {
		if (typedChar != ConfigHolder.client.charToSectionSign || ConfigHolder.client.typeThreshold_ms == 0) {
			resetTime();
			invokeKeyTyped(guiScreen, typedChar, keyCode);
			return;
		}

		long time = Minecraft.getSystemTime();
		if (time - lastTypedTime <= ConfigHolder.client.typeThreshold_ms) {
			resetTime();
			invokeKeyTyped(guiScreen, '\b', Keyboard.KEY_BACK);
			invokeKeyTyped(guiScreen, '§', Keyboard.KEY_SECTION);
		} else {
			lastTypedTime = time;
			invokeKeyTyped(guiScreen, typedChar, keyCode);
		}
	}


	public static void resetTime() {
		lastTypedTime = 0;
	}

	private static void invokeKeyTyped(GuiScreen guiScreen, char typedChar, int keyCode) {
		MyReflectionHelper.invokeObfPrivateMethod(GuiScreen.class, guiScreen, "keyTyped", "func_73869_a", typedChar, keyCode);
	}
}
