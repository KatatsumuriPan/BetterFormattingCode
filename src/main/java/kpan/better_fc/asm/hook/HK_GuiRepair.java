package kpan.better_fc.asm.hook;

import kpan.better_fc.asm.tf.EnumSectionSignMode;
import net.minecraft.client.gui.GuiScreenBook;

public class HK_GuiRepair {
	public static int getItemNameMaxLength() {
		return 32500;
	}
	public static EnumSectionSignMode getSectionSignMode() {
		return EnumSectionSignMode.REPAIR;
	}
}
