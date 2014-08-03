package erogenousbeef.bigreactors.net;

import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.net.message.*;

public class CommonPacketHandler {

	/*
	 * Naming Convention:
	 *  Client >> Server
	 *   [Machine|TileEntity]ChangeMessage -- a full state change message (for large/batch commits)
	 *   [Machine|TileEntity]Change[Datum]Message -- a client request to change [Datum]
	 *  
	 *  Server >> Client
	 *   [Machine|TileEntity]UpdateMessage  -- a full state update
	 *   [Machine|TileEntity]Update[Datum]Message -- an update for only [Datum]
	 *   
	 *   Generic Format: [Machine|TileEntity][Operation][Type]Message
	 */
	
    public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(BigReactors.CHANNEL.toLowerCase());

    public static void init() {
    	initServer();
    	initClient();
    }
    
    public static void initServer() {
        INSTANCE.registerMessage(MultiblockMessageClient.Handler.class, MultiblockMessageClient.class, 1, Side.CLIENT);
        INSTANCE.registerMessage(ReactorUpdateWasteEjectionMessage.Handler.class, ReactorUpdateWasteEjectionMessage.class, 3, Side.CLIENT);
        INSTANCE.registerMessage(DeviceUpdateMessage.Handler.class, DeviceUpdateMessage.class, 5, Side.CLIENT);
        INSTANCE.registerMessage(DeviceUpdateRotationMessage.Handler.class, DeviceUpdateRotationMessage.class, 7, Side.CLIENT);
        INSTANCE.registerMessage(DeviceUpdateInvExposureMessage.Handler.class, DeviceUpdateInvExposureMessage.class, 9, Side.CLIENT);
        INSTANCE.registerMessage(DeviceUpdateFluidExposureMessage.Handler.class, DeviceUpdateFluidExposureMessage.class, 11, Side.CLIENT);
        INSTANCE.registerMessage(ControlRodUpdateMessage.Handler.class, ControlRodUpdateMessage.class, 13, Side.CLIENT);
    }
    
    public static void initClient() {
        INSTANCE.registerMessage(MultiblockMessageServer.Handler.class, MultiblockMessageServer.class, 0, Side.SERVER);
        INSTANCE.registerMessage(DeviceChangeExposureMessage.Handler.class, DeviceChangeExposureMessage.class, 2, Side.SERVER);
        INSTANCE.registerMessage(ControlRodChangeNameMessage.Handler.class, ControlRodChangeNameMessage.class, 4, Side.SERVER);
        INSTANCE.registerMessage(ReactorChangeRedNetMessage.Handler.class, ReactorChangeRedNetMessage.class, 6, Side.SERVER);
        INSTANCE.registerMessage(ReactorChangeRedstoneMessage.Handler.class, ReactorChangeRedstoneMessage.class, 8, Side.SERVER);
        INSTANCE.registerMessage(ControlRodChangeInsertionMessage.Handler.class, ControlRodChangeInsertionMessage.class, 10, Side.SERVER);
    }
}
