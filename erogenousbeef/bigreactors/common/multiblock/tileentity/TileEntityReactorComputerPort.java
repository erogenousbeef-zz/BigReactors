package erogenousbeef.bigreactors.common.multiblock.tileentity;

import cpw.mods.fml.common.Optional;
import dan200.computer.api.IComputerAccess;
import dan200.computer.api.ILuaContext;
import dan200.computer.api.IPeripheral;
import erogenousbeef.bigreactors.common.multiblock.MultiblockReactor;
import erogenousbeef.core.common.CoordTriplet;
import li.cil.oc.api.network.Arguments;
import li.cil.oc.api.network.Context;
import li.cil.oc.api.network.ManagedPeripheral;
import li.cil.oc.api.network.SimpleComponent;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fluids.Fluid;

@Optional.InterfaceList({
		@Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "OpenComputers"),
		@Optional.Interface(iface = "li.cil.oc.api.network.ManagedPeripheral", modid = "OpenComputers"),
		@Optional.Interface(iface = "dan200.computer.api.IPeripheral", modid = "ComputerCraft")
})
public class TileEntityReactorComputerPort extends TileEntityReactorPart implements IPeripheral, SimpleComponent, ManagedPeripheral {

	public enum ComputerMethod {
		getConnected,			// No arguments
		getActive,				// No arguments
		getFuelTemperature,		// No arguments
		getCasingTemperature,	// No arguments
		getEnergyStored, 		// No arguments
		getFuelAmount,  		// No arguments
		getWasteAmount, 		// No arguments
		getFuelAmountMax,		// No arguments
		getControlRodName,		// Required Arg: fuel rod index
		getNumberOfControlRods,	// No arguments
		getControlRodLevel, 	// Required Arg: control rod index
		getEnergyProducedLastTick, // No arguments
		getCoolantAmount,		// No arguments
		getCoolantType,			// No arguments
		getHotFluidAmount,		// No arguments
		getHotFluidType,		// No arguments
		getFuelReactivity,		// No arguments
		getFuelConsumedLastTick,// No arguments
		isActivelyCooled,		// No arguments
		setActive,				// Required Arg: integer (active)
		setControlRodLevel,		// Required Args: fuel rod index, integer (insertion)
		setAllControlRodLevels,	// Required Arg: integer (insertion)
		doEjectWaste			// No arguments
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
			throw new Exception("Unable to access reactor - port is not connected");
		}
		MultiblockReactor reactor = this.getReactorController();

		ComputerMethod computerMethod = ComputerMethod.values()[method];
		int index, newLevel;
		boolean newState;
		TileEntityReactorControlRod controlRod;

		switch(computerMethod) {
		case getEnergyStored:
			return new Object[] { (int)reactor.getEnergyStored() };
		case getNumberOfControlRods:
			return new Object[] { (int)reactor.getFuelRodCount() };
		case getActive:
			return new Object[] { reactor.isActive() };
		case getFuelTemperature:
			return new Object[] { reactor.getFuelHeat() };
		case getCasingTemperature:
			return new Object[] { reactor.getReactorHeat() };
		case getFuelAmount:
			return new Object[] { (int)reactor.getFuelAmount() };
		case getWasteAmount:
			return new Object[] { (int)reactor.getWasteAmount() };
		case getFuelAmountMax:
			return new Object[] { reactor.getCapacity() };
		case getControlRodName:
			if(arguments.length < 1) {
				throw new IllegalArgumentException("Insufficient number of arguments, expected 1");
			}
			
			controlRod = getControlRodFromArguments(reactor, arguments, 0);
			return new Object[] { controlRod.getName() };

		case getControlRodLevel:
			if(arguments.length < 1) {
				throw new IllegalArgumentException("Insufficient number of arguments, expected 1");
			}
			
			controlRod = getControlRodFromArguments(reactor, arguments, 0);
			return new Object[] { (int)controlRod.getControlRodInsertion() };

		case getEnergyProducedLastTick:
			return new Object[] { reactor.getEnergyGeneratedLastTick() };
		
		case isActivelyCooled:
			return new Object[] { !reactor.isPassivelyCooled() };

		case getCoolantAmount:
			return new Object[] { reactor.getCoolantContainer().getCoolantAmount() };
			
		case getCoolantType: {
			Fluid fluidType = reactor.getCoolantContainer().getCoolantType();
			if(fluidType == null) {
				return null;
			}
			else {
				return new Object[] { fluidType.getName() };
			}
		}

		case getHotFluidAmount:
			return new Object[] { reactor.getCoolantContainer().getVaporAmount() };
			
		case getHotFluidType: {
			Fluid fluidType = reactor.getCoolantContainer().getVaporType();
			if(fluidType == null) {
				return null;
			}
			else {
				return new Object[] { fluidType.getName() };
			}
		}
		
		case getFuelReactivity:
			return new Object[] { reactor.getFuelFertility() * 100f };

		case getFuelConsumedLastTick:
			return new Object[] { reactor.getFuelConsumedLastTick() };
			
		case setActive:
			if(arguments.length < 1) {
				throw new IllegalArgumentException("Insufficient number of arguments, expected 1");
			}
			if(!(arguments[0] instanceof Boolean)) {
				throw new IllegalArgumentException("Invalid argument 0, expected Boolean");
			}
			newState = (Boolean)arguments[0];
			reactor.setActive(newState);
			return null;

		case setAllControlRodLevels:
			if(arguments.length < 1) {
				throw new IllegalArgumentException("Insufficient number of arguments, expected 1");
			}
			if(!(arguments[0] instanceof Double)) {
				throw new IllegalArgumentException("Invalid argument 0, expected Number");
			}
			newLevel = (int)Math.round((Double)arguments[0]);
			reactor.setAllControlRodInsertionValues(newLevel);
			return null;

		case setControlRodLevel:
			if(arguments.length < 2) {
				throw new IllegalArgumentException("Insufficient number of arguments, expected 2 (control rod index, level)");
			}

			if(!(arguments[1] instanceof Double)) {
				throw new IllegalArgumentException("Invalid argument 0, expected Number");
			}

			newLevel = (int)Math.round((Double)arguments[1]);
			if(newLevel < 0 || newLevel > 100) {
				throw new IllegalArgumentException("Invalid argument 1, valid range is 0-100");
			}

			controlRod = getControlRodFromArguments(reactor, arguments, 0);
			controlRod.setControlRodInsertion((short) newLevel);
			
			return null;
		// "do" methods - void return, no inputs
		case doEjectWaste:
			reactor.ejectWaste(false);
			return null;
		default: throw new Exception("Method unimplemented - yell at Beef");
		}
	}
	
	private TileEntityReactorControlRod getControlRodFromArguments(MultiblockReactor reactor, Object[] arguments, int index) throws Exception {
		if(!(arguments[index] instanceof Double)) {
			throw new IllegalArgumentException(String.format("Invalid argument %d, expected Number", index));
		}
		
		int rodIndex = (int)Math.round((Double)arguments[index]);
		
		if(index < 0 || index >= reactor.getFuelRodCount()) {
			throw new IndexOutOfBoundsException(String.format("Invalid argument %d, control rod index is out of bounds", index));
		}
		
		CoordTriplet coord = reactor.getControlRodLocations()[rodIndex];
		
		TileEntity te = worldObj.getBlockTileEntity(coord.x, coord.y, coord.z);
		if(!(te instanceof TileEntityReactorControlRod)) {
			throw new Exception("Encountered an invalid tile entity when seeking a control rod. That's weird.");
		}
		
		return (TileEntityReactorControlRod)te;
	}
	
	// ComputerCraft
	
	@Override
	@Optional.Method(modid = "ComputerCraft")
	public String getType() {
		return "BigReactors-Reactor";
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
		return "br_reactor";
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
