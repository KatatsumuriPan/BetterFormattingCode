package kpan.better_fc.config;

import kpan.better_fc.config.ConfigAnnotations.BooleanValue;
import kpan.better_fc.config.ConfigAnnotations.Comment;
import net.minecraftforge.common.config.Configuration;

public class ConfigHolder {

	@Comment("Common settings")
	public static Common common = new Common();

	public static class Common {

		@BooleanValue(defaultValue = true)
		@Comment({"This option must match between server and client!",
				"If true, the formatting character '§' can be entered in text edit boxes and the text with formatting codes can be edited.",
				"If false, Editing text that contains '§' will cause the '§' to be lost and the text to be corrupted."})
		public boolean AllowEditingFormattingChar;

	}

	public static void updateVersion(Configuration config) {
		String loadedConfigVersion = config.getLoadedConfigVersion();
		switch (loadedConfigVersion) {
			case "1":
				break;
			default:
				throw new RuntimeException("Unknown config version:" + loadedConfigVersion);
		}
	}

	public static String getVersion() { return "1"; }
}
