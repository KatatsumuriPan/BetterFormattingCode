package kpan.better_fc.config;

import kpan.better_fc.config.core.ConfigAnnotations.Comment;
import kpan.better_fc.config.core.ConfigAnnotations.ConfigOrder;
import kpan.better_fc.config.core.ConfigAnnotations.Name;
import kpan.better_fc.config.core.ConfigVersionUpdateContext;

public class ConfigHolder {

	@Comment("Common settings(Blocks, items, etc.)")
	@ConfigOrder(1)
	public static Common common = new Common();

	public static class Common {

		@Name("AllowEditingFormattingChar")
		@Comment({"This option must match between server and client!",
				"If true, the formatting character '§' can be entered in text edit boxes and the text with formatting codes can be edited.",
				"If false, Editing text that contains '§' will cause the '§' to be lost and the text to be corrupted."})
		public boolean allowEditingFormattingChar;

	}

	@Comment("Client only settings(Rendering, resources, etc.)")
	@ConfigOrder(2)
	public static Client client = new Client();

	public static class Client {

	}

	//	@Comment("Server settings(Behaviors, physics, etc.)")
	//	public static Server server = new Server();

	public static class Server {

	}

	public static void updateVersion(ConfigVersionUpdateContext context) {
		switch (context.loadedConfigVersion) {
			case "1":
				break;
			default:
				throw new RuntimeException("Unknown config version:" + context.loadedConfigVersion);
		}
	}

	public static String getVersion() { return "1"; }
}
