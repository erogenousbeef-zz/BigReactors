package welfare93.bigreactors.energy;

import net.minecraftforge.common.util.ForgeDirection;
import ic2.api.energy.tile.IEnergySink;

public interface IEnergyHandler {

	public int getEnergyStored(ForgeDirection direction);
	public int getMaxEnergyStored(ForgeDirection direction);
	public int addEnergy(int energy);
	public int removeEnergy(int energy);
}
