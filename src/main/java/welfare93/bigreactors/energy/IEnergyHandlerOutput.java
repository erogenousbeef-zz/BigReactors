package welfare93.bigreactors.energy;

import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergySource;
import net.minecraftforge.common.util.ForgeDirection;

public interface IEnergyHandlerOutput extends IEnergySource,IEnergyHandler {

	public int getEnergyStored(ForgeDirection direction);
	public int getMaxEnergyStored(ForgeDirection direction);
}
