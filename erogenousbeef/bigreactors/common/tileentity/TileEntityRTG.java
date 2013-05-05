package erogenousbeef.bigreactors.common.tileentity;

import net.minecraft.tileentity.TileEntity;

public class TileEntityRTG extends TileEntity {

	private int heat;
	
	public boolean isActive() { return heat > 0; }
	
	public int getHeat() {
		return heat;
	}
	
}
