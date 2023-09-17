package kpan.better_fc.config;

import kpan.better_fc.ModTagsGenerated;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ConfigEventHandler {

	@SubscribeEvent
	public void onConfigChanged(OnConfigChangedEvent event) {
		if (event.getModID().equals(ModTagsGenerated.MODID)) {
			ConfigHandler.syncAll();
		}
	}
}
