package kpan.better_fc.asm.tf;

import kpan.better_fc.ModTagsGenerated;
import kpan.better_fc.asm.core.AsmTypes;
import kpan.better_fc.asm.core.AsmUtil;
import kpan.better_fc.asm.core.MyAsmNameRemapper.MethodRemap;

public class References {
	public static final String GuiTextField = "net.minecraft.client.gui.GuiTextField";
	public static final String FontRenderer = "net.minecraft.client.gui.FontRenderer";
	public static final String EnumSectionSignMode = ModTagsGenerated.MODGROUP + ".asm.tf." + "EnumSectionSignMode";
	public static final String ModifiedGuiTextField = ModTagsGenerated.MODGROUP + ".api." + "ModifiedGuiTextField";

	private static final String ChatAllowedCharacters = "net.minecraft.util.ChatAllowedCharacters";
	public static final MethodRemap drawTextBox = new MethodRemap(GuiTextField, "drawTextBox", AsmTypes.METHOD_VOID, "func_146194_f");
	public static final MethodRemap textboxKeyTyped = new MethodRemap(GuiTextField, "textboxKeyTyped", AsmUtil.toMethodDesc(AsmTypes.BOOL, AsmTypes.CHAR, AsmTypes.INT), "func_146201_a");
	public static final MethodRemap mouseClicked = new MethodRemap(GuiTextField, "mouseClicked", AsmUtil.toMethodDesc(AsmTypes.BOOL, AsmTypes.INT, AsmTypes.INT, AsmTypes.INT), "func_146192_a");
	public static final MethodRemap setText = new MethodRemap(GuiTextField, "setText", AsmUtil.toMethodDesc(AsmTypes.VOID, AsmTypes.STRING), "func_146180_a");
	public static final MethodRemap setSelectionPos = new MethodRemap(GuiTextField, "setSelectionPos", AsmUtil.toMethodDesc(AsmTypes.VOID, AsmTypes.INT), "func_146199_i");
	public static final MethodRemap isAllowedCharacter = new MethodRemap(ChatAllowedCharacters, "isAllowedCharacter", AsmUtil.toMethodDesc(AsmTypes.BOOL, AsmTypes.CHAR), "func_71566_a");
	public static final MethodRemap filterAllowedCharacters = new MethodRemap(ChatAllowedCharacters, "filterAllowedCharacters", AsmUtil.toMethodDesc(AsmTypes.STRING, AsmTypes.STRING), "func_71565_a");
	public static final MethodRemap writeText = new MethodRemap(ChatAllowedCharacters, "writeText", AsmUtil.toMethodDesc(AsmTypes.VOID, AsmTypes.STRING), "func_146191_b");
	public static final MethodRemap setMaxStringLength = new MethodRemap(GuiTextField, "setMaxStringLength", AsmUtil.toMethodDesc(AsmTypes.VOID, AsmTypes.INT), "func_146203_f");
	public static final MethodRemap getText = new MethodRemap(GuiTextField, "getText", AsmUtil.toMethodDesc(AsmTypes.STRING), "func_146179_b");
	public static final MethodRemap init = new MethodRemap(GuiTextField, "<init>", AsmUtil.toMethodDesc(AsmTypes.VOID, AsmTypes.INT, References.FontRenderer, AsmTypes.INT, AsmTypes.INT, AsmTypes.INT, AsmTypes.INT), "<init>");

}
