package erogenousbeef.bigreactors.net;

import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.net.message.*;

public class CommonPacketHandler {

    public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(BigReactors.CHANNEL.toLowerCase());

    public static void initServer() {
        INSTANCE.registerMessage(MultiblockMessageClient.Handler.class, MultiblockMessageClient.class, 1, Side.CLIENT);
        INSTANCE.registerMessage(ReactorWasteEjectionSettingMessage.Handler.class, ReactorWasteEjectionSettingMessage.class, 3, Side.CLIENT);
        INSTANCE.registerMessage(SmallMachineUIMessage.Handler.class, SmallMachineUIMessage.class, 5, Side.CLIENT);
        INSTANCE.registerMessage(SmallMachineRotationMessage.Handler.class, SmallMachineRotationMessage.class, 7, Side.CLIENT);
        INSTANCE.registerMessage(SmallMachineInventoryExposureMessage.Handler.class, SmallMachineInventoryExposureMessage.class, 9, Side.CLIENT);
        INSTANCE.registerMessage(SmallMachineFluidExposureMessage.Handler.class, SmallMachineFluidExposureMessage.class, 11, Side.CLIENT);
        INSTANCE.registerMessage(ControlRodUpdateMessage.Handler.class, ControlRodUpdateMessage.class, 13, Side.CLIENT);
    }
    
    public static void initClient() {
        INSTANCE.registerMessage(MultiblockMessageServer.Handler.class, MultiblockMessageServer.class, 0, Side.SERVER);
        INSTANCE.registerMessage(GuiButtonPressMessage.Handler.class, GuiButtonPressMessage.class, 2, Side.SERVER);
        INSTANCE.registerMessage(ControlRodSetNameMessage.Handler.class, ControlRodSetNameMessage.class, 4, Side.SERVER);
        INSTANCE.registerMessage(RedNetSetDataMessage.Handler.class, RedNetSetDataMessage.class, 6, Side.SERVER);
        INSTANCE.registerMessage(RedstoneSetDataMessage.Handler.class, RedstoneSetDataMessage.class, 8, Side.SERVER);
    }
}
