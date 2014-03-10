package erogenousbeef.bigreactors.common.multiblock.tileentity;

import cpw.mods.fml.common.Optional.Interface;
import cpw.mods.fml.common.Optional.InterfaceList;
import cpw.mods.fml.common.Optional.Method;
import cpw.mods.fml.common.registry.GameRegistry;
import li.cil.oc.api.driver.Block;
import li.cil.oc.api.network.Arguments;
import li.cil.oc.api.network.Callback;
import li.cil.oc.api.network.Context;
import li.cil.oc.api.network.Environment;
import li.cil.oc.api.network.ManagedEnvironment;
import li.cil.oc.api.network.SimpleComponent;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import dan200.computer.api.IComputerAccess;
import dan200.computer.api.ILuaContext;
import dan200.computer.api.IPeripheral;
import erogenousbeef.bigreactors.common.multiblock.MultiblockReactor;
import erogenousbeef.core.common.CoordTriplet;

@InterfaceList({ @Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "OpenComputers"), @Interface(iface = "dan200.computer.api.IPeripheral", modid = "ComputerCraft") })
public class TileEntityReactorComputerPort extends TileEntityReactorPart implements IPeripheral, SimpleComponent {


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
	
	@Override
	@Method(modid = "ComputerCraft")
	public String getType() {
		return "BigReactors-Reactor";
	}

	@Override
	@Method(modid = "ComputerCraft")
	public String[] getMethodNames() {
		ComputerMethod[] methods = ComputerMethod.values();
		String[] methodNames = new String[methods.length];
		for(ComputerMethod method : methods) {
			methodNames[method.ordinal()] = method.toString();
		}

		return methodNames;
	}

	@Override
	@Method(modid = "ComputerCraft")
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

	@Override
	@Method(modid = "ComputerCraft")
	public boolean canAttachToSide(int side) {
		if(side < 2 || side > 5) { return false; }
		return true;
	}

	@Override
	@Method(modid = "ComputerCraft")
	public void attach(IComputerAccess computer) {
	}

	@Override
	@Method(modid = "ComputerCraft")
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
	
	private TileEntityReactorControlRod getControlRodFromArguments(MultiblockReactor reactor, Arguments args, int index) throws Exception {
		if(!(args.isDouble(index))) {
			throw new IllegalArgumentException(String.format("Invalid argument %d, expected Number", index));
		}
		
		int rodIndex = (int)Math.round((Double)args.checkAny(index));
		
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
	
	@Callback
    @Method(modid = "OpenComputers")
    public Object[] greet(Context context, Arguments args) {
        return new Object[]{String.format("Hello, %s! I am OpenComputers/BigReactors!", args.checkString(0))};
    }
	
	@Callback
    @Method(modid = "OpenComputers")
    public Object[] getEnergyStored(Context context, Arguments args) throws Exception {
		if(!this.isConnected()) {
			throw new Exception("Unable to access reactor - port is not connected");
		}
		MultiblockReactor reactor = this.getReactorController();
        return new Object[]{(int)reactor.getEnergyStored()};
    }
	
	@Callback
    @Method(modid = "OpenComputers")
    public Object[] getNumberOfControlRods(Context context, Arguments args) throws Exception {
		if(!this.isConnected()) {
			throw new Exception("Unable to access reactor - port is not connected");
		}
		MultiblockReactor reactor = this.getReactorController();
        return new Object[]{(int)reactor.getFuelRodCount()};
    }
	
	@Callback
    @Method(modid = "OpenComputers")
    public Object[] getActive(Context context, Arguments args) throws Exception {
		if(!this.isConnected()) {
			throw new Exception("Unable to access reactor - port is not connected");
		}
		MultiblockReactor reactor = this.getReactorController();
        return new Object[]{reactor.isActive()};
    }
	
	@Callback
    @Method(modid = "OpenComputers")
    public Object[] getFuelTemperature(Context context, Arguments args) throws Exception {
		if(!this.isConnected()) {
			throw new Exception("Unable to access reactor - port is not connected");
		}
		MultiblockReactor reactor = this.getReactorController();
        return new Object[]{reactor.getFuelHeat()};
    }
	
	@Callback
    @Method(modid = "OpenComputers")
    public Object[] getCasingTemperature(Context context, Arguments args) throws Exception {
		if(!this.isConnected()) {
			throw new Exception("Unable to access reactor - port is not connected");
		}
		MultiblockReactor reactor = this.getReactorController();
        return new Object[]{reactor.getReactorHeat()};
    }
	
	@Callback
    @Method(modid = "OpenComputers")
    public Object[] getFuelAmount(Context context, Arguments args) throws Exception {
		if(!this.isConnected()) {
			throw new Exception("Unable to access reactor - port is not connected");
		}
		MultiblockReactor reactor = this.getReactorController();
        return new Object[]{(int)reactor.getFuelAmount()};
    }
	
	@Callback
    @Method(modid = "OpenComputers")
    public Object[] getWasteAmount(Context context, Arguments args) throws Exception {
		if(!this.isConnected()) {
			throw new Exception("Unable to access reactor - port is not connected");
		}
		MultiblockReactor reactor = this.getReactorController();
        return new Object[]{(int)reactor.getWasteAmount()};
    }
	
	@Callback
    @Method(modid = "OpenComputers")
    public Object[] getFuelAmountMax(Context context, Arguments args) throws Exception {
		if(!this.isConnected()) {
			throw new Exception("Unable to access reactor - port is not connected");
		}
		MultiblockReactor reactor = this.getReactorController();
        return new Object[]{reactor.getCapacity()};
    }

	@Callback
    @Method(modid = "OpenComputers")
    public Object[] getControlRodName(Context context, Arguments args) throws Exception {
		TileEntityReactorControlRod controlRod;
		if(!this.isConnected()) {
			throw new Exception("Unable to access reactor - port is not connected");
		}
		if(args.count() < 1) {
			throw new IllegalArgumentException("Insufficient number of arguments, expected 1");
		}
		MultiblockReactor reactor = this.getReactorController();
		controlRod = getControlRodFromArguments(reactor, args, 0);
		return new Object[] { controlRod.getName() };
    }
	
	@Callback
    @Method(modid = "OpenComputers")
    public Object[] getControlRodLevel(Context context, Arguments args) throws Exception {
		TileEntityReactorControlRod controlRod;
		if(!this.isConnected()) {
			throw new Exception("Unable to access reactor - port is not connected");
		}
		if(args.count() < 1) {
			throw new IllegalArgumentException("Insufficient number of arguments, expected 1");
		}
		MultiblockReactor reactor = this.getReactorController();
		Object[] arguments[] = null;
		
		controlRod = getControlRodFromArguments(reactor, args, 0);
		return new Object[] { (int)controlRod.getControlRodInsertion() };
    }
	
	@Callback
    @Method(modid = "OpenComputers")
    public Object[] getEnergyProducedLastTick(Context context, Arguments args) throws Exception {
		if(!this.isConnected()) {
			throw new Exception("Unable to access reactor - port is not connected");
		}
		MultiblockReactor reactor = this.getReactorController();
        return new Object[]{reactor.getEnergyGeneratedLastTick()};
    }
	
	@Callback
    @Method(modid = "OpenComputers")
    public Object[] isActivelyCooled(Context context, Arguments args) throws Exception {
		if(!this.isConnected()) {
			throw new Exception("Unable to access reactor - port is not connected");
		}
		MultiblockReactor reactor = this.getReactorController();
        return new Object[]{!reactor.isPassivelyCooled()};
    }
	
	@Callback
    @Method(modid = "OpenComputers")
    public Object[] getCoolantAmount(Context context, Arguments args) throws Exception {
		if(!this.isConnected()) {
			throw new Exception("Unable to access reactor - port is not connected");
		}
		MultiblockReactor reactor = this.getReactorController();
        return new Object[]{reactor.getCoolantContainer().getCoolantAmount()};
    }
	
	@Callback
    @Method(modid = "OpenComputers")
    public Object[] getCoolantType(Context context, Arguments args) throws Exception {
		if(!this.isConnected()) {
			throw new Exception("Unable to access reactor - port is not connected");
		}
		MultiblockReactor reactor = this.getReactorController();
		Fluid fluidType = reactor.getCoolantContainer().getCoolantType();
		if(fluidType == null) {
			return null;
		}
		else {
			return new Object[] { fluidType.getName() };
		}
    }
	
	@Callback
    @Method(modid = "OpenComputers")
    public Object[] getHotFluidAmount(Context context, Arguments args) throws Exception {
		if(!this.isConnected()) {
			throw new Exception("Unable to access reactor - port is not connected");
		}
		MultiblockReactor reactor = this.getReactorController();
        return new Object[]{reactor.getCoolantContainer().getVaporAmount()};
    }
	
	@Callback
    @Method(modid = "OpenComputers")
    public Object[] getHotFluidType(Context context, Arguments args) throws Exception {
		if(!this.isConnected()) {
			throw new Exception("Unable to access reactor - port is not connected");
		}
		MultiblockReactor reactor = this.getReactorController();
		Fluid fluidType = reactor.getCoolantContainer().getVaporType();
		if(fluidType == null) {
			return null;
		}
		else {
			return new Object[] { fluidType.getName() };
		}
    }
	
	@Callback
    @Method(modid = "OpenComputers")
    public Object[] getFuelReactivity(Context context, Arguments args) throws Exception {
		if(!this.isConnected()) {
			throw new Exception("Unable to access reactor - port is not connected");
		}
		MultiblockReactor reactor = this.getReactorController();
        return new Object[]{ reactor.getFuelFertility() * 100f };
    }
	
	@Callback
    @Method(modid = "OpenComputers")
    public Object[] getFuelConsumedLastTick(Context context, Arguments args) throws Exception {
		if(!this.isConnected()) {
			throw new Exception("Unable to access reactor - port is not connected");
		}
		MultiblockReactor reactor = this.getReactorController();
        return new Object[]{reactor.getFuelConsumedLastTick()};
    }
	
	@Callback
    @Method(modid = "OpenComputers")
    public Object[] setActive(Context context, Arguments args) throws Exception {
		int index, newLevel;
		boolean newState;
		if(!this.isConnected()) {
			throw new Exception("Unable to access reactor - port is not connected");
		}
		MultiblockReactor reactor = this.getReactorController();
		if(args.count() < 1) {
			throw new IllegalArgumentException("Insufficient number of arguments, expected 1");
		}
		if(!(args.checkAny(0) instanceof Boolean)) {
			throw new IllegalArgumentException("Invalid argument 0, expected Boolean");
		}
		newState = (Boolean)args.checkAny(0);
		reactor.setActive(newState);
		return null;
    }
	
	@Callback
    @Method(modid = "OpenComputers")
    public Object[] setAllControlRodLevels(Context context, Arguments args) throws Exception {
		int index, newLevel;
		boolean newState;
		if(!this.isConnected()) {
			throw new Exception("Unable to access reactor - port is not connected");
		}
		MultiblockReactor reactor = this.getReactorController();
		if(args.count() < 1) {
			throw new IllegalArgumentException("Insufficient number of arguments, expected 1");
		}
		if(!(args.checkAny(0) instanceof Double)) {
			throw new IllegalArgumentException("Invalid argument 0, expected Number");
		}
		newLevel = (int)Math.round((Double)args.checkAny(0));
		reactor.setAllControlRodInsertionValues(newLevel);
		return null;
    }
	
	@Callback
    @Method(modid = "OpenComputers")
    public Object[] setControlRodLevel(Context context, Arguments args) throws Exception {
		int index, newLevel;
		boolean newState;
		TileEntityReactorControlRod controlRod;
		if(!this.isConnected()) {
			throw new Exception("Unable to access reactor - port is not connected");
		}
		MultiblockReactor reactor = this.getReactorController();
		controlRod = getControlRodFromArguments(reactor, args, 0);
		if(args.count() < 2) {
			throw new IllegalArgumentException("Insufficient number of arguments, expected 2 (control rod index, level)");
		}

		if(!(args.checkAny(1) instanceof Double)) {
			throw new IllegalArgumentException("Invalid argument 0, expected Number");
		}

		newLevel = (int)Math.round((Double)args.checkDouble(1));
		if(newLevel < 0 || newLevel > 100) {
			throw new IllegalArgumentException("Invalid argument 1, valid range is 0-100");
		}

		controlRod = getControlRodFromArguments(reactor, args, 0);
		controlRod.setControlRodInsertion((short) newLevel);
		return null;
    }
	
	@Callback
    @Method(modid = "OpenComputers")
    public Object[] doEjectWaste(Context context, Arguments args) throws Exception {
		if(!this.isConnected()) {
			throw new Exception("Unable to access reactor - port is not connected");
		}
		MultiblockReactor reactor = this.getReactorController();
		reactor.ejectWaste(false);
		return null;
    }
	
	@Override
	public String getComponentName() {
		return "br_reactor";
	}
}
