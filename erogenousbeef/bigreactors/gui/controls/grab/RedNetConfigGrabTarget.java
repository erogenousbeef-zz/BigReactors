package erogenousbeef.bigreactors.gui.controls.grab;

import erogenousbeef.bigreactors.client.gui.BeefGuiBase;
import erogenousbeef.bigreactors.common.tileentity.TileEntityReactorRedNetPort;

public class RedNetConfigGrabTarget extends BeefGuiGrabTarget {

	TileEntityReactorRedNetPort port;
	int channel;
	TileEntityReactorRedNetPort.CircuitType currentCircuitType;
	
	public RedNetConfigGrabTarget(BeefGuiBase container, int x, int y, TileEntityReactorRedNetPort port, int channel) {
		super(container, x, y);
		this.port = port;
		this.channel = channel;
		currentCircuitType = port.getChannelCircuitType(channel);
	}
	
	@Override
	public void onSlotCleared() {
		currentCircuitType = TileEntityReactorRedNetPort.CircuitType.DISABLED;
	}

	@Override
	public void onSlotSet() {
		currentCircuitType = ((RedNetConfigGrabbable)this.grabbable).GetCircuitType();
	}

	@Override
	public boolean isAcceptedGrab(IBeefGuiGrabbable grabbedItem) {
		return grabbedItem instanceof RedNetConfigGrabbable;
	}
	
	public boolean hasChanged() {
		return currentCircuitType == this.port.getChannelCircuitType(channel);
	}
	
	public int getChannel() { return channel; }
	public TileEntityReactorRedNetPort.CircuitType getCircuitType() { return this.currentCircuitType; }
}
