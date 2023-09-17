package kpan.better_fc.util.handlers;

import kpan.better_fc.ModMain;
import kpan.better_fc.config.ConfigHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

@EventBusSubscriber
public class RegistryHandler {

	@SuppressWarnings("InstantiationOfUtilityClass")
	public static void preInitRegistries(@SuppressWarnings("unused") FMLPreInitializationEvent event) {
		ConfigHandler.preInit(event);
		MinecraftForge.EVENT_BUS.register(new RegistryHandler());
		ModMain.proxy.registerOnlyClient();
	}

	public static void initRegistries() {
	}

	public static void postInitRegistries() {
	}

	public static void serverRegistries(@SuppressWarnings("unused") FMLServerStartingEvent event) {
	}

//	@SubscribeEvent
//	public void onBlockRegister(RegistryEvent.Register<Block> event) {
//	}

//	@SubscribeEvent
//	public void onItemRegister(RegistryEvent.Register<Item> event) {
//	}

//	@SubscribeEvent
//	public void onEnchantmentRegister(RegistryEvent.Register<Enchantment> event) {
//	}

//	@SubscribeEvent
//	public static void registerRecipes(RegistryEvent.Register<IRecipe> event) {
//	}

//	@SubscribeEvent
//	public static void onDataSerializerRegister(RegistryEvent.Register<DataSerializerEntry> event) {
//	}

//	@SubscribeEvent
//	public void onModelRegister(ModelRegistryEvent event) {
//	}

}
