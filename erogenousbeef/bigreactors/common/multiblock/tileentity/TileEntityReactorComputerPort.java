package erogenousbeef.bigreactors.common.multiblock.tileentity;

import net.minecraft.tileentity.TileEntity;
import dan200.computer.api.IComputerAccess;
import dan200.computer.api.ILuaContext;
import dan200.computer.api.IPeripheral;
import erogenousbeef.bigreactors.common.multiblock.MultiblockReactor;
import erogenousbeef.core.common.CoordTriplet;

public class TileEntityReactorComputerPort extends TileEntityReactorPart implements IPeripheral {

	public enum ComputerMethod {
		getConnected,			// No arguments
		getActive,				// No arguments
		getTemperature,			// Optional Arg: fuel rod index
		getEnergyStored, 		// No arguments
		getFuelAmount,  		// Optional Arg: fuel rod index
		getWasteAmount, 		// Optional Arg: fuel rod index
		getFuelAmountMax,		// No arguments
		getControlRodName,		// Required Arg: fuel rod index
		getNumberOfControlRods,	// No arguments
		getControlRodLevel, 	// Required Arg: control rod index
		getEnergyProducedLastTick, // No arguments
		setActive,				// Required Arg: integer (active)
		setControlRodLevel,		// Required Args: fuel rod index, integer (insertion)
		setAllControlRodLevels,	// Required Arg: integer (insertion)
		doEjectWaste			// No arguments
	}
	
	public static final int numMethods = ComputerMethod.values().length; 
	
	@Override
	public String getType() {
		return "BigReactors-Reactor";
	}

	@Override
	public String[] getMethodNames() {
		ComputerMethod[] methods = ComputerMethod.values();
		String[] methodNames = new String[methods.length];
		for(ComputerMethod method : methods) {
			methodNames[method.ordinal()] = method.toString();
		}

		return methodNames;
	}

	@Override
	public Object[] callMethod(IComputerAccess computer, ILuaContext context,
			int method, Object[] arguments) throws Exception {
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
		case getTemperature:
			if(arguments.length <= 0) {
				return new Object[] { (int)reactor.getReactorHeat() };
			}
			else {
				controlRod = getControlRodFromArguments(reactor, arguments, 0);
				return new Object[] { 0 }; // TODO FIXME (int)controlRod.getHeat() };
			}
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

	@Override
	public boolean canAttachToSide(int side) {
		if(side < 2 || side > 5) { return false; }
		return true;
	}

	@Override
	public void attach(IComputerAccess computer) {
	}

	@Override
	public void detach(IComputerAccess computer) {
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
}
