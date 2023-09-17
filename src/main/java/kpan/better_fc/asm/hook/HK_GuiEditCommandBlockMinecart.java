package kpan.better_fc.asm.hook;

import kpan.better_fc.asm.tf.EnumSectionSignMode;
import net.minecraft.client.gui.inventory.GuiEditCommandBlockMinecart;

@SuppressWarnings("unused")
public class HK_GuiEditCommandBlockMinecart {
	GuiEditCommandBlockMinecart guiCommandBlock;

	public static int getCommandMaxLength() {
		return HK_GuiCommandBlock.getCommandMaxLength();
	}

	public static EnumSectionSignMode getSectionSignMode() {
		return HK_GuiCommandBlock.getSectionSignMode();
	}
}
