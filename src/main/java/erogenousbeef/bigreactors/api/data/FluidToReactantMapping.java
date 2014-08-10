package erogenousbeef.bigreactors.api.data;

import erogenousbeef.bigreactors.api.registry.Reactants;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

public class FluidToReactantMapping extends SourceProductMapping {

	public FluidToReactantMapping(String fluidName, int fluidAmount, String reactantName, int reactantAmount) {
		super(fluidName, fluidAmount, reactantName, reactantAmount);
	}
	
	public FluidToReactantMapping(Fluid fluid, int fluidAmount, String reactantName, int reactantAmount) {
		super(fluid.getName(), fluidAmount, reactantName, reactantAmount);
	}
	
	public FluidToReactantMapping(FluidStack fluidStack, String reactantName, int reactantAmount) {
		super(fluidStack.getFluid().getName(), fluidStack.amount, reactantName, reactantAmount);
	}
	
	public FluidToReactantMapping(String fluidName, int fluidAmount, String reactantName) {
		super(fluidName, fluidAmount, reactantName, Reactants.standardFluidReactantAmount);
	}
	
	public FluidToReactantMapping(Fluid fluid, int fluidAmount, String reactantName) {
		super(fluid.getName(), fluidAmount, reactantName, Reactants.standardFluidReactantAmount);
	}
	
	public FluidToReactantMapping(FluidStack fluidStack, String reactantName) {
		super(fluidStack.getFluid().getName(), fluidStack.amount, reactantName, Reactants.standardFluidReactantAmount);
	}
	
	public FluidToReactantMapping(String fluidName, String reactantName) {
		super(fluidName, Reactants.standardFluidReactantAmount, reactantName, Reactants.standardFluidReactantAmount);
	}
	
	public FluidToReactantMapping(Fluid fluid, String reactantName) {
		super(fluid.getName(), Reactants.standardFluidReactantAmount, reactantName, Reactants.standardFluidReactantAmount);
	}
	
}
