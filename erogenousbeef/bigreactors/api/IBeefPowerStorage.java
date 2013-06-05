package erogenousbeef.bigreactors.api;

/**
 * Interface that allows the GUI to access power storage information
 * regardless of whether your entity is a multiblock or a standard TE.
 * @author Yoru
 */
public interface IBeefPowerStorage {
	
	/**
	 * @return Returns the amount of energy stored, in internal units (MJ)
	 */
	public int getEnergyStored();
	
	/**
	 * @return Returns the amount of energy stored, in internal units (MJ)
	 */
	public int getMaxEnergyStored();
	
}
