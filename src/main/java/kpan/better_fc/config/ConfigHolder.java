package kpan.better_fc.config;

import kpan.better_fc.config.ConfigAnnotations.Comment;
import net.minecraftforge.common.config.Configuration;

public class ConfigHolder {

	//	@Comment("Common settings(Blocks, items, etc.)") //
	//	public static Common common = new Common();

	public static class Common {

	}

	@Comment("Client only settings(Rendering, resources, etc.)") //
	public static Client client = new Client();

	public static class Client {

		@Comment("Gui ID settings") //
		public GuiIDs Gui_IDs = new GuiIDs();

		public static class GuiIDs {

			@Comment("Gui ID1") //
			public int GuiId1 = 31;
		}

	}

	//	@Comment("Server settings(Behaviors, phisics, etc.)") //
	//	public static Server server = new Server();

	public static class Server {

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
