package erogenousbeef.bigreactors.gui.controls.grab;

import net.minecraft.util.IIcon;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityReactorRedNetPort;

public class RedNetConfigGrabbable implements IBeefGuiGrabbable {

	protected String name;
	protected IIcon icon;
	protected TileEntityReactorRedNetPort.CircuitType circuitType;
	
	public RedNetConfigGrabbable(String name, IIcon icon, TileEntityReactorRedNetPort.CircuitType circuitType) {
		this.name = name;
		this.icon = icon;
		this.circuitType = circuitType;
	}
	
	@Override
	public IIcon getIcon() {
		return icon;
	}
	
	public TileEntityReactorRedNetPort.CircuitType getCircuitType() {
		return circuitType;
	}
	
	@Override
	public boolean equals(Object other) {
		if(other instanceof RedNetConfigGrabbable) {
			return this.circuitType == ((RedNetConfigGrabbable)other).circuitType;
		}
		return false;
	}
	
	@Override
	public String getName() {
		return name;
	}
}
