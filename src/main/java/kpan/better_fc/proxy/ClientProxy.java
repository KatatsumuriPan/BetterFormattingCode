package kpan.better_fc.proxy;

@SuppressWarnings("unused")
public class ClientProxy extends CommonProxy {

	@SuppressWarnings("RedundantMethodOverride")
	@Override
	public void registerOnlyClient() {
	}

	@Override
	public boolean hasClientSide() { return true; }

}
