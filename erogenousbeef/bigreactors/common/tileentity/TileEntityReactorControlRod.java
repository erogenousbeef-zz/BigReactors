package erogenousbeef.bigreactors.common.tileentity;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import erogenousbeef.bigreactors.api.HeatPulse;
import erogenousbeef.bigreactors.api.IHeatEntity;
import erogenousbeef.bigreactors.api.IRadiationModerator;
import erogenousbeef.bigreactors.api.IRadiationPulse;
import erogenousbeef.bigreactors.api.IRadiationSource;
import erogenousbeef.bigreactors.api.IReactorFuel;
import erogenousbeef.bigreactors.client.gui.GuiReactorControlRod;
import erogenousbeef.bigreactors.common.BRRegistry;
import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.common.RadiationPulse;
import erogenousbeef.bigreactors.common.multiblock.MultiblockReactor;
import erogenousbeef.bigreactors.common.tileentity.base.TileEntityBeefBase;
import erogenousbeef.bigreactors.gui.IBeefGuiEntity;
import erogenousbeef.bigreactors.gui.container.ContainerReactorControlRod;
import erogenousbeef.bigreactors.net.PacketWrapper;
import erogenousbeef.bigreactors.net.Packets;
import erogenousbeef.core.multiblock.MultiblockControllerBase;
import erogenousbeef.core.multiblock.MultiblockTileEntityBase;
import net.minecraft.block.material.Material;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet132TileEntityData;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;

public class TileEntityReactorControlRod extends MultiblockTileEntityBase implements IRadiationSource, IRadiationModerator, IHeatEntity, IBeefGuiEntity {
	public final static int maxTotalFluidPerBlock = FluidContainerRegistry.BUCKET_VOLUME * 4;
	public final static int maxFuelRodsBelow = 32;
	public final static short maxInsertion = 100;
	public final static short minInsertion = 0;

	public final static int fuelTankIndex = 0;
	public final static int wasteTankIndex = 1;
	public final static int numTanks = 2;

	// Game Balance Values
	// TODO: Make these configurable
	private static final float maximumNeutronsPerFuel = 50000f; // Should be a few minutes per ingot, on average.
	private static final float neutronsPerFuel = 0.001f; // neutrons per fuel unit
	private static final float heatPerNeutron = 0.5f; // C per fission event
	private static final float powerPerNeutron = 0.5f; // internal units per fission event
	private static final float wasteNeutronPenalty = 0.01f;
	private static final float incidentNeutronFuelRate = 0.5f;

	// 1 ingot = 1 bucket = 1000 internal fuel
	public static final int fuelPerIngot = 1000;
	public static final int fuelPerBucket = 1000;
	
	protected boolean isAssembled = false;
	protected int minFuelRodY;

	// Fuel/Waste tracking
	protected FluidStack fuel;
	protected FluidStack waste;

	// Radiation
	protected float incidentRadiation; // Radiation received since last radiate() call
	protected short controlRodInsertion; // 0 = retracted fully, 100 = inserted fully
	
	// Heat
	protected float localHeat;
	
	// Fuel Consumption
	protected int neutronsSinceLastFuelConsumption;

	// Fuel messaging
	protected int fuelAtLastUpdate;
	protected int wasteAtLastUpdate;
	protected static final int maximumDevianceInContentsBetweenUpdates = 50; // about 20 seconds of operation
	
	// GUI messaging
	private Set<EntityPlayer> updatePlayers;
	private int ticksSinceLastUpdate;
	private static final int ticksBetweenUpdates = 3;
	private static final int INVALID_Y = Integer.MIN_VALUE;
	
	public TileEntityReactorControlRod() {
		super();
	
		isAssembled = false;

		fuel = null;
		waste = null;

		incidentRadiation = 0.0f;
		localHeat = 0.0f;
		neutronsSinceLastFuelConsumption = 0;
		minFuelRodY = INVALID_Y;
		wasteAtLastUpdate = 0;
		fuelAtLastUpdate = 0;
		controlRodInsertion = minInsertion;
		
		updatePlayers = new HashSet<EntityPlayer>();
	}
	
	// Data accessors

	public Fluid getFuelType() {
		if(fuel == null) { return null; }
		else {
			return fuel.getFluid();
		}
	}
	
	public Fluid getWasteType() {
		if(waste == null) {
			return null;
		} else {
			return waste.getFluid();
		}
	}
	
	public boolean isFull() {
		return waste.amount + fuel.amount >= getSizeOfFuelTank();
	}
	
	public boolean isEmpty() {
		return waste.amount + fuel.amount <= 0;
	}
	
	public int getSizeOfFuelTank() {
		if(this.minFuelRodY == INVALID_Y) { return 0; }
		else {
			return maxTotalFluidPerBlock * getColumnHeight();
		}
	}

	public int getFuelAmount() {
		if(this.fuel == null) { return 0; }
		return this.fuel.amount;
	}

	public int getWasteAmount() {
		if(this.waste == null) { return 0; }
		return this.waste.amount;
	}

	public int getTotalContainedAmount() {
		return this.getFuelAmount() + this.getWasteAmount();
	}

	public boolean isAssembled() {
		return isAssembled;
	}
	
	public short getControlRodInsertion() {
		return this.controlRodInsertion;
	}
	
	public void setControlRodInsertion(short newInsertion) {
		if(newInsertion > maxInsertion || newInsertion < minInsertion || newInsertion == controlRodInsertion) { return; }
		if(!this.isAssembled) { return; }

		this.controlRodInsertion = (short)Math.max(Math.min(newInsertion, maxInsertion), minInsertion);
		this.sendControlRodUpdate();
	}
	
	public int getColumnHeight() {
		if(minFuelRodY == INVALID_Y) { return 0; }
		return yCoord - minFuelRodY;
	}
	
	// Fuel Handling
	
	/**
	 * Attempt to add some fuel to the fuel rod.
	 * 
	 * @param incomingFuel A FluidStack containing the fuel to add.
	 * @param amount The amount of fuel to add, in internal units (1 ingot = 1000)
	 * @param doAdd If true, actually adds the amount to the internal store. If false, just calculates the amount to add and returns that.
	 * @return Returns the amount of fuel added to this rod (or that would have been added, if doAdd is false)
	 */
	public int addFuel(FluidStack incomingFuel, int amount, boolean doAdd) {
		if(incomingFuel == null) {
			return 0;
		}
		
		int amountToAdd = 0;
		boolean forceUpdate = false;
		if(this.fuel != null) {
			if(!this.fuel.isFluidEqual(incomingFuel)) {
				return 0;
			}
			
			amountToAdd = Math.min(amount, getSizeOfFuelTank() - (getWasteAmount() + getFuelAmount()));
			if(doAdd) {
				this.fuel.amount += amountToAdd;
			}
		}
		else {
			if(!this.isAcceptedFuel(incomingFuel.getFluid())) {
				return 0;
			}
			
			amountToAdd = Math.min(amount, getSizeOfFuelTank() - (getWasteAmount() + getFuelAmount()));
			
			if(amountToAdd <= 0) {
				return 0;
			}

			if(doAdd) {
				this.fuel = incomingFuel.copy();
				this.fuel.amount = amountToAdd;
				forceUpdate = true;
			}
		}
		
		if(amountToAdd > 0 && doAdd) {
			this.updateWorldIfNeeded(forceUpdate);
		}

		return amountToAdd;
	}

	public boolean isAcceptedFuel(Fluid fuelType) {
		return BRRegistry.getDataForFuel(fuelType) != null;
	}
	
	/**
	 * Attempt to remove fuel from this rod.
	 * @param fuelType The type of fuel to remove, or null for whatever's in there.
	 * @param amount The amount of fuel to remove.
	 * @param doRemove If true, actually removes the fuel. Otherwise, just calculates how much can be removed.
	 * @return The amount of fuel removed; only actually removed if doRemove is set to true.
	 */
	public int removeFuel(FluidStack outgoingFuel, int amount, boolean doRemove) {
		if(getFuelAmount() <= 0 || amount <= 0) {
			return 0;
		}
		
		if(outgoingFuel == null || this.fuel.isFluidEqual(outgoingFuel)) {
			int amtToRemove = Math.min(amount, getFuelAmount());
			if(doRemove) {
				fuel.amount -= amount;
				if(fuel.amount <= 0) {
					this.fuel = null;
				}
			}
			
			if(amtToRemove > 0 && doRemove) {
				this.updateWorldIfNeeded(this.fuel == null);
			}
			return amtToRemove;
		}

		return 0;
	}
	
	/**
	 * Attempt to add some fuel to the fuel rod.
	 * 
	 * @param wasteType An itemstack containing the type of fuel to add.
	 * @param amount The amount of fuel to add, in internal units (1 ingot = 1000)
	 * @param doAdd If true, actually adds the amount to the internal store. If false, just calculates the amount to add and returns that.
	 * @return Returns the amount of fuel added to this rod (or that would have been added, if doAdd is false)
	 */
	public int addWaste(FluidStack incomingWaste, int amount, boolean doAdd) {
		if(incomingWaste == null || amount <= 0) {
			return 0;
		}
		
		int amountToAdd = 0;
		boolean forceUpdate = false;
		if(this.waste != null) {
			if(!this.waste.isFluidEqual(incomingWaste)) {
				return 0;
			}
			
			amountToAdd = Math.min(amount, getSizeOfFuelTank() - (getWasteAmount()+getFuelAmount()));
			if(doAdd) {
				this.waste.amount += amountToAdd;
			}
		}
		else {
			if(!this.isAcceptedWaste(incomingWaste.getFluid())) {
				return 0;
			}
			
			amountToAdd = Math.min(amount, getSizeOfFuelTank() - (getWasteAmount()+getFuelAmount()));
			
			if(amountToAdd <= 0) {
				return 0;
			}

			if(doAdd) {
				this.waste = incomingWaste.copy();
				this.waste.amount = amountToAdd;
				forceUpdate = true;
			}
		}

		// Force this block's description packet to update the nearby world if there's a large variance in contents
		if(amountToAdd > 0 && doAdd) {
			this.updateWorldIfNeeded(forceUpdate);
		}

		return amountToAdd;
	}

	public boolean isAcceptedWaste(Fluid candidateWaste) {
		return BRRegistry.getDataForWaste(candidateWaste) != null;
	}
	
	/**
	 * Attempt to remove fuel from this rod.
	 * @param wasteType The type of fuel to remove, or null for whatever's in there.
	 * @param amount The amount of fuel to remove.
	 * @param doRemove If true, actually removes the fuel. Otherwise, just calculates how much can be removed.
	 * @return The amount of fuel removed; only actually removed if doRemove is set to true.
	 */
	public int removeWaste(FluidStack outgoingWaste, int amount, boolean doRemove) {
		if(getWasteAmount() <= 0 || amount <= 0) {
			return 0;
		}
		
		if(outgoingWaste == null || this.waste.isFluidEqual(outgoingWaste)) {
			int amtToRemove = Math.min(amount, waste.amount);
			if(doRemove) {
				waste.amount -= amount;
				if(waste.amount <= 0) {
					this.waste = null;
				}
			}
			
			if(amtToRemove > 0 && doRemove) {
				this.updateWorldIfNeeded(this.waste == null);
			}
			return amtToRemove;
		}

		return 0;
	}
	
	protected void updateWorldIfNeeded(boolean force) {
		int fuelAmt = this.getFuelAmount();
		int wasteAmt = this.getWasteAmount();
		if(force) {
			this.fuelAtLastUpdate = fuelAmt;
			this.wasteAtLastUpdate = wasteAmt;
			this.worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		}
		else {
			if(Math.abs(this.fuelAtLastUpdate - fuelAmt) > maximumDevianceInContentsBetweenUpdates ||
				Math.abs(this.wasteAtLastUpdate -wasteAmt) > maximumDevianceInContentsBetweenUpdates) {
					this.fuelAtLastUpdate = fuelAmt;
					this.wasteAtLastUpdate = wasteAmt;
					this.worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
			}
		}
	}
	
	// TileEntity stuff
	// We need an update loop for this class, as we show special GUIs
	@Override
	public boolean canUpdate() { return true; }

	@Override
	public void updateEntity() {
		super.updateEntity();
		
		if(!this.worldObj.isRemote && this.updatePlayers.size() > 0) {
			ticksSinceLastUpdate++;
			if(ticksSinceLastUpdate >= ticksBetweenUpdates) {
				sendGuiUpdate();
				ticksSinceLastUpdate = 0;
			}
		}
	}
	
	// Save/Load
	@Override
	public void readFromNBT(NBTTagCompound data) {
		super.readFromNBT(data);
		this.readLocalDataFromNBT(data);
	}
	
	@Override
	public void writeToNBT(NBTTagCompound data) {
		super.writeToNBT(data);
		this.writeLocalDataToNBT(data);
	}
	
    // IRadiationSource
	@Override
	public IRadiationPulse radiate() {
		Random rand = this.worldObj.rand;

		// Generate new heat based on internal fuel state, broadcast radiation pulse
		float internalHeatGenerated = 0.0f;
		float internalPowerGenerated = 0.0f;
		
		float rawNeutronsGenerated = 0.0f;
		float fuelDesired = 0.0f;
		
		// Nothing to do.
		if(this.getFuelAmount() <= 0 && this.getWasteAmount() <= 0) { return new RadiationPulse(); }
		
		if(this.localHeat < 0.0 || Float.isNaN(this.localHeat) || Float.isInfinite(this.localHeat)) {
			// We do not deal with cryogenic reactors. Repair thine self.
			this.localHeat = 0.0f;
		}
		
		if(this.incidentRadiation < 0.0 || Float.isNaN(this.incidentRadiation) || Float.isInfinite(this.incidentRadiation)) {
			// Wacky shit has happened. Try to auto-repair thyself
			this.incidentRadiation = 0.0f;
		}

		// Step 1: Generate raw neutron mass
		// Step 1a: Generate spontaneous neutrons from fuel (consumes fuel)
		if(this.fuel != null && this.fuel.amount > 0) {
			rawNeutronsGenerated += this.fuel.amount * neutronsPerFuel;
			rawNeutronsGenerated *= 1.0f - (this.controlRodInsertion / 100.0f);

			fuelDesired += rawNeutronsGenerated * Math.max(1.0, Math.log10(this.localHeat));
			
			// This will generate some side heat & power
			internalHeatGenerated += rawNeutronsGenerated * heatPerNeutron;
			internalPowerGenerated += rawNeutronsGenerated * powerPerNeutron;
		}

		// Step 1b: Generate neutrons from incident radiation (consumes fuel, but less than above per neutron)
		if(this.incidentRadiation > 0.0f && this.localHeat > 0.0f) {
			float additionalNeutronsGenerated = Math.max(0.0f, this.incidentRadiation * 0.5f - (float)Math.log10(this.localHeat));
			additionalNeutronsGenerated *= 1.0f - ((float)this.controlRodInsertion / 100.0f);

			if(additionalNeutronsGenerated > 0.0f) {
				fuelDesired += additionalNeutronsGenerated * incidentNeutronFuelRate * Math.max(1.0, Math.log10(this.localHeat));
				rawNeutronsGenerated += additionalNeutronsGenerated;
				
				// This will generate some side heat & power
				internalHeatGenerated += additionalNeutronsGenerated * heatPerNeutron;
				internalPowerGenerated += additionalNeutronsGenerated * powerPerNeutron;

				// Reduce incident radiation that was used to produce more neutrons, some of the rest escapes, the rest sticks around
				this.incidentRadiation -= additionalNeutronsGenerated;

				if(this.incidentRadiation < 0.01) { this.incidentRadiation = 0; }
				else if(this.localHeat > 1000.0){ this.incidentRadiation /= Math.log10(this.localHeat); }
			}
		}

		// Step 1c: Consume fuel based on incident neutrons
		if(fuelDesired > 0.0) {
			// Fuel desired is a multiplier to consumption chance.
			// Each neutron adds a 4% chance to consume fuel on top of the normal, time-based chance
			neutronsSinceLastFuelConsumption += (int)fuelDesired;
			double fuelUsageChance = (double)neutronsSinceLastFuelConsumption / maximumNeutronsPerFuel;

			if(rand.nextDouble() < fuelUsageChance) {
				// Use fuel, at least 1, but up to ln(fuelDesired) (8 neutrons = 2 fuel, etc.)
				int fuelUsed = (int)Math.ceil(Math.max(1, Math.log(fuelDesired)));
				if(fuelUsed > 1) {
					 // Random between 1 and fuel desired
					fuelUsed = rand.nextInt(fuelUsed) + 1;
				}

				// Just slurp out whatever fuel we've got inside. This may need a refactor if we allow fuel blends.
				this.removeFuel(null, fuelUsed, true);
				
				if(this.waste == null) {
					IReactorFuel fuelData = BRRegistry.getDataForFuel(this.fuel.getFluid());
					if(fuelData != null && fuelData.getProductFluid() != null) {
						this.waste = new FluidStack(fuelData.getProductFluid(), 0);
					}
					else {
						// Fallback plan, in case something weird is happening
						FMLLog.warning("Big Reactors: Reactor column is defaulting to cyanite waste, as there is no fuel-product data for %s", fuel.getFluid().getName());
						this.waste = new FluidStack(BigReactors.fluidCyanite, 0);
					}
				}
				
				this.addWaste(this.waste, fuelUsed, true);

				neutronsSinceLastFuelConsumption = 0;
			}
		}
		
		// Generate a tiny amount of radiation from waste. A really tiny amount.
		float wasteNeutronsGenerated = (float)this.getWasteAmount() * neutronsPerFuel * wasteNeutronPenalty;
		internalHeatGenerated += wasteNeutronsGenerated * heatPerNeutron;
		internalPowerGenerated += wasteNeutronsGenerated * powerPerNeutron;
		rawNeutronsGenerated += wasteNeutronsGenerated;
		
		// Step 2: Calculate split between fast and slow neutrons.
		// Higher heat = more fast, fewer slow.
		// Forgives the first few hundred degrees before ramping up swiftly, then very swiftly after 1000
		float neutronSplit = 0.1f;
		if(this.localHeat > 0.0f) {
			neutronSplit = 0.1f + Math.max(0.0f, Math.min(0.9f, Math.min(0.0f, (float)Math.log(this.localHeat/75.0f)/9.0f) + Math.min(0.0f, (float)Math.log(this.localHeat/300.0f)/5.0f)));
		}

		float fastNeutrons = neutronSplit * rawNeutronsGenerated;
		float slowNeutrons = (1.0f-neutronSplit) * rawNeutronsGenerated;
		
		// Step 3: Generate initial radiation packet
		// Step 3a: Calculate initial TTL based off of size of pulse
		int ttl = 2;
		if(rawNeutronsGenerated > 0) {
			ttl = 2 + (int)Math.min(1.0, Math.log10(rawNeutronsGenerated));
		}
		
		// Step 3b: Create pulse
		RadiationPulse radiation = new RadiationPulse(fastNeutrons, slowNeutrons, ttl, 0.0f, internalPowerGenerated);

		// Step 4: Pick a direction
		int dx, dz;
		dx = dz = 0;
		// since columns have to run the entire height of the reactor, we can cheat here.
		int dy = yCoord-1;
		
		int direction = rand.nextInt(4);
		switch(direction) {
		case 0:
			dz += 1; break;
		case 1:
			dx +=1; break;
		case 2:
			dz -= 1; break;
		default:
			dx -=1; break;
		}
		
		// Step 5: Run the packet's simulation until it peters out
		TileEntity te;
		IRadiationModerator ir;
		Material mat;

		// Propagate radiation up to 4 blocks away
		int i = 1;
		while(radiation.getTimeToLive() > 0 && (radiation.getFastRadiation() > 0 || radiation.getSlowRadiation() > 0)) {
			te = worldObj.getBlockTileEntity(xCoord + (dx*i), dy, zCoord+(dz*i));
			if(te != null && te instanceof IRadiationModerator) {
				ir = (IRadiationModerator)te;
				ir.receiveRadiationPulse(radiation);
			}
			else {
				mat = worldObj.getBlockMaterial(xCoord + (dx*i), dy, zCoord+(dz*i));
				if(mat != null) {
					modulateRadiationByMaterial(radiation, mat);
				}
				else {
					// Durr..?
					modulateRadiationByMaterial(radiation, Material.air);
				}
			}
			
			// Reduce TTL by one since we've stepped
			radiation.changeTTL(-1);
			i++; // And move through the world
		}

		// Finally, add locally-produced heat to self
		localHeat += internalHeatGenerated;

		return radiation;
	}	

	// Player updates via IBeefGuiEntity
	@Override
	public void beginUpdatingPlayer(EntityPlayer player) {
		updatePlayers.add(player);
		sendGuiUpdatePacketToClient(player);
	}

	@Override
	public void stopUpdatingPlayer(EntityPlayer player) {
		updatePlayers.remove(player);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public GuiScreen getGUI(EntityPlayer player) {
		return new GuiReactorControlRod(getContainer(player), this);
	}

	@Override
	public Container getContainer(EntityPlayer player) {
		return new ContainerReactorControlRod(this, player);
	}
	
	@Override
	public void onReceiveGuiButtonPress(String buttonName, DataInputStream dataStream) throws IOException {
		if(buttonName.equals("rodInsert")) {
			setControlRodInsertion((short)(this.controlRodInsertion + 10));
		}
		else if(buttonName.equals("rodRetract")) {
			setControlRodInsertion((short)(this.controlRodInsertion - 10));
		}
	}
	
	public void onReceiveGuiUpdate(NBTTagCompound updateData) {
		this.readFromNBT(updateData);
	}
	
	
	protected Packet getGuiUpdatePacket() {
		NBTTagCompound childData = new NBTTagCompound();
		this.writeToNBT(childData);
		
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream data = new DataOutputStream(bytes);
		try
		{
			data.write(Packets.ControlRodGuiUpdate);
			data.writeInt(this.xCoord);
			data.writeInt(this.yCoord);
			data.writeInt(this.zCoord);

			// Taken from Packet.java
            byte[] abyte = CompressedStreamTools.compress(childData);
            data.writeShort((short)abyte.length);
            data.write(abyte);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		Packet250CustomPayload newPacket = new Packet250CustomPayload();
		newPacket.channel = BigReactors.CHANNEL;
		newPacket.data = bytes.toByteArray();
		newPacket.length = newPacket.data.length;
		
		return newPacket;
	}
	
	private void sendGuiUpdatePacketToClient(EntityPlayer recipient) {
		if(this.worldObj.isRemote) { return; }

		PacketDispatcher.sendPacketToPlayer(getGuiUpdatePacket(), (Player)recipient);
	}
	
	private void sendGuiUpdate() {
		if(this.worldObj.isRemote) { return; }
		if(this.updatePlayers.size() <= 0) { return; }
		
		Packet data = getGuiUpdatePacket();

		for(EntityPlayer player : updatePlayers) {
			PacketDispatcher.sendPacketToPlayer(data, (Player)player);
		}
	}	

	// Control Rod Updates
	protected void sendControlRodUpdate() {
		if(this.worldObj == null || this.worldObj.isRemote) { return; }

		Packet p = PacketWrapper.createPacket(BigReactors.CHANNEL, Packets.ControlRodUpdate,
				new Object[] { xCoord, yCoord, zCoord, isAssembled, minFuelRodY, controlRodInsertion });
		
		PacketDispatcher.sendPacketToAllAround(xCoord, yCoord, zCoord, 50, worldObj.provider.dimensionId, p);
	}
	
	@SideOnly(Side.CLIENT)
	public void onControlRodUpdate(boolean isAssembled, int minFuelRodY, short controlRodInsertion) {
		this.isAssembled = isAssembled;
		this.minFuelRodY = minFuelRodY;
		this.controlRodInsertion = controlRodInsertion;
	}

    // IRadiationModerator
    
	@Override
	public void receiveRadiationPulse(IRadiationPulse radiation) {
		// Consume thermal neutrons, with a bonus based on control rods
		// 50% normally, scaling linearly to 100% at 100% insertion
		float slowRadiationConsumed = radiation.getSlowRadiation() * (0.5f + (float)this.controlRodInsertion/200.0f);
		
		// Convert 10% of locally-consumed neutrons to power
		radiation.addPower(slowRadiationConsumed*0.1f);
		
		// Remaining 90% will be retained for use in additional neutron generation
		this.incidentRadiation += slowRadiationConsumed * 0.9f;

		// Remove slow radiation that got consumed
		radiation.setSlowRadiation(radiation.getSlowRadiation() - slowRadiationConsumed);

		// Moderate some fast radiation, based on control rod settings
		float fastRadiationModerationFactor = ((float)this.controlRodInsertion / 100.0f);
		// Reduce effectiveness of control rods in moderating fast neutrons as they overheat
		// 1 from 0 to about 500, crosses 0.5 at 2000, 0.5 by 3500.
		fastRadiationModerationFactor *= (-Math.tanh((this.localHeat-2000.0f)/500.0f)/4.0f) + 0.25f;

		float fastRadiationModerated = radiation.getFastRadiation() * fastRadiationModerationFactor;
		if(fastRadiationModerated > 0.0) {
			radiation.setSlowRadiation(radiation.getSlowRadiation() + fastRadiationModerated);
			radiation.setFastRadiation(radiation.getFastRadiation() - fastRadiationModerated);
		}
		
		// Now generate some additional radiation, based on local heat & fuel, at a disadvantaged rate
		float newFastRadiation = this.getFuelAmount() * this.neutronsPerFuel * 0.25f * Math.min(0.01f, Math.max(1.0f, 1.0f - (this.localHeat / 2000.0f)));
		radiation.setFastRadiation(radiation.getFastRadiation() + newFastRadiation);

		// Strengthen the pulse so it travels further in truly huge reactors
		radiation.changeTTL(1);		
	}
	
	// IHeatEntity
	@Override
	public float getHeat() {
		return localHeat;
	}

	@Override
	public float getThermalConductivity() {
		return IHeatEntity.conductivityCopper;
	}

	@Override
	public float onAbsorbHeat(IHeatEntity source, HeatPulse pulse, int faces, int contactArea) {
		float deltaTemp = source.getHeat() - getHeat();
		if(deltaTemp <= 0.0f) {
			return 0.0f;
		}

		float heatToAbsorb = deltaTemp * 0.05f * getThermalConductivity() * (1.0f/(float)faces) * contactArea;

		// Just zero it out after a while
		if(deltaTemp < 0.01) {
			heatToAbsorb = deltaTemp;
		}

		localHeat += heatToAbsorb;
		
		if(localHeat < 0.0) { localHeat = 0.0f; }

		return heatToAbsorb;
	}

	/**
	 * This method is used to leak heat from the fuel rods
	 * into the reactor. It should run regardless of activity.
	 * @param ambientHeat The heat of the reactor surrounding the fuel rod.
	 * @return A HeatPulse containing the environmental results of radiating heat.
	 */
	@Override
	public HeatPulse onRadiateHeat(float ambientHeat) {
		HeatPulse results = new HeatPulse();
		TileEntity te;
		IHeatEntity he;
		float lostHeat = 0.0f;

		if(!this.isAssembled) {
			return null;
		}
		
		// Run this along the length of the stack, in case of a nonuniform interior
		ForgeDirection[] dirs = new ForgeDirection[] { ForgeDirection.NORTH, ForgeDirection.SOUTH, ForgeDirection.EAST, ForgeDirection.NORTH};
		for(int dy = this.minFuelRodY; dy < yCoord; dy++) {
			for(ForgeDirection dir : dirs) {
				te = this.worldObj.getBlockTileEntity(xCoord + dir.offsetX, dy, zCoord + dir.offsetZ);
				if(te != null && te instanceof IHeatEntity) {
					he = (IHeatEntity)te;
					lostHeat += he.onAbsorbHeat(this, results, dirs.length, 1);
				}
				else {
					lostHeat += transmitHeatByMaterial(ambientHeat, this.worldObj.getBlockMaterial(xCoord + dir.offsetX, dy + dir.offsetY, zCoord + dir.offsetZ), results, dirs.length);
				}
			}
		}
		
		localHeat -= lostHeat;
		if(localHeat < 0.0f) { localHeat = 0.0f; }
		return results;
	}

	private float transmitHeatByMaterial(float ambientHeat, Material material, HeatPulse pulse, int faces) {
		if(localHeat <= ambientHeat) {
			return 0.0f;
		}
		
		float thermalConductivity = IHeatEntity.conductivityAir;
		float conversionEfficiency = 0.1f;
		
		if(material.equals(Material.water)) {
			thermalConductivity = IHeatEntity.conductivityWater;
			conversionEfficiency = 0.75f;
		}
		
		float heatToTransfer = (localHeat - ambientHeat) * thermalConductivity * (1.0f/(float)faces);
		if((localHeat - ambientHeat) < 0.01) {
			heatToTransfer = localHeat - ambientHeat;
		}

		pulse.powerProduced += heatToTransfer * conversionEfficiency * powerPerHeat;
		pulse.heatChange += heatToTransfer * (1.0-conversionEfficiency);
		
		return heatToTransfer;
	}
	
	// Helpers
	private void onControlRodAssembled() {
		if(this.worldObj.isRemote) { return; }

		this.isAssembled = true;
		
		// Look for at least one fuel rod beneath us
		minFuelRodY = this.yCoord - 1;
		int blocksChecked = 0;
		while(blocksChecked <= maxFuelRodsBelow) {
			TileEntity te = this.worldObj.getBlockTileEntity(xCoord, minFuelRodY, zCoord);
			if(te != null && te instanceof TileEntityFuelRod) {
				((TileEntityFuelRod)te).onAssemble(this);
			}
			else {
				break;
			}
			
			blocksChecked++;
			minFuelRodY--;
		}
		
		minFuelRodY++;

		sendControlRodUpdate();
	}

	private void onControlRodDisassembled() {
		if(this.worldObj.isRemote) { return; }
		if(!this.isAssembled) { return; }

		// Notify all fuel rods beneath us that we're disassembling
		if(!this.worldObj.isRemote) {
			TileEntity te;
			for(int dy = this.yCoord - 1; dy >= this.minFuelRodY; dy--) {
				te = this.worldObj.getBlockTileEntity(xCoord, yCoord, zCoord);
				if(te != null && te instanceof TileEntityFuelRod) {
					((TileEntityFuelRod)te).onDisassemble();
				}
			}
		}
		
		this.isAssembled = false;
		sendControlRodUpdate();
	}
	
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox()
    {
    	if(!this.isAssembled || this.getColumnHeight() < 1) {
    		return super.getRenderBoundingBox();
    	}

    	return AxisAlignedBB.getAABBPool().getAABB(xCoord, yCoord - getColumnHeight(), zCoord, xCoord + 1, yCoord + 1, zCoord + 1);
    }

	private static float lerp(float from, float to, float proportion) {
		return from + (to - from) * proportion;
	}
	
	private void modulateRadiationByMaterial(RadiationPulse radiation,
			Material material) {
		if(material == Material.lava) {
			// Lose 25% of slow
			int moderated = (int)((float)radiation.getSlowRadiation() * 0.25f);
			radiation.setSlowRadiation(radiation.getSlowRadiation() - moderated);
			radiation.changeHeat(moderated);
			
			// Convert 50% of remainder to fast, because you are dumb
			moderated = (int)((float)radiation.getSlowRadiation() * 0.5f);
			radiation.setSlowRadiation(radiation.getSlowRadiation() - moderated);
			radiation.setFastRadiation(radiation.getFastRadiation() + moderated);
		}
		else {
			// Air/stone/dirt produces only tiny amounts of moderation, 20%
			float moderationFactor = 0.2f;
			
			// Water will consume 80% of slow
			if(material == Material.water) {
				moderationFactor = 0.80f;
			}

			// Remove moderated slow radiation
			float moderated = radiation.getSlowRadiation() * moderationFactor;
			radiation.setSlowRadiation(Math.max(0.0f, radiation.getSlowRadiation() - moderated));
			
			// Convert moderated slow to power, the rest to heat, if in coolant.
			if(material == Material.water) {
				// Moderate 60% of fast in water and generate 30% heat as power
				moderationFactor = 0.60f;

				// Directly generate energy based on heat
				radiation.addPower(moderated * moderationFactor/2.0f * powerPerNeutron);
				moderated -= moderated * moderationFactor;
				
				// Apply the rest of the energy as reactor heat
				radiation.changeHeat(moderated);
				
				// Convert some fast to slow, using the same proportions as above.
				moderated = radiation.getFastRadiation() * moderationFactor;
				radiation.setFastRadiation(radiation.getFastRadiation() - moderated);
				radiation.setSlowRadiation(radiation.getSlowRadiation() + moderated);
			}
			else {
				// You just get some reactor heat :(
				radiation.changeHeat(moderated * 0.2f);
			}
		}
	}

	private void readLocalDataFromNBT(NBTTagCompound data) {
		if(data.hasKey("localHeat")) {
			this.localHeat = data.getFloat("localHeat");
			
			// Handle legacy (0.1.x) saves
			if(localHeat == 0.0f) {
				this.localHeat = (float)data.getDouble("localHeat");
			}

			if(Float.isNaN(localHeat)) { localHeat = 0.0f; }
		}
		
		if(data.hasKey("incidentRadiation")) {
			incidentRadiation = data.getFloat("incidentRadiation");

			// Handle legacy saves
			if(incidentRadiation == 0.0f) {
				this.incidentRadiation = (float)data.getDouble("incidentRadiation");
			}

			if(Float.isNaN(incidentRadiation)) { incidentRadiation = 0.0f; }
		}
		
		if(data.hasKey("ticksSinceLastFuelConsumption")) {
			this.neutronsSinceLastFuelConsumption = data.getInteger("ticksSinceLastFuelConsumption");
		}
		
		this.fuel = null;
		if(data.hasKey("fuelFluidStack")) {
			this.fuel = FluidStack.loadFluidStackFromNBT(data.getCompoundTag("fuelFluidStack"));
		}
		this.fuelAtLastUpdate = getFuelAmount();

		this.waste = null;
		if(data.hasKey("wasteFluidStack")) {
			this.waste = FluidStack.loadFluidStackFromNBT(data.getCompoundTag("wasteFluidStack"));
		}
		this.wasteAtLastUpdate = this.getWasteAmount();
		
		if(data.hasKey("controlRodInsertion")) {
			this.controlRodInsertion = data.getShort("controlRodInsertion");
		}
	}
	
	private void writeLocalDataToNBT(NBTTagCompound data) {
		data.setFloat("incidentRadiation", this.incidentRadiation);
		data.setFloat("localHeat", this.localHeat);
		data.setInteger("ticksSinceLastFuelConsumption", this.neutronsSinceLastFuelConsumption);
		data.setShort("controlRodInsertion", this.controlRodInsertion);
		
		if(this.fuel != null) {
			NBTTagCompound fuelData = new NBTTagCompound();
			this.fuel.writeToNBT(fuelData);
			data.setCompoundTag("fuelFluidStack", fuelData);
		}
		
		if(this.waste != null) {
			NBTTagCompound wasteData = new NBTTagCompound();
			data.setCompoundTag("wasteFluidStack", wasteData);
		}
	}
	
	// MultiblockTileEntityBase
	@Override
	public MultiblockControllerBase getNewMultiblockControllerObject() {
		return new MultiblockReactor(this.worldObj);
	}

	@Override
	public boolean isGoodForFrame() {
		return false;
	}

	@Override
	public boolean isGoodForSides() {
		return false;
	}

	@Override
	public boolean isGoodForTop() {
		// Check that the space below us is a fuel rod
		TileEntity teBelow = this.worldObj.getBlockTileEntity(xCoord, yCoord - 1, zCoord);
		return teBelow instanceof TileEntityFuelRod;
	}

	@Override
	public boolean isGoodForBottom() {
		return false;
	}

	@Override
	public boolean isGoodForInterior() {
		return false;
	}

	@Override
	public void onMachineAssembled() {
		this.onControlRodAssembled();
	}

	@Override
	public void onMachineBroken() {
		this.onControlRodDisassembled();
	}

	@Override
	public void onMachineActivated() {
	}

	@Override
	public void onMachineDeactivated() {
	}
	
	@Override
	protected void formatDescriptionPacket(NBTTagCompound packet) {
		super.formatDescriptionPacket(packet);
		NBTTagCompound localData = new NBTTagCompound();
		this.writeLocalDataToNBT(localData);
		localData.setBoolean("isAssembled", this.isAssembled);
		localData.setInteger("minFuelRodY", this.minFuelRodY);
		packet.setCompoundTag("reactorControlRod", localData);
	}
	
	@Override
	protected void decodeDescriptionPacket(NBTTagCompound packet) {
		super.decodeDescriptionPacket(packet);
		
		if(packet.hasKey("reactorControlRod")) {
			NBTTagCompound localData = packet.getCompoundTag("reactorControlRod");
			this.readLocalDataFromNBT(localData);
			
			if(localData.hasKey("isAssembled")) {
				this.isAssembled = localData.getBoolean("isAssembled");
			}
			
			if(localData.hasKey("minFuelRodY")) {
				this.minFuelRodY = localData.getInteger("minFuelRodY");
			}
		}
		
	}
}
