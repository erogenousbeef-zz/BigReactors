package erogenousbeef.bigreactors.net;

import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.net.message.*;
import erogenousbeef.bigreactors.net.message.multiblock.*;

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

    public static void initMessages(Side side) {
    	// Server >> Client Messages
        INSTANCE.registerMessage(DeviceUpdateMessage.Handler.class, DeviceUpdateMessage.class, 1, side);
        INSTANCE.registerMessage(DeviceUpdateRotationMessage.Handler.class, DeviceUpdateRotationMessage.class, 3, side);
        INSTANCE.registerMessage(DeviceUpdateInvExposureMessage.Handler.class, DeviceUpdateInvExposureMessage.class, 5, side);
        INSTANCE.registerMessage(DeviceUpdateFluidExposureMessage.Handler.class, DeviceUpdateFluidExposureMessage.class, 7, side);
        INSTANCE.registerMessage(ControlRodUpdateMessage.Handler.class, ControlRodUpdateMessage.class, 9, side);
        INSTANCE.registerMessage(ReactorUpdateMessage.Handler.class, ReactorUpdateMessage.class, 11, side);
        INSTANCE.registerMessage(ReactorUpdateWasteEjectionMessage.Handler.class, ReactorUpdateWasteEjectionMessage.class, 13, side);
        INSTANCE.registerMessage(TurbineUpdateMessage.Handler.class, TurbineUpdateMessage.class, 15, side);

        // Client >> Server Messages
    	INSTANCE.registerMessage(MachineCommandActivateMessage.Handler.class, MachineCommandActivateMessage.class, 0, side);
        INSTANCE.registerMessage(DeviceChangeExposureMessage.Handler.class, DeviceChangeExposureMessage.class, 2, side);
        INSTANCE.registerMessage(ControlRodChangeNameMessage.Handler.class, ControlRodChangeNameMessage.class, 4, side);
        INSTANCE.registerMessage(ControlRodChangeInsertionMessage.Handler.class, ControlRodChangeInsertionMessage.class, 6, side);
        INSTANCE.registerMessage(ReactorRedNetPortChangeMessage.Handler.class, ReactorRedNetPortChangeMessage.class, 8, side);
        INSTANCE.registerMessage(ReactorRedstonePortChangeMessage.Handler.class, ReactorRedstonePortChangeMessage.class, 10, side);
        INSTANCE.registerMessage(ReactorCommandEjectMessage.Handler.class, ReactorCommandEjectMessage.class, 12, side);
        INSTANCE.registerMessage(ReactorCommandEjectToPortMessage.Handler.class, ReactorCommandEjectToPortMessage.class, 14, side);
        INSTANCE.registerMessage(ReactorChangeWasteEjectionMessage.Handler.class, ReactorChangeWasteEjectionMessage.class, 16, side);
        INSTANCE.registerMessage(ReactorAccessPortChangeDirectionMessage.Handler.class, ReactorAccessPortChangeDirectionMessage.class, 18, side);
        INSTANCE.registerMessage(TurbineChangeMaxIntakeMessage.Handler.class, TurbineChangeMaxIntakeMessage.class, 20, side);
        INSTANCE.registerMessage(TurbineChangeVentMessage.Handler.class, TurbineChangeVentMessage.class, 22, side);
        INSTANCE.registerMessage(TurbineChangeInductorMessage.Handler.class, TurbineChangeInductorMessage.class, 24, side);
    }
    
    // server >> client, use odd numbers
    protected static void initClient() {
    	initMessages(Side.CLIENT);
    }
    
    // client >> server, use even numbers
    protected static void initServer() {
    	initMessages(Side.SERVER);
    }
}
