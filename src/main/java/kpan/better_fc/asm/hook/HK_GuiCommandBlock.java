package kpan.better_fc.asm.hook;

import kpan.better_fc.asm.tf.EnumSectionSignMode;
import net.minecraft.client.gui.GuiCommandBlock;

public class HK_GuiCommandBlock {
	GuiCommandBlock guiCommandBlock;

	public static int getCommandMaxLength() {
		return 32500;
	}

	public static EnumSectionSignMode getSectionSignMode() {
		return EnumSectionSignMode.COMMAND;
	}
}
