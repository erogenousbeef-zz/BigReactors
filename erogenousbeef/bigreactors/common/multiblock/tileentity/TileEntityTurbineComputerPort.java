package erogenousbeef.bigreactors.common.multiblock.tileentity;

import cpw.mods.fml.common.Optional;
import dan200.computer.api.IComputerAccess;
import dan200.computer.api.ILuaContext;
import dan200.computer.api.IPeripheral;
import erogenousbeef.bigreactors.common.multiblock.MultiblockTurbine;
import li.cil.oc.api.network.Arguments;
import li.cil.oc.api.network.Context;
import li.cil.oc.api.network.ManagedPeripheral;
import li.cil.oc.api.network.SimpleComponent;
import net.minecraftforge.fluids.FluidTankInfo;

@Optional.InterfaceList({
		@Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "OpenComputers"),
		@Optional.Interface(iface = "li.cil.oc.api.network.ManagedPeripheral", modid = "OpenComputers"),
		@Optional.Interface(iface = "dan200.computer.api.IPeripheral", modid = "ComputerCraft")
})
public class TileEntityTurbineComputerPort extends
		TileEntityTurbinePartStandard implements IPeripheral, SimpleComponent, ManagedPeripheral {

	public enum ComputerMethod {
		getConnected,			// No arguments
		getActive,				// No arguments
		getEnergyStored, 		// No arguments
		getRotorSpeed,			// No arguments
		getInputAmount,  		// No arguments
		getInputType,			// No arguments
		getOutputAmount, 		// No arguments
		getOutputType,			// No arguments
		getFluidAmountMax,		// No arguments
		getFluidFlowRate,		// No arguments
		getFluidFlowRateMax,	// No arguments
		getFluidFlowRateMaxMax, // No arguments
		getEnergyProducedLastTick, // No arguments
		getNumberOfBlades,		// No arguments
		getBladeEfficiency,		// No arguments
		getRotorMass,			// No arguments
		setActive,				// Required Arg: integer (active)
		setFluidFlowRateMax,	// Required Arg: integer (active)
	}
	
	public static final int numMethods = ComputerMethod.values().length;
	
	public Object[] callMethod(int method, Object[] arguments) throws Exception {
		if(method < 0 || method >= numMethods) {
			throw new IllegalArgumentException("Invalid method number");
		}
		
		// Special case: getConnected can always be called.
		if(method == 0) {
			return new Object[] { this.isConnected() };
		}

		if(!this.isConnected()) {
			throw new Exception("Unable to access turbine - port is not connected");
		}

		ComputerMethod computerMethod = ComputerMethod.values()[method];
		MultiblockTurbine turbine = getTurbine();
		FluidTankInfo ti;

		switch(computerMethod) {
		case getConnected:
			return new Object[] { isConnected() };
		case getActive:
			return new Object[] { turbine.isActive() };
		case getEnergyProducedLastTick:
			return new Object[] { turbine.getEnergyGeneratedLastTick() };
		case getEnergyStored:
			return new Object[] { turbine.getEnergyStored() };
		case getFluidAmountMax:
			return new Object[] { MultiblockTurbine.TANK_SIZE };
		case getFluidFlowRate:
			return new Object[] { turbine.getFluidConsumedLastTick() };
		case getFluidFlowRateMax:
			return new Object[] { turbine.getMaxIntakeRate() };
		case getFluidFlowRateMaxMax:
			return new Object[] { turbine.getMaxIntakeRateMax() };
		case getInputAmount:
			ti = turbine.getTankInfo(MultiblockTurbine.TANK_INPUT);
			if(ti != null && ti.fluid != null) {
				return new Object[] { ti.fluid.amount };
			}
			else {
				return new Object[] { 0f };
			}
		case getInputType:
			ti = turbine.getTankInfo(MultiblockTurbine.TANK_INPUT);
			if(ti != null && ti.fluid != null) {
				return new Object[] { ti.fluid.getFluid().getName() };
			}
			else {
				return null;
			}
		case getOutputAmount:
			ti = turbine.getTankInfo(MultiblockTurbine.TANK_OUTPUT);
			if(ti != null && ti.fluid != null) {
				return new Object[] { ti.fluid.amount };
			}
			else {
				return new Object[] { 0f };
			}
		case getOutputType:
			ti = turbine.getTankInfo(MultiblockTurbine.TANK_OUTPUT);
			if(ti != null && ti.fluid != null) {
				return new Object[] { ti.fluid.getFluid().getName() };
			}
			else {
				return null;
			}
		case getRotorSpeed:
			return new Object[] { turbine.getRotorSpeed() };
		case getNumberOfBlades:
			return new Object[] { turbine.getNumRotorBlades() };
		case getBladeEfficiency:
			return new Object[] { turbine.getRotorEfficiencyLastTick() * 100f };
		case getRotorMass:
			return new Object[] { turbine.getRotorMass() };
		case setActive:
			if(arguments.length < 1) {
				throw new IllegalArgumentException("Insufficient number of arguments, expected 1");
			}
			if(!(arguments[0] instanceof Boolean)) {
				throw new IllegalArgumentException("Invalid argument 0, expected Boolean");
			}
			turbine.setActive((Boolean)arguments[0]);
			break;
		case setFluidFlowRateMax:
			if(arguments.length < 1) {
				throw new IllegalArgumentException("Insufficient number of arguments, expected 1");
			}
			if(!(arguments[0] instanceof Double)) {
				throw new IllegalArgumentException("Invalid argument 0, expected Number");
			}
			int newRate = (int)Math.round((Double)arguments[0]);
			turbine.setMaxIntakeRate(newRate);
			break;
		default:
			throw new Exception("Method unimplemented - yell at Beef");
		}
		
		return null;
	}
	
	// ComputerCraft
	
	@Override
	@Optional.Method(modid = "ComputerCraft")
	public String getType() {
		return "BigReactors-Turbine";
	}
	
	@Override
	// Not @Optional, also used for OpenComputers.
	public String[] getMethodNames() {
		ComputerMethod[] methods = ComputerMethod.values();
		String[] methodNames = new String[methods.length];
		for(ComputerMethod method : methods) {
			methodNames[method.ordinal()] = method.toString();
		}
	
		return methodNames;
	}
	
	@Override
	@Optional.Method(modid = "ComputerCraft")
	public Object[] callMethod(IComputerAccess computer, ILuaContext context,
							   int method, Object[] arguments) throws Exception {
		return callMethod(method, arguments);
	}
	
	@Override
	@Optional.Method(modid = "ComputerCraft")
	public boolean canAttachToSide(int side) {
		if(side < 2 || side > 5) { return false; }
		return true;
	}
	
	@Override
	@Optional.Method(modid = "ComputerCraft")
	public void attach(IComputerAccess computer) {
	}
	
	@Override
	@Optional.Method(modid = "ComputerCraft")
	public void detach(IComputerAccess computer) {
	}
	
	// OpenComputers
	
	@Override
	@Optional.Method(modid = "OpenComputers")
	public String getComponentName() {
		return "br_turbine";
	}
	
	@Override
	@Optional.Method(modid = "OpenComputers")
	public String[] methods() {
		return getMethodNames();
	}
	
	@Override
	@Optional.Method(modid = "OpenComputers")
	public Object[] invoke(final String method, final Context context,
						   final Arguments args) throws Exception {
		final Object[] arguments = new Object[args.count()];
		for (int i = 0; i < args.count(); ++i) {
			arguments[i] = args.checkAny(i);
		}
		final int methodId = java.util.Arrays.asList(methods()).indexOf(method);
		if (methodId < 0) {
			throw new NoSuchMethodError();
		}
		return callMethod(methodId, arguments);
	}
}
