package erogenousbeef.bigreactors.common.data;

import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import erogenousbeef.bigreactors.api.data.FluidStateData;
import erogenousbeef.bigreactors.api.registry.FluidStates;
import erogenousbeef.bigreactors.common.BigReactors;

public class StandardFluids {

	private static boolean m_Registered = false;
	
	public static void register() {
		if(m_Registered) { return; }

		FluidStates.registerData(new FluidStateData("water",
			FluidRegistry.getFluid("water"),
			BigReactors.fluidSteam,
			100,// 100C boiling point for water
			4,	// 1mB water<>steam = 4 rf
			1	// TODO: Change to 100 or something, so 1 unit water >> 100 units steam
		));

		m_Registered = true;
	}
	
}
