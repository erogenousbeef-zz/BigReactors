package erogenousbeef.bigreactors.api.data;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import erogenousbeef.bigreactors.api.registry.Reactants;

public class ReactantToFluidMapping extends SourceProductMapping {

	public ReactantToFluidMapping(String reactantName, int reactantAmount, String fluidName, int fluidAmount) {
		super(reactantName, reactantAmount, fluidName, fluidAmount);
	}
	
	public ReactantToFluidMapping(String reactantName, int reactantAmount, Fluid fluid, int fluidAmount) {
		super(reactantName, reactantAmount, fluid.getName(), fluidAmount);
	}
	
	public ReactantToFluidMapping(String reactantName, int reactantAmount, FluidStack fluidStack) {
		super(reactantName, reactantAmount, fluidStack.getFluid().getName(), fluidStack.amount);
	}
	
	public ReactantToFluidMapping(String reactantName, String fluidName, int fluidAmount) {
		super(reactantName, Reactants.standardFluidReactantAmount, fluidName, fluidAmount);
	}
	
	public ReactantToFluidMapping(String reactantName, Fluid fluid, int fluidAmount) {
		super(reactantName, Reactants.standardFluidReactantAmount, fluid.getName(), fluidAmount);
	}
	
	public ReactantToFluidMapping(String reactantName, FluidStack fluidStack) {
		super(reactantName, Reactants.standardFluidReactantAmount, fluidStack.getFluid().getName(), fluidStack.amount);
	}	

	public ReactantToFluidMapping(String reactantName, String fluidName) {
		super(reactantName, Reactants.standardFluidReactantAmount, fluidName, Reactants.standardFluidReactantAmount);
	}
	
	public ReactantToFluidMapping(String reactantName, Fluid fluid) {
		super(reactantName, Reactants.standardFluidReactantAmount, fluid.getName(), Reactants.standardFluidReactantAmount);
	}
	
}
