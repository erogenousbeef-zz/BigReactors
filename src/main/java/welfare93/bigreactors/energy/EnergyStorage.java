package welfare93.bigreactors.energy;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

public class EnergyStorage implements IEnergyHandler {

	int energy,energymax;
	public EnergyStorage(int maxEnergyStored) {
		energymax=maxEnergyStored;
	}

	public void writeToNBT(NBTTagCompound energyTag) {
		energyTag.setInteger("energy", energy);
	}

	public int extractEnergy(double cycleEnergyCost, boolean b) {
		int d=(int)Math.min(cycleEnergyCost, energy);
		energy-=d;
		return d;
	}

	public int getEnergyStored() {
		return energy;
	}

	public void readFromNBT(NBTTagCompound compoundTag) {
		energy=compoundTag.getInteger("energy");
		
	}

	public int receiveEnergy(double maxReceive, boolean simulate) {
		int d=(int)Math.min(maxReceive+energy, energymax);
		int c=(int)Math.max(energy+maxReceive-energymax,0);
		energy=d;
		return c;
	}

	public int getMaxEnergyStored() {
		return energymax;
	}

	@Override
	public int getEnergyStored(ForgeDirection direction) {
		return energy;
	}

	@Override
	public int getMaxEnergyStored(ForgeDirection direction) {
		return energymax;
	}

	@Override
	public int addEnergy(int energys) {
		int c=(int)Math.max(0, energy+energys-energymax);
		energy=Math.min(energymax, energy+energys);
		return c;
	}

	@Override
	public int removeEnergy(int energys) {
		if(energys>energy)
		{
			int c=(int)energy;
			energy=0;
			return c;
		}
		energy-=energys;
		return energys;
	}

}
