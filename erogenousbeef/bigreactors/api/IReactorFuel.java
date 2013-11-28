package erogenousbeef.bigreactors.api;

import net.minecraftforge.fluids.Fluid;

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
	 * Does this ReactorFuel object govern data related to this fluid?
	 * Essentially an isFluidEqual check.
	 * 
	 * @param fluid The Fluid to compare.
	 * @return True if the reference fluid is equal to the parameter.
	 */
	public boolean isFuelEqual(Fluid fluid);
	
	/**
	 * Return the reference fluid for this fuel. This is the Forge Fluid for which
	 * this ReactorFuel object defines properties.
	 * @return The Fluid which this Reactor Fuel object governs.
	 */
	public Fluid getReferenceFluid();
	
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
	/**
	 * Fuels are converted into other types of Fluids as they get processed.
	 * For now, any given Fuel/Waste Fluid is only converted into one other.
	 * @return The Fluid representing the converted product of this Fuel. Null is acceptable and means the Fuel is simply consumed.
	 */
	public Fluid getProductFluid();
	
	public boolean isFuel();
	
	public boolean isWaste();
}
