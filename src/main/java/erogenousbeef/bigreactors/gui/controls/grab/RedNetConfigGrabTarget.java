package erogenousbeef.bigreactors.gui.controls.grab;

import erogenousbeef.bigreactors.client.gui.BeefGuiBase;
import erogenousbeef.bigreactors.client.gui.GuiReactorRedNetPort;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityReactorRedNetPort;
import erogenousbeef.bigreactors.gui.IBeefTooltipControl;

public class RedNetConfigGrabTarget extends BeefGuiGrabTarget implements IBeefTooltipControl {

	TileEntityReactorRedNetPort port;
	int channel;
	String tooltip;
	TileEntityReactorRedNetPort.CircuitType currentCircuitType;
	
	public RedNetConfigGrabTarget(BeefGuiBase container, int x, int y, TileEntityReactorRedNetPort port, int channel) {
		super(container, x, y);
		this.port = port;
		this.channel = channel;
		this.tooltip = null;
		currentCircuitType = port.getChannelCircuitType(channel);
	}
	
	@Override
	public void onSlotCleared() {
		currentCircuitType = TileEntityReactorRedNetPort.CircuitType.DISABLED;
		tooltip = null;
		
		if(guiContainer instanceof GuiReactorRedNetPort) {
			((GuiReactorRedNetPort)guiContainer).onChannelChanged(this.channel);
		}
	}

	@Override
	public void onSlotSet() {
		currentCircuitType = ((RedNetConfigGrabbable)this.grabbable).GetCircuitType();
		tooltip = ((RedNetConfigGrabbable)this.grabbable).getName();
		
		if(guiContainer instanceof GuiReactorRedNetPort) {
			((GuiReactorRedNetPort)guiContainer).onChannelChanged(this.channel);
		}		
	}

	@Override
	public boolean isAcceptedGrab(IBeefGuiGrabbable grabbedItem) {
		return grabbedItem instanceof RedNetConfigGrabbable;
	}
	
	public boolean hasChanged() {
		return currentCircuitType != this.port.getChannelCircuitType(channel);
	}
	
	public int getChannel() { return channel; }
	public TileEntityReactorRedNetPort.CircuitType getCircuitType() { return this.currentCircuitType; }

	@Override
	public String[] getTooltip() {
		return new String[] { tooltip };
	}
}
