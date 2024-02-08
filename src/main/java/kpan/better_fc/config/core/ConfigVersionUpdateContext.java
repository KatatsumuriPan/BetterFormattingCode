package kpan.better_fc.config.core;

public class ConfigVersionUpdateContext {
	public final String loadedConfigVersion;
	public String categoryPath;
	public String type;
	public String name;
	public String value;
	public ConfigVersionUpdateContext(String loadedConfigVersion, String categoryPath, String type, String name, String value) {
		this.loadedConfigVersion = loadedConfigVersion;
		this.categoryPath = categoryPath;
		this.type = type;
		this.name = name;
		this.value = value;
	}
}
