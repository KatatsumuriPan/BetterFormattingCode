package kpan.better_fc.util.handlers;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;

public class TickingClientHandler {

	public static long clientTime = 0;
	public static float partialTick = 0;

	@SubscribeEvent
	public void clientTick(TickEvent.ClientTickEvent event) {
		if (event.phase == Phase.START) {
		} else {
			clientTime++;
		}
	}
	@SubscribeEvent
	public void renderTick(TickEvent.RenderTickEvent event) {
		if (event.phase == Phase.START) {
			partialTick = event.renderTickTime;
			Minecraft mc = Minecraft.getMinecraft();
			if (!mc.getFramebuffer().isStencilEnabled())
				mc.getFramebuffer().enableStencil();
		}
	}

}
