package erogenousbeef.bigreactors.api.data;

import erogenousbeef.bigreactors.api.registry.Reactants;

/**
 * Used to map any object which is registered in the ore dictionary to
 * a reactant.
 * @author Erogenous Beef
 */
public class OreDictToReactantMapping extends SourceProductMapping {

	public OreDictToReactantMapping(String oreDictName, int oreAmount,
									String reactantName, int reactantAmount) {
		super(oreDictName, oreAmount, reactantName, reactantAmount);
	}

	public OreDictToReactantMapping(String oreDictName, int oreAmount, String reactantName) {
		super(oreDictName, oreAmount, reactantName, Reactants.standardSolidReactantAmount);
	}

	public OreDictToReactantMapping(String oreDictName, String reactantName, int reactantAmount) {
		super(oreDictName, 1, reactantName, reactantAmount);
	}
	
	public OreDictToReactantMapping(String oreDictName, String reactantName) {
		super(oreDictName, 1, reactantName, Reactants.standardSolidReactantAmount);
	}
}
