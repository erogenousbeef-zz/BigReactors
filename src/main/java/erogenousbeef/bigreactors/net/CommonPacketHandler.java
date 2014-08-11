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

    /**
     * Initialize the messages. Note that all messages (server>client and client>server)
     * must be initialized on _both_ the client and the server.
     */
    // Be careful not to reference any client code in your message handlers, such as WorldClient!
    public static void init() {
    	// Server >> Client Messages
        INSTANCE.registerMessage(DeviceUpdateMessage.Handler.class, DeviceUpdateMessage.class, 1, Side.CLIENT);
        INSTANCE.registerMessage(DeviceUpdateRotationMessage.Handler.class, DeviceUpdateRotationMessage.class, 3, Side.CLIENT);
        INSTANCE.registerMessage(DeviceUpdateInvExposureMessage.Handler.class, DeviceUpdateInvExposureMessage.class, 5, Side.CLIENT);
        INSTANCE.registerMessage(DeviceUpdateFluidExposureMessage.Handler.class, DeviceUpdateFluidExposureMessage.class, 7, Side.CLIENT);
        INSTANCE.registerMessage(ControlRodUpdateMessage.Handler.class, ControlRodUpdateMessage.class, 9, Side.CLIENT);
        INSTANCE.registerMessage(ReactorUpdateMessage.Handler.class, ReactorUpdateMessage.class, 11, Side.CLIENT);
        INSTANCE.registerMessage(ReactorUpdateWasteEjectionMessage.Handler.class, ReactorUpdateWasteEjectionMessage.class, 13, Side.CLIENT);
        INSTANCE.registerMessage(TurbineUpdateMessage.Handler.class, TurbineUpdateMessage.class, 15, Side.CLIENT);

        // Client >> Server Messages
    	INSTANCE.registerMessage(MachineCommandActivateMessage.Handler.class, MachineCommandActivateMessage.class, 0, Side.SERVER);
        INSTANCE.registerMessage(DeviceChangeExposureMessage.Handler.class, DeviceChangeExposureMessage.class, 2, Side.SERVER);
        INSTANCE.registerMessage(ControlRodChangeNameMessage.Handler.class, ControlRodChangeNameMessage.class, 4, Side.SERVER);
        INSTANCE.registerMessage(ControlRodChangeInsertionMessage.Handler.class, ControlRodChangeInsertionMessage.class, 6, Side.SERVER);
        INSTANCE.registerMessage(ReactorRedNetPortChangeMessage.Handler.class, ReactorRedNetPortChangeMessage.class, 8, Side.SERVER);
        INSTANCE.registerMessage(ReactorRedstonePortChangeMessage.Handler.class, ReactorRedstonePortChangeMessage.class, 10, Side.SERVER);
        INSTANCE.registerMessage(ReactorCommandEjectMessage.Handler.class, ReactorCommandEjectMessage.class, 12, Side.SERVER);
        INSTANCE.registerMessage(ReactorCommandEjectToPortMessage.Handler.class, ReactorCommandEjectToPortMessage.class, 14, Side.SERVER);
        INSTANCE.registerMessage(ReactorChangeWasteEjectionMessage.Handler.class, ReactorChangeWasteEjectionMessage.class, 16, Side.SERVER);
        INSTANCE.registerMessage(ReactorAccessPortChangeDirectionMessage.Handler.class, ReactorAccessPortChangeDirectionMessage.class, 18, Side.SERVER);
        INSTANCE.registerMessage(TurbineChangeMaxIntakeMessage.Handler.class, TurbineChangeMaxIntakeMessage.class, 20, Side.SERVER);
        INSTANCE.registerMessage(TurbineChangeVentMessage.Handler.class, TurbineChangeVentMessage.class, 22, Side.SERVER);
        INSTANCE.registerMessage(TurbineChangeInductorMessage.Handler.class, TurbineChangeInductorMessage.class, 24, Side.SERVER);
    }
}
