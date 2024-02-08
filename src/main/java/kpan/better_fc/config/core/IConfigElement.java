package kpan.better_fc.config.core;

import kpan.better_fc.config.core.gui.ModGuiConfig;
import kpan.better_fc.config.core.gui.ModGuiConfigEntries;
import kpan.better_fc.config.core.gui.ModGuiConfigEntries.IGuiConfigEntry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.BufferedWriter;
import java.io.IOException;

public interface IConfigElement {
	int getOrder();
	void write(BufferedWriter out, int indent) throws IOException;
	boolean showInGui();

	@SideOnly(Side.CLIENT)
	IGuiConfigEntry toEntry(ModGuiConfig screen, ModGuiConfigEntries entryList);
}
