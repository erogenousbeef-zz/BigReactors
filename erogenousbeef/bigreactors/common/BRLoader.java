package erogenousbeef.bigreactors.common;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkMod.SidedPacketHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import erogenousbeef.bigreactors.client.ClientPacketHandler;
import erogenousbeef.bigreactors.net.ServerPacketHandler;
import erogenousbeef.bigreactors.net.ConnectionHandler;


@Mod(modid = BigReactors.CHANNEL, name = BigReactors.NAME, version = BRConfig.VERSION, acceptedMinecraftVersions = "[1.5,)")
@NetworkMod(clientSideRequired = true, serverSideRequired = false, connectionHandler = ConnectionHandler.class, 
			clientPacketHandlerSpec = @SidedPacketHandler(channels = { BigReactors.CHANNEL }, packetHandler = ClientPacketHandler.class),
			serverPacketHandlerSpec = @SidedPacketHandler(channels = { BigReactors.CHANNEL }, packetHandler = ServerPacketHandler.class))
public class BRLoader {

	@Instance("BigReactors")
	public static BRLoader instance;

	@SidedProxy(clientSide = "erogenousbeef.bigreactors.client.ClientProxy", serverSide = "erogenousbeef.bigreactors.common.CommonProxy")
	public static CommonProxy proxy;
	
	@PreInit
	public void preInit(FMLPreInitializationEvent event)
	{
		BigReactors.registerOres(0, true);
		BigReactors.registerIngots(0, true);
		BigReactors.registerFuelRods(0, true);
		BigReactors.registerReactorPartBlocks(0, true);
		BigReactors.registerSmallMachines(0,  true);
		BigReactors.registerYelloriumLiquids(0,  true);

		proxy.preInit();
	}

	@Init
	public void load(FMLInitializationEvent evt)
	{
		proxy.init();
		BigReactors.register(this);
	}	
}
