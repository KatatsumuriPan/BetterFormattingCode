package kpan.better_fc.proxy;

import kpan.better_fc.util.handlers.TickingClientHandler;
import net.minecraftforge.common.MinecraftForge;

@SuppressWarnings("unused")
public class ClientProxy extends CommonProxy {

	@Override
	public void registerOnlyClient() {
		MinecraftForge.EVENT_BUS.register(new TickingClientHandler());
	}

	@Override
	public boolean hasClientSide() { return true; }

}
