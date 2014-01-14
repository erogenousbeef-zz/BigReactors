package erogenousbeef.bigreactors.common.multiblock.tileentity;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidBlock;
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
import erogenousbeef.bigreactors.common.tileentity.TileEntityFuelRod;
import erogenousbeef.bigreactors.gui.IBeefGuiEntity;
import erogenousbeef.bigreactors.gui.container.ContainerReactorControlRod;
import erogenousbeef.bigreactors.net.PacketWrapper;
import erogenousbeef.bigreactors.net.Packets;
import erogenousbeef.core.multiblock.MultiblockControllerBase;
import erogenousbeef.core.multiblock.MultiblockTileEntityBase;
import erogenousbeef.core.multiblock.MultiblockValidationException;

public class TileEntityReactorControlRod extends MultiblockTileEntityBase implements IRadiationSource, IRadiationModerator, IBeefGuiEntity {
	public final static int maxTotalFluidPerBlock = FluidContainerRegistry.BUCKET_VOLUME * 4;
	public final static short maxInsertion = 100;
	public final static short minInsertion = 0;

	// Game Balance Values
	// TODO: Make these configurable
	private static final float maximumNeutronsPerFuel = 50000f; // Should be a few minutes per ingot, on average.
	private static final float neutronsPerFuel = 0.001f; // neutrons per fuel unit
	private static final float heatPerNeutron = 0.1f; // C per fission event
	private static final float powerPerNeutron = 10f; // RF units per fission event
	private static final float wasteNeutronPenalty = 0.01f;
	private static final float incidentNeutronFuelRate = 0.25f;
	private static final float incidentRadiationDecayRate = 0.5f;

	protected boolean isAssembled = false;
	protected int minFuelRodY;

	// Radiation
	protected float incidentRadiation; // Radiation received since last radiate() call
	protected short controlRodInsertion; // 0 = retracted fully, 100 = inserted fully
	
	// Fuel Consumption
	protected int neutronsSinceLastFuelConsumption;
	
	// User settings
	protected String name;

	// GUI messaging
	private static final int INVALID_Y = Integer.MIN_VALUE;
	
	// Backwards Compatibility
	private FluidStack cachedFuel;
	
	public TileEntityReactorControlRod() {
		super();
	
		isAssembled = false;

		incidentRadiation = 0.0f;
		neutronsSinceLastFuelConsumption = 0;
		minFuelRodY = INVALID_Y;
		controlRodInsertion = minInsertion;
		
		name = "";
		
		cachedFuel = null;
	}
	
	// Data accessors
	public boolean isAssembled() {
		return isAssembled;
	}
	
	public short getControlRodInsertion() {
		return this.controlRodInsertion;
	}
	
	public FluidStack getCachedFuel() { return cachedFuel; }
	
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

	// TileEntity stuff
	@Override
	public boolean canUpdate() { return false; }

	// Save/Load
	@Override
	public void readFromNBT(NBTTagCompound data) {
		super.readFromNBT(data);
		this.readLocalDataFromNBT(data);
		
		if(data.hasKey("name")) {
			this.name = data.getString("name");
		}
		
		if(data.hasKey("fuelFluidStack")) {
			this.cachedFuel = FluidStack.loadFluidStackFromNBT(data.getCompoundTag("fuelFluidStack"));
		}
	}
	
	@Override
	public void writeToNBT(NBTTagCompound data) {
		super.writeToNBT(data);
		this.writeLocalDataToNBT(data);
		
		if(!this.name.isEmpty()) {
			data.setString("name", this.name);
		}
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
		
		int fuelAmt, wasteAmt;
		fuelAmt = wasteAmt = 0;

		// Nothing to do.
		if(fuelAmt <= 0 && wasteAmt <= 0) { return new RadiationPulse(); }
		
		if(this.incidentRadiation < 0.0 || Float.isNaN(this.incidentRadiation) || Float.isInfinite(this.incidentRadiation)) {
			// Wacky shit has happened. Try to auto-repair thyself
			this.incidentRadiation = 0.0f;
		}

		// Hotter fuel rods fuse less.
//		float heatFertilityModifier = 1f + (float)(-0.95f*Math.exp(-10f*Math.exp(-0.0012f*this.localHeat)));

		// Step 1: Generate raw neutron mass
		// Step 1a: Generate spontaneous neutrons from fuel (consumes fuel)
		if(fuelAmt > 0) {

//			rawNeutronsGenerated += fuelAmt * neutronsPerFuel * heatFertilityModifier;
			rawNeutronsGenerated *= 1.0f - (this.controlRodInsertion / 100.0f);

			//fuelDesired += rawNeutronsGenerated * Math.max(1.0, Math.log10(this.localHeat));
			
			// This will generate some side heat & power
			internalHeatGenerated += rawNeutronsGenerated * heatPerNeutron;
			internalPowerGenerated += rawNeutronsGenerated * powerPerNeutron;

			// Step 1b: Generate neutrons from incident radiation (consumes fuel, but less than above per neutron)
/*			if(this.incidentRadiation > 0.0f && this.localHeat > 0.0f) {
				float additionalNeutronsGenerated = Math.max(0.0f, heatFertilityModifier * this.incidentRadiation * 0.5f - (float)Math.log10(this.localHeat));
				additionalNeutronsGenerated *= 1.0f - ((float)this.controlRodInsertion / 100.0f);
	
				if(additionalNeutronsGenerated > 0.0f) {
					fuelDesired += additionalNeutronsGenerated * incidentNeutronFuelRate * Math.max(1.0, Math.log10(this.localHeat));
					rawNeutronsGenerated += additionalNeutronsGenerated;
					
					// This will generate some side heat & power
					internalHeatGenerated += additionalNeutronsGenerated * heatPerNeutron;
					internalPowerGenerated += additionalNeutronsGenerated * powerPerNeutron;
	
					// Reduce incident radiation at a slower rate than they're actually used.
					// This should help smooth out power production.
					this.incidentRadiation -= additionalNeutronsGenerated * incidentRadiationDecayRate;
	
					if(this.incidentRadiation < 0.01) { this.incidentRadiation = 0; }
					else if(this.localHeat > 1000.0){ this.incidentRadiation /= Math.log10(this.localHeat); }
				}
			}
*/
		}

		// Step 1c: Consume fuel based on incident neutrons, if we have any fuel
		if(fuelDesired > 0.0 && fuelAmt > 0) {
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

				/*
				if(this.waste == null) {
					IReactorFuel fuelData = BRRegistry.getDataForFuel(this.fuel.getFluid());
					FluidStack wasteToAdd = null;
					if(fuelData != null && fuelData.getProductFluid() != null) {
						wasteToAdd = new FluidStack(fuelData.getProductFluid(), fuelUsed);
					}
					else {
						// Fallback plan, in case something weird is happening
						FMLLog.warning("Big Reactors: Reactor column is defaulting to cyanite waste, as there is no fuel-product data for %s", fuel.getFluid().getName());
						wasteToAdd = new FluidStack(BigReactors.fluidCyanite, fuelUsed);
					}

					this.removeFuel(null, fuelUsed, true);
					this.addWaste(wasteToAdd, fuelUsed, true);
				}
				else {
					this.removeFuel(null, fuelUsed, true);
					this.addWaste(this.waste, fuelUsed, true);
				}
				*/

				neutronsSinceLastFuelConsumption = 0;
			}
		}
		
		// Generate a tiny amount of radiation from waste. A really tiny amount.
		float wasteNeutronsGenerated = (float)wasteAmt * neutronsPerFuel * wasteNeutronPenalty;
		internalHeatGenerated += wasteNeutronsGenerated * heatPerNeutron;
		internalPowerGenerated += wasteNeutronsGenerated * powerPerNeutron;
		rawNeutronsGenerated += wasteNeutronsGenerated;
		
		// Step 2: Calculate split between fast and slow neutrons.
		// Higher heat = more fast, fewer slow.
		// Forgives the first few hundred degrees before ramping up swiftly, then very swiftly after 1000
		float neutronSplit = 0.1f;
		
		/*
		 * Disabled because this horribly nerfs large reactors.
		 * TODO: Restore this in 0.3 with better heat-dissipation 
		if(this.localHeat > 0.0f) {
			neutronSplit = 0.1f + Math.max(0.0f, Math.min(0.9f, Math.max(0.0f, (float)Math.log(this.localHeat/75.0f)/9.0f) + Math.max(0.0f, (float)Math.log(this.localHeat/300.0f)/5.0f)));
		}
		*/

		float fastNeutrons = neutronSplit * rawNeutronsGenerated;
		float slowNeutrons = (1.0f-neutronSplit) * rawNeutronsGenerated;
		
		// Step 3: Generate initial radiation packet
		// Step 3a: Calculate initial TTL based off of size of pulse
		int ttl = 2;
		if(rawNeutronsGenerated > 0) {
			ttl = 2 + (int)Math.min(1.0, Math.log10(rawNeutronsGenerated));
		}
		
		// Step 3b: Create pulse
		RadiationPulse radiation = new RadiationPulse(fastNeutrons, slowNeutrons, ttl, internalPowerGenerated);

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
		int blockId;
		while(radiation.getTimeToLive() > 0 && (radiation.getFastRadiation() > 0 || radiation.getSlowRadiation() > 0)) {
			te = worldObj.getBlockTileEntity(xCoord + (dx*i), dy, zCoord+(dz*i));
			if(te != null && te instanceof IRadiationModerator) {
				ir = (IRadiationModerator)te;
				ir.receiveRadiationPulse(radiation);
			}
			else {
				mat = worldObj.getBlockMaterial(xCoord + (dx*i), dy, zCoord+(dz*i));
				blockId = worldObj.getBlockId(xCoord + (dx*i), dy, zCoord+(dz*i));
				if(mat != null) {
					modulateRadiationByMaterialAndBlock(radiation, mat, blockId);
				}
				else {
					// Durr..?
					modulateRadiationByMaterialAndBlock(radiation, Material.air, -1);
				}
			}
			
			// Reduce TTL by one since we've stepped
			radiation.changeTTL(-1);
			i++; // And move through the world
		}

		// Finally, add locally-produced heat to self
//		localHeat += internalHeatGenerated;

		return radiation;
	}	

	// Player updates via IBeefGuiEntity
	// TODO REMOVEME
	@Override
	public void beginUpdatingPlayer(EntityPlayer player) {
	}

	@Override
	public void stopUpdatingPlayer(EntityPlayer player) {
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
		//fastRadiationModerationFactor *= (-Math.tanh((this.localHeat-2000.0f)/500.0f)/4.0f) + 0.25f;

		float fastRadiationModerated = radiation.getFastRadiation() * fastRadiationModerationFactor;
		if(fastRadiationModerated > 0.0) {
			radiation.setSlowRadiation(radiation.getSlowRadiation() + fastRadiationModerated);
			radiation.setFastRadiation(radiation.getFastRadiation() - fastRadiationModerated);
		}
		
		// Now generate some additional radiation, based on local heat & fuel, at a disadvantaged rate
		int FUELAMOUNT = 0;
		//float newFastRadiation = FUELAMOUNT * this.neutronsPerFuel * 0.25f * Math.min(0.01f, Math.max(1.0f, 1.0f - (this.localHeat / 2000.0f)));
		//radiation.setFastRadiation(radiation.getFastRadiation() + newFastRadiation);

		// Strengthen the pulse so it travels further in truly huge reactors
		radiation.changeTTL(1);		
	}
	
	/**
	 * Transmits heat out from one face of the rod.
	 * @param ambientHeat Ambient heat of the surrounding reactor environment.
	 * @param material Material we are radiating through.
	 * @param pulse The heatpulse result.
	 * @return
	 */
	/*
	private float transmitHeatByMaterialAndBlock(float ambientHeat, Material material, int blockId, HeatPulse pulse) {
		if(localHeat <= ambientHeat) {
			return 0.0f;
		}
		
		float thermalConductivity = IHeatEntity.conductivityAir;

		if(material.equals(Material.water)) {
			thermalConductivity = IHeatEntity.conductivityWater;
		}
		else if(!material.equals(Material.air)) {
			// Check block for data
			if(blockId == Block.blockGold.blockID) {
				thermalConductivity = IHeatEntity.conductivityGold;
			}
			else if(blockId == Block.blockDiamond.blockID) {
				thermalConductivity = IHeatEntity.conductivityDiamond;
			}
			else if(blockId == Block.blockIron.blockID) {
				thermalConductivity = IHeatEntity.conductivityIron;
			}
			else if(blockId > 0 && blockId < Block.blocksList.length) {
				// Check for fluids
				Block blockClass = Block.blocksList[blockId];
				if(blockClass instanceof IFluidBlock) {
					String fluidName = ((IFluidBlock)blockClass).getFluid().getName();
					if(fluidName.equals("cryotheum")) {
						thermalConductivity = IHeatEntity.conductivityCopper;
					}
					else if(fluidName.equals("pyrotheum")) {
						thermalConductivity = IHeatEntity.conductivityGold;
					}
					else if(fluidName.equals("redstone")) {
						thermalConductivity = IHeatEntity.conductivityIron;
					}
					else if(fluidName.equals("glowstone")) {
						thermalConductivity = IHeatEntity.conductivityIron;
					}
					else if(fluidName.equals("ender")) {
						thermalConductivity = IHeatEntity.conductivityGold;
					}
				}
			}
			else {
				// Weird edge case?
				thermalConductivity = IHeatEntity.conductivityGlass;
			}
		}
		
		float heatToTransfer = (localHeat - ambientHeat) * thermalConductivity * 0.25f * this.getColumnHeight();
		if((localHeat - ambientHeat) < 0.01f) {
			heatToTransfer = localHeat - ambientHeat;
		}

		pulse.heatChange += heatToTransfer;
		
		return heatToTransfer;
	}
	*/
	
	// Helpers
	private void onControlRodAssembled() {
		if(this.worldObj.isRemote) { return; }

		this.isAssembled = true;
		
		// Look for at least one fuel rod beneath us
		minFuelRodY = this.yCoord - 1;
		int blocksChecked = 0;
		while(blocksChecked <= BigReactors.maximumReactorHeight - 2) {
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
	
	private void modulateRadiationByMaterialAndBlock(RadiationPulse radiation,
			Material material, int blockId) {
		// This data is for air
		float neutronPermeability = 0.95f; // How much radiation can pass through (allowing slow neutrons to pass)
		float neutronHeating = 0.5f; // How much heat to generate per absorbed neutron
		float neutronModeration = 0.1f; // How many neutrons to moderate (i.e. how many fast neutrons to downconvert)

		if(blockId == Block.blockIron.blockID) {
			neutronPermeability = 0.5f;
			neutronModeration = 0.6f;
		}
		else if(blockId == Block.blockGold.blockID) {
			neutronPermeability = 0.6f;
			neutronModeration = 0.7f;
			neutronHeating = 0.75f;
		}
		else if(blockId == Block.blockDiamond.blockID) {
			neutronPermeability = 0.7f;
			neutronModeration = 0.5f;
			neutronHeating = 1.0f;
		}
		else if(blockId > 0 && blockId < Block.blocksList.length) {
			Block blockClass = Block.blocksList[blockId];
			if(blockClass instanceof IFluidBlock) {
				String fluidName = ((IFluidBlock)blockClass).getFluid().getName();
				if(fluidName.equals("cryotheum")) {
					// Effortdynamics
					neutronHeating = 0.75f;
					neutronModeration = 0.6f;
					neutronPermeability = 0.9f;
				}
				else if(fluidName.equals("pyrotheum")) {
					neutronHeating = 0.9f;
					neutronModeration = 0.2f;
					neutronPermeability = 0.85f;
				}
				else if(fluidName.equals("redstone")) {
					neutronModeration = 0.75f;
					neutronPermeability = 0.75f;
				}
				else if(fluidName.equals("glowstone")) {
					neutronModeration = 0.4f;
					neutronPermeability = 0.8f;
				}
				else if(fluidName.equals("ender")) {
					// It's basically a brick wall
					neutronModeration = 1.0f;
					neutronPermeability = 0.0f;
					neutronHeating = 1.0f;
				}
				else if(fluidName.equals("water")) {
					neutronPermeability = 0.8f;
					neutronModeration = 0.5f;
				}
			}
		}
		else if(material == Material.water) {
			neutronPermeability = 0.8f;
			neutronModeration = 0.5f;
		}
		// Else, treat as air and use default values
		
		float neutronsCaptured, neutronsModerated;
		neutronsCaptured = radiation.getSlowRadiation() * 1f - neutronPermeability;
		neutronsModerated = radiation.getFastRadiation() * neutronModeration;
		
		// TODO: Allow heating from radiation again in 0.3
		radiation.setSlowRadiation(Math.max(0f, radiation.getSlowRadiation() - neutronsCaptured + neutronsModerated));
		radiation.setFastRadiation(Math.max(0f, radiation.getFastRadiation() - neutronsModerated));
		radiation.addPower(neutronsCaptured * neutronHeating * BigReactors.powerPerHeat);
	}

	private void readLocalDataFromNBT(NBTTagCompound data) {
		if(data.hasKey("incidentRadiation")) {
			incidentRadiation = data.getFloat("incidentRadiation");

			if(Float.isNaN(incidentRadiation)) { incidentRadiation = 0.0f; }
		}
		
		if(data.hasKey("ticksSinceLastFuelConsumption")) {
			this.neutronsSinceLastFuelConsumption = data.getInteger("ticksSinceLastFuelConsumption");
		}
		
		if(data.hasKey("controlRodInsertion")) {
			this.controlRodInsertion = data.getShort("controlRodInsertion");
		}
	}
	
	private void writeLocalDataToNBT(NBTTagCompound data) {
		data.setFloat("incidentRadiation", this.incidentRadiation);
		data.setInteger("ticksSinceLastFuelConsumption", this.neutronsSinceLastFuelConsumption);
		data.setShort("controlRodInsertion", this.controlRodInsertion);
	}
	
	// MultiblockTileEntityBase
	@Override
	public MultiblockControllerBase createNewMultiblock() {
		return new MultiblockReactor(this.worldObj);
	}
	
	@Override
	public Class<? extends MultiblockControllerBase> getMultiblockControllerType() { return MultiblockReactor.class; }

	@Override
	public void isGoodForFrame() throws MultiblockValidationException {
		throw new MultiblockValidationException(String.format("%d, %d, %d - Control rods may only be placed on the top face", xCoord, yCoord, zCoord));
	}

	@Override
	public void isGoodForSides() throws MultiblockValidationException {
		throw new MultiblockValidationException(String.format("%d, %d, %d - Control rods may only be placed on the top face", xCoord, yCoord, zCoord));
	}

	@Override
	public void isGoodForTop() throws MultiblockValidationException {
		// Check that the space below us is a fuel rod
		TileEntity teBelow = this.worldObj.getBlockTileEntity(xCoord, yCoord - 1, zCoord);
		if(!(teBelow instanceof TileEntityFuelRod)) {
			throw new MultiblockValidationException(String.format("%d, %d, %d - Control rods may only be placed on the top face, atop a column of fuel rods", xCoord, yCoord, zCoord));
		}
	}

	@Override
	public void isGoodForBottom() throws MultiblockValidationException {
		throw new MultiblockValidationException(String.format("%d, %d, %d - Control rods may only be placed on the top face", xCoord, yCoord, zCoord));
	}

	@Override
	public void isGoodForInterior() throws MultiblockValidationException {
		throw new MultiblockValidationException(String.format("%d, %d, %d - Control rods may only be placed on the top face", xCoord, yCoord, zCoord));
	}

	@Override
	public void onMachineAssembled(MultiblockControllerBase multiblockControllerBase) {
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
	protected void encodeDescriptionPacket(NBTTagCompound packet) {
		super.encodeDescriptionPacket(packet);
		NBTTagCompound localData = new NBTTagCompound();
		this.writeLocalDataToNBT(localData);
		localData.setBoolean("isAssembled", this.isAssembled);
		localData.setString("name", this.name);
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
			
			if(localData.hasKey("name")) {
				this.name = localData.getString("name");
			}
			
			if(this.worldObj != null) {
				this.worldObj.markBlockForRenderUpdate(xCoord, yCoord, zCoord);
			}
		}
	}
	
	public void setName(String newName) {
		if(this.name.equals(newName)) { return; }
		
		this.name = newName;
		if(!this.worldObj.isRemote) {
			this.worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		}
	}
	
	public String getName() {
		return this.name;
	}
}
