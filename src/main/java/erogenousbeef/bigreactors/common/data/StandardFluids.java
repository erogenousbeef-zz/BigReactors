package erogenousbeef.bigreactors.common.data;

import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import erogenousbeef.bigreactors.api.data.FluidStateData;
import erogenousbeef.bigreactors.api.registry.FluidStates;
import erogenousbeef.bigreactors.api.registry.SteamRegistry;
import erogenousbeef.bigreactors.common.BigReactors;

public class StandardFluids {

	private static boolean m_Registered = false;
	
	public static final int BoilingPoint_Water = 100;
	public static final int Rf_Water = 4;
	public static final int Expansion_Water = 1; // TODO: Change to 100 or something, so 1 unit water >> 100 units steam
	
	public static void register() {
		if(m_Registered) { return; }

		FluidStates.registerData(new FluidStateData("water",
			FluidRegistry.getFluid("water"),
			BigReactors.fluidSteam,
			BoilingPoint_Water,	
			Rf_Water,
			Expansion_Water		
		));
		
		SteamRegistry.setDefault(BigReactors.fluidSteam);

		m_Registered = true;
	}
}
