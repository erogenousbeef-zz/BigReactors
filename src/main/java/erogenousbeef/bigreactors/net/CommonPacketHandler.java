package erogenousbeef.bigreactors.net;

import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.net.message.*;

public class CommonPacketHandler {

    public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(BigReactors.CHANNEL.toLowerCase());

    public static void init() {
        INSTANCE.registerMessage(MultiblockNetworkHandlerMessageServer.class, MultiblockNetworkHandlerMessageServer.class, 0, Side.SERVER);
        INSTANCE.registerMessage(MultiblockNetworkHandlerMessageClient.class, MultiblockNetworkHandlerMessageClient.class, 1, Side.CLIENT);
        INSTANCE.registerMessage(GuiButtonPressMessage.class, GuiButtonPressMessage.class, 2, Side.SERVER);
        INSTANCE.registerMessage(ControlRodSetNameMessage.class, ControlRodSetNameMessage.class, 3, Side.SERVER);
        INSTANCE.registerMessage(RedNetSetDataMessage.class, RedNetSetDataMessage.class, 4, Side.SERVER);
        INSTANCE.registerMessage(RedstoneSetDataMessage.class, RedstoneSetDataMessage.class, 5, Side.SERVER);
        INSTANCE.registerMessage(ReactorWasteEjectionSettingMessage.class, ReactorWasteEjectionSettingMessage.class, 6, Side.CLIENT);
        INSTANCE.registerMessage(SmallMachineUIMessage.class, SmallMachineUIMessage.class, 7, Side.CLIENT);
        INSTANCE.registerMessage(SmallMachineRotationMessage.class, SmallMachineRotationMessage.class, 8, Side.CLIENT);
        INSTANCE.registerMessage(SmallMachineInventoryExposureMessage.class, SmallMachineInventoryExposureMessage.class, 9, Side.CLIENT);
        INSTANCE.registerMessage(SmallMachineFluidExposureMessage.class, SmallMachineFluidExposureMessage.class, 10, Side.CLIENT);
        INSTANCE.registerMessage(ControlRodUpdateMessage.class, ControlRodUpdateMessage.class, 11, Side.CLIENT);
    }
}
