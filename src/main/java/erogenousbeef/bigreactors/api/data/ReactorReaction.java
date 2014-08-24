package erogenousbeef.bigreactors.api.data;

public class ReactorReaction extends SourceProductMapping {

	public static final float standardReactivity = 1.05f;
	public static final float standardFissionRate = 0.01f;
	
	// An exponent applied to the number of fission events
	// to determine the base amount of radiation created.
	// Raising this produces more radiation per fuel unit.
	protected float reactivity;

	// How fast this reaction occurs. Each tick, the reactor will multiply
	// this number by the amount of source reactant to determine how
	// much source reactant actually reacts.
	// Raising this will cause the reactor to burn this reactant faster.
	protected float fissionRate;
	
	/**
	 * This is the reaction of 1 unit of source to 1 unit of product.
	 * Floating-point quantities will be used, so this registration
	 * is for a Unit Reaction of 1 unit of reactant to 1 unit of product.
	 * @param sourceKey
	 * @param productKey
	 */
	public ReactorReaction(String sourceKey, String productKey) {
		super(sourceKey, 1, productKey, 1);
		
		this.reactivity = standardReactivity;
		this.fissionRate = standardFissionRate;
	}

	public ReactorReaction(String sourceKey, String productKey, float reactivity, float fissionRate) {
		super(sourceKey, 1, productKey, 1);
		this.reactivity = reactivity;
		this.fissionRate = fissionRate;
	}
	
	public float getReactivity() { return reactivity; }
	public float getFissionRate() { return fissionRate; }

}
