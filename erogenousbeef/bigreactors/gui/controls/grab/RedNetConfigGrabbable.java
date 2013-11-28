package erogenousbeef.bigreactors.gui.controls.grab;

import net.minecraft.util.Icon;
import erogenousbeef.bigreactors.common.tileentity.TileEntityReactorRedNetPort;

public class RedNetConfigGrabbable implements IBeefGuiGrabbable {

	protected String name;
	protected Icon icon;
	protected TileEntityReactorRedNetPort.CircuitType circuitType;
	
	public RedNetConfigGrabbable(String name, Icon icon, TileEntityReactorRedNetPort.CircuitType circuitType) {
		this.name = name;
		this.icon = icon;
		this.circuitType = circuitType;
	}
	
	@Override
	public Icon getIcon() {
		return icon;
	}
	
	public TileEntityReactorRedNetPort.CircuitType GetCircuitType() {
		return circuitType;
	}
	
	@Override
	public boolean equals(Object other) {
		if(other instanceof RedNetConfigGrabbable) {
			return this.circuitType == ((RedNetConfigGrabbable)other).circuitType;
		}
		return false;
	}
	
	public String getName() {
		return name;
	}
}
