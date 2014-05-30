package erogenousbeef.bigreactors.common.interfaces;


/**
 * Implement on entities which expose their internal fueling state
 * @author Erogenous Beef
 */
public interface IReactorFuelInfo {

	/**
	 * @return The amount of fuel contained in the entity.
	 */
	int getFuelAmount();
	
	/**
	 * @return The amount of waste contained in the entity.
	 */
	int getWasteAmount();

	/**
	 * @return An integer representing the maximum amount of fuel + waste, combined, the entity can contain.
	 */
	int getCapacity();
	
	/**
	 * @return The number of fuel rods in this entity
	 */
	int getFuelRodCount();
}
