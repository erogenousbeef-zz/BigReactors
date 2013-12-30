package erogenousbeef.bigreactors.common.tileentity;

import java.io.DataInputStream;
import java.io.IOException;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import erogenousbeef.bigreactors.client.gui.GuiDebugTurbine;
import erogenousbeef.bigreactors.client.gui.GuiHeatGenerator;
import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.common.tileentity.base.TileEntityPoweredInventoryFluid;
import erogenousbeef.bigreactors.gui.container.ContainerSlotless;
import erogenousbeef.bigreactors.gui.container.ISlotlessUpdater;
import erogenousbeef.bigreactors.utils.StaticUtils;

public class TileEntityDebugTurbine extends TileEntityPoweredInventoryFluid implements ISlotlessUpdater {

	public static final int TANK_WATER = 0;
	public static final int TANK_STEAM = 1;
	
	public enum VentStatus {
		DoNotVent,
		VentOverflow,
		VentAll,
	};
	
	protected boolean isActive;
	protected VentStatus ventStatus;
	
	protected float rotationalVelocity; // in arbitrary units, really. We'll call them RPMs for verisimilitude
	protected float energyStored;
	protected float energyGeneratedLastTick;

	public TileEntityDebugTurbine() {
		super();
		
		isActive = false;
		ventStatus = VentStatus.DoNotVent;
		rotationalVelocity = 0f;
		energyStored = 0f;
		energyGeneratedLastTick = 0f;
	}
	
	@Override
	public void updateEntity() {
		super.updateEntity();

		// Amount of input fluid to convert. Based on amount in input tank. Size of input tank caps speed.
		int steamIn = 0; // mB. Based on water, actually. Probably higher for steam. Measure it.
		
		// Input fluid constants. Derive from input fluid.
		float fluidEnergyDensity = 0.001f; // effectively, force-units per mB. (one mB-force or mBf). Stand-in for fluid density.

		// Blade constants. Calculate the area on assembly. Lift coefficient should be constant.
		// Better blades should be lighter and have less drag.
		// Penalize suboptimal shapes with worse drag (i.e. increased drag without increasing lift)
		// Suboptimal is defined as "not a christmas-tree shape". At worst, drag is increased 4x.
		float bladeLiftCoefficient = 0.75f; // From wikipedia, lift of a standard airfoil 
		float bladeDragCoefficient = 0.04f; // From wikipedia, drag of a standard airfoil
		int bladeSurfaceArea = 4; // in blocks. 4x blade blocks

		// Inductor constants
		float inductionEnergyCoefficient = 500f; // RF per energy-unit converted. A constant, multiplied by the energy density of the fluid.
		
		// Inductor dynamic constants - get from a table on assembly
		float inductorDragCoefficient = 0.01f; // Keep this small, as it gets multiplied by v^2 and dominates at high speeds. Higher = more drag from the inductor vs. aerodynamic drag = more efficient energy conversion.
		int inductorVolume = 8; // in blocks, assumed to be in a ring around the rotor. 8xblocks
		float inductionEnergyBonus = 1f; // Bonus to energy generation based on construction materials. 1 = plain iron.

		// Rotor constants - calculate on assembly
		float rotorMass = 60f; // in deci-blocks-of-iron (1 block of iron = 10 units)
		float rotorDragCoefficient = 0.1f; // totally arbitrary. Allow upgrades to decrease this.
		
		if(isActive) {
			// Spin up via steam inputs, convert some steam back into water
			steamIn = 120;
		}
		
		// No-op if we have no work to do
		if(steamIn <= 0 && rotationalVelocity <= 0) { return; }

		// Induction-driven torque pulled out of my ass.
		float inductionTorque = (float)(Math.pow(rotationalVelocity*0.1f, 1.5)*inductorDragCoefficient*inductorVolume);

		// Aerodynamic drag equation. Thanks, Mr. Euler.
		// Floored at 0.001f so the damn thing spins down in a reasonable period of time.
		float aerodynamicDragTorque = Math.max(0.001f, (float)Math.pow(rotationalVelocity, 2) * fluidEnergyDensity * bladeDragCoefficient * bladeSurfaceArea / 2f);

		// Frictional drag equation. Basically, a small amount of constant drag based on the size of your rotor.
		float frictionalDragTorque = rotorDragCoefficient * rotorMass;
		
		// Aerodynamic lift equation. Also via Herr Euler.
		// Steam is an integer, so this 
		float liftTorque = 2 * (float)Math.pow(steamIn, 2) * fluidEnergyDensity * bladeLiftCoefficient * bladeSurfaceArea;

		// Yay for derivation. We're assuming delta-Time is always 1, as we're always calculating for 1 tick.
		// TODO: When calculating rotor mass, factor in a division by two to eliminate the constant term.
		float deltaV = (2 * (liftTorque + -1f*inductionTorque + -1f*aerodynamicDragTorque + -1f*frictionalDragTorque)) / rotorMass;

		energyGeneratedLastTick = inductionTorque * inductionEnergyCoefficient * inductionEnergyBonus * fluidEnergyDensity;
		energyStored += energyGeneratedLastTick;

		if(energyStored >= 1f) {
			// Whenever we accumulate more than 1RF, put it into the energy buffer
			receiveEnergy(ForgeDirection.UNKNOWN, (int)energyStored, false);
			energyStored -= Math.floor(energyStored);
		}
		
		rotationalVelocity += deltaV;
		if(rotationalVelocity < 0f) { rotationalVelocity = 0f; }
	}

	public float getTurbineSpeed() {
		return rotationalVelocity;
	}
	
	public float getEnergyGeneratedLastTick() {
		return energyGeneratedLastTick;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public GuiScreen getGUI(EntityPlayer player) {
		return new GuiDebugTurbine(getContainer(player), this);
	}

	@Override
	public Container getContainer(EntityPlayer player) {
		return new ContainerSlotless(this, player);
	}
	
	@Override
	public void onReceiveGuiButtonPress(String buttonName, DataInputStream dataStream) throws IOException {
		super.onReceiveGuiButtonPress(buttonName, dataStream);
		
		if(buttonName.equals("active")) {
			isActive = !isActive;
			worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
			return;
		}
		
		if(buttonName.equals("ventNone")) {
			ventStatus = VentStatus.DoNotVent;
			worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
			return;
		}
		
		if(buttonName.equals("ventOverflow")) {
			ventStatus = VentStatus.VentOverflow;
			worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
			return;
		}

		if(buttonName.equals("ventAll")) {
			ventStatus = VentStatus.VentAll;
			worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
			return;
		}
	}

	@Override
	protected void onSendUpdate(NBTTagCompound updateTag) {
		super.onSendUpdate(updateTag);
		
		updateTag.setBoolean("active", isActive);
		updateTag.setInteger("vent", ventStatus.ordinal());
		updateTag.setFloat("vRot", rotationalVelocity);
		updateTag.setFloat("energy", energyStored);
		updateTag.setFloat("energyGenerated", energyGeneratedLastTick);
	}
	
	@Override
	public void onReceiveUpdate(NBTTagCompound updateTag) {
		super.onReceiveUpdate(updateTag);
		
		isActive = updateTag.getBoolean("active");
		ventStatus = VentStatus.values()[updateTag.getInteger("vent")];
		rotationalVelocity = updateTag.getFloat("vRot");
		energyStored = updateTag.getFloat("energy");
		energyGeneratedLastTick = updateTag.getFloat("energyGenerated");
	}

	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
		
		if(tag.hasKey("active")) {
			isActive = tag.getBoolean("active");
		}
		else {
			isActive = false;
		}
		
		if(tag.hasKey("vent")) {
			ventStatus = VentStatus.values()[tag.getInteger("vent")];
		}
		else {
			ventStatus = VentStatus.DoNotVent;
		}
		
		if(tag.hasKey("vRot")) {
			rotationalVelocity = tag.getFloat("vRot");
		}
		else {
			rotationalVelocity = 0f;
		}

		if(tag.hasKey("energy")) {
			energyStored = tag.getFloat("energy");
		}
		else {
			energyStored = 0f;
		}
	}
	
	@Override
	public void writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);
		
		tag.setBoolean("active", isActive);
		tag.setInteger("vent", ventStatus.ordinal());
		tag.setFloat("vRot", rotationalVelocity);
		tag.setFloat("energy", energyStored);
	}
	
	// Fluids
	@Override
	public int getNumTanks() {
		return 2;
	}

	@Override
	public int getTankSize(int tankIndex) {
		return 1000;
	}

	@Override
	protected boolean isFluidValidForTank(int tankIdx, FluidStack type) {
		if(tankIdx == TANK_WATER && type.getFluid().getID() == FluidRegistry.WATER.getID())
			return true;
		else if(tankIdx == TANK_STEAM && type.getFluid().getID() == BigReactors.fluidSteam.getID())
			return true;
		else
			return false;
	}

	@Override
	protected int getDefaultTankForFluid(Fluid fluid) {
		if(fluid.getID() == FluidRegistry.WATER.getID())
			return TANK_WATER;
		else if(fluid.getID() == BigReactors.fluidSteam.getID())
			return TANK_STEAM;
		else
			return FLUIDTANK_NONE;
	}

	// Powered
	@Override
	protected int getMaxEnergyStored() {
		return 10000;
	}

	@Override
	public int getCycleEnergyCost() {
		return 20000;
	}

	@Override
	public int getCycleLength() {
		return 1;
	}

	@Override
	public boolean canBeginCycle() {
		return false;
	}

	@Override
	public void onPoweredCycleBegin() {
	}

	@Override
	public void onPoweredCycleEnd() {
	}

	// Inventory
	@Override
	public int getSizeInventory() {
		return 0;
	}

	@Override
	public String getInvName() {
		return "Debug Turbine";
	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack itemstack) {
		return false;
	}

}
