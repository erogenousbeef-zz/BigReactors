package erogenousbeef.bigreactors.api;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 * Implement this interface to provide information on fuel for Big Reactors.
 */
public interface IReactorFuel {
	/**
	 * Is this fuel equal to another? Should be identical to equals(otherFuel)
	 * @param otherFuel The IReactorFuel to compare.
	 * @return True if these fuels are equal, false otherwise.
	 */
	public boolean isFuelEqual(IReactorFuel otherFuel);
	
	/**
	 * Does this ReactorFuel object govern data related to this item stack?
	 * Essentially an isItemEqual check between this object's reference item and
	 * the parameter.
	 * @param item The ItemStack to compare.
	 * @return True if the reference item is equal to the parameter, false otherwise.
	 */
	public boolean isFuelEqual(ItemStack item);
	
	/**
	 * Return the "reference item" for this fuel object. This is the Minecraft item
	 * for which this ReactorFuel object defines properties.
	 * @return The ItemStack representing the item which this Reactor Fuel object governs.
	 */
	public ItemStack getReferenceItem();
	
	/**
	 * Returns the basic color of your fuel in RRGGBB format. This will be used for
	 * color blending in fuel rods. In hex format, colors are 0xRRGGBB.
	 * @return A color in 0xRRGGBB format.
	 */
	public int getFuelColor();
	
	/**
	 * Standard Java equals operator. This is used by the registry, so it's required.
	 * Fuels should generally be equal if their reference items are equal.
	 * @param otherObject The other object with which you should compare.
	 * @return True if the fuels are equal, false otherwise.
	 */
	public boolean equals(Object otherObject);

	// TODO: Add fuel characteristics here
}
