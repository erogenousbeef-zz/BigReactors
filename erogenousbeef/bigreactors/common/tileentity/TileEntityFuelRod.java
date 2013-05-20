package erogenousbeef.bigreactors.common.tileentity;

import java.util.Random;

import erogenousbeef.bigreactors.api.HeatPulse;
import erogenousbeef.bigreactors.api.IHeatEntity;
import erogenousbeef.bigreactors.api.IRadiationModerator;
import erogenousbeef.bigreactors.api.IRadiationPulse;
import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.common.RadiationPulse;
import net.minecraft.block.material.Material;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet132TileEntityData;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.liquids.ILiquidTank;
import net.minecraftforge.liquids.ITankContainer;
import net.minecraftforge.liquids.LiquidContainerRegistry;
import net.minecraftforge.liquids.LiquidStack;
import net.minecraftforge.liquids.LiquidTank;

public class TileEntityFuelRod extends TileEntity implements ITankContainer, IRadiationModerator, IHeatEntity {

	public final static int maxTotalLiquid = LiquidContainerRegistry.BUCKET_VOLUME * 4;
	
	public final static int fuelTankIndex = 0;
	public final static int wasteTankIndex = 1;
	public final static int numTanks = 2;
	
	// 1 ingot = 1 bucket
	protected LiquidTank fuelTank;
	protected LiquidTank wasteTank;

	protected double localHeat;
	protected int ticksSinceLastFuelConsumption;
	protected final int averageTicksToConsumeFuel = 1200; // 60 secs (20 ticks / sec)
	
	public TileEntityFuelRod() {
		super();
		
		fuelTank = new LiquidTank(maxTotalLiquid);
		wasteTank = new LiquidTank(maxTotalLiquid);
		localHeat = 0.0;
		ticksSinceLastFuelConsumption = 0;
	}
	
	public LiquidStack getFuel() { return fuelTank.getLiquid(); }
	public LiquidStack getWaste() { return wasteTank.getLiquid(); }
	
	public int getTotalLiquid() {
		int currAmt = 0;
		LiquidStack fuelLiquid = fuelTank.getLiquid();
		if(fuelLiquid != null) {
			currAmt += fuelLiquid.amount;
		}
		
		LiquidStack wasteLiquid = wasteTank.getLiquid();
		if(wasteLiquid != null) {
			currAmt += wasteLiquid.amount;
		}

		return currAmt;
	}
	
	public boolean isFull() {
		return getTotalLiquid() >= maxTotalLiquid;
	}

	// Returns fuel added in millibuckets
	public int addFuel(int fuelToAdd) {
		int spaceRemaining = maxTotalLiquid - getTotalLiquid();
		fuelToAdd = Math.min(spaceRemaining, fuelToAdd);
		
		LiquidStack fuel = fuelTank.getLiquid();
		
		if(fuel == null) {
			fuel = BigReactors.liquidYellorium.copy();
			fuel.amount = fuelToAdd;
			fuelTank.setLiquid(fuel);
		}
		else {
			fuel.amount += fuelToAdd;
		}
		
		return fuelToAdd;
	}
	
	// Returns waste added in millibuckets
	public int addWaste(int wasteToAdd) {
		int spaceRemaining = maxTotalLiquid - getTotalLiquid();
		wasteToAdd = Math.min(spaceRemaining, wasteToAdd);
		
		LiquidStack waste = wasteTank.getLiquid();
		
		if(waste == null) {
			waste = BigReactors.liquidCyanite.copy();
			waste.amount = wasteToAdd;
			wasteTank.setLiquid(waste);
		}
		else {
			waste.amount += wasteToAdd;
		}
		
		return wasteToAdd;
	}
	
	public void doEmpty() {
		fuelTank.setLiquid(null);
		wasteTank.setLiquid(null);
	}

	// Save/Load
	@Override
	public void readFromNBT(NBTTagCompound data) {
		super.readFromNBT(data);
		
		if(data.hasKey("localHeat")) {
			localHeat = data.getDouble("localHeat");
		}
		
		LiquidStack fuel = LiquidStack.loadLiquidStackFromNBT(data.getCompoundTag("fuelTank"));
		if(fuel != null) {
			fuelTank.setLiquid(fuel);
		}
		
		LiquidStack waste = LiquidStack.loadLiquidStackFromNBT(data.getCompoundTag("wasteTank"));
		if(waste != null) {
			wasteTank.setLiquid(waste);
		}
	}
	
	@Override
	public void writeToNBT(NBTTagCompound data) {
		super.writeToNBT(data);

		data.setDouble("localHeat", localHeat);
		
		if(fuelTank.getLiquid() != null) {
			data.setTag("fuelTank", fuelTank.getLiquid().writeToNBT(new NBTTagCompound()));
		}
		
		if(wasteTank.getLiquid() != null) {
			data.setTag("wasteTank", wasteTank.getLiquid().writeToNBT(new NBTTagCompound()));
		}
	}

	// Networking
	
	@Override
	public Packet getDescriptionPacket() {
		NBTTagCompound tagCompound = new NBTTagCompound();
		this.writeToNBT(tagCompound);
		
		return new Packet132TileEntityData(xCoord, yCoord, zCoord, 0, tagCompound);
	}
	
	@Override
	public void onDataPacket(INetworkManager network, Packet132TileEntityData packet) {
		this.readFromNBT(packet.customParam1);
	}
	
	// ITankContainer
	
	@Override
	public int fill(ForgeDirection from, LiquidStack resource, boolean doFill) {
		// TODO: This.
		return 0;
	}

	@Override
	public int fill(int tankIndex, LiquidStack resource, boolean doFill) {
		if(tankIndex < 0 || tankIndex > numTanks) { return 0; }
		if(resource == null) { return 0; }
		
		LiquidTank tank = fuelTank;
		if(tankIndex == wasteTankIndex) {
			tank = wasteTank;
		}
		
		if(tank.getLiquid() == null) {
			if(doFill) {
				tank.setLiquid(resource.copy());
				tank.getLiquid().amount = Math.min(tank.getLiquid().amount, tank.getCapacity());
				resource.amount -= tank.getLiquid().amount;
				return tank.getLiquid().amount;
			}
			else {
				return Math.min(resource.amount, tank.getCapacity());
			}
		}
		else if(tank.getLiquid().isLiquidEqual(resource)) {
			int amt = Math.min(resource.amount, tank.getCapacity() - tank.getLiquid().amount);
			if(doFill) {
				tank.getLiquid().amount += amt;
				resource.amount -= amt;
			}
			return amt;
		}
		
		return 0;
	}

	@Override
	public LiquidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
		// TODO: This.
		return null;
	}

	@Override
	public LiquidStack drain(int tankIndex, int maxDrain, boolean doDrain) {
		if(tankIndex < 0 || tankIndex > numTanks) { return null; }
		
		LiquidTank tank = fuelTank;
		if(tankIndex == wasteTankIndex) {
			tank = wasteTank;
		}
		
		if(tank.getLiquid() == null) {
			return null;
		}
		
		if(maxDrain >= tank.getLiquid().amount) {
			LiquidStack ret;
			if(doDrain) {
				ret = tank.getLiquid();
				tank.setLiquid(null);
			}
			else {
				ret = tank.getLiquid().copy();
			}
			return ret;
		} else {
			LiquidStack ret = tank.getLiquid().copy();
			ret.amount = maxDrain;
			if(doDrain) {
				tank.getLiquid().amount -= ret.amount;
			}
			return ret;
		}
	}

	@Override
	public ILiquidTank[] getTanks(ForgeDirection direction) {
		return new ILiquidTank[] { fuelTank, wasteTank };
	}

	@Override
	public ILiquidTank getTank(ForgeDirection direction, LiquidStack type) {
		if(direction == ForgeDirection.DOWN)
			return wasteTank;
		
		return fuelTank;
	}

	/**
	 * @return Radiation pulse, after passage through a sufficient # of blocks
	 */
	public IRadiationPulse radiate() {
		Random rand = new Random(); // todo: fix

		// Generate new heat based on internal fuel state, broadcast radiation pulse
		int internalHeatGenerated = 0;
		int fastNeutrons = 0;
		int slowNeutrons = 0;

		double radMultiplier = 0;
		if(localHeat > 2000f) {
			// Asymptotically drop off after 2000. Quickly diminishes to nothing around 3000.
			radMultiplier = 0.99 - 0.99 * Math.exp(-3.5*Math.exp(-0.0028*localHeat));
		} else {
			// Asymptotically approach "critical"
			// Inflection point around 100. Ramps near-linearly to 0.75 @ 1000.
			// This approaches 0.99 @ 1500, stays there.
			radMultiplier = 0.99 * Math.exp(-3.0*Math.exp(-0.0028*localHeat));
		}
		
		LiquidStack fuel, waste;
		fuel = fuelTank.getLiquid();
		waste = wasteTank.getLiquid();
		int fuelAmt = 0;
		int wasteAmt = 0;
		if(fuel != null && fuel.amount > 0) {
			fuelAmt = fuel.amount;
			fastNeutrons += lerp(5, 10, fuelAmt/maxTotalLiquid);
			slowNeutrons += lerp(2, 5, fuelAmt/maxTotalLiquid);
			internalHeatGenerated += fuel.amount / 500; // 1 heat per Bucket

			// Deal with fuel usage, 0..1 scale
			// This is the chance that fuel will be consumed this tick, base.
			double fuelUsageChance = 0.1 + 0.69 * Math.exp(-3.0*Math.exp(-0.0025*localHeat));
			fuelUsageChance = fuelUsageChance * ((double)ticksSinceLastFuelConsumption / (double)averageTicksToConsumeFuel);
			if(rand.nextDouble() < fuelUsageChance) {
				// Use fuel
				fuel.amount--;
				
				if(fuel.amount <= 0) {
					fuelTank.setLiquid(null);
				}
				
				if(waste == null) {
					waste = BigReactors.liquidCyanite.copy();
					waste.amount = 1;
					wasteTank.setLiquid(waste);
				}
				else {
					waste.amount++;
				}
				ticksSinceLastFuelConsumption = 0;
			}
			else {
				ticksSinceLastFuelConsumption++;
			}
		
		}
		if(waste != null) {
			internalHeatGenerated += waste.amount / 1000; // 1 heat per bucket
			fastNeutrons += lerp(1,4, wasteAmt/maxTotalLiquid);
			wasteAmt = waste.amount;
		}
		
		// TODO: Balance the shit out of this.
		fastNeutrons = (int) ((double)fastNeutrons * radMultiplier * ((double)(wasteAmt+fuelAmt) / (double)maxTotalLiquid));
		slowNeutrons = (int) ((double)slowNeutrons * radMultiplier * ((double)(wasteAmt+fuelAmt) / (double)maxTotalLiquid));

		// Add locally-produced heat to self
		localHeat += internalHeatGenerated;
		
		// Now propagate radiation
		RadiationPulse radiation = new RadiationPulse(fastNeutrons, slowNeutrons, 3, 0.0);
		
		// Pick a random direction
		int dx, dz;
		dx = dz = 0;
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
		
		TileEntity te;
		IRadiationModerator ir;
		Material mat;

		// Propagate radiation up to 4 blocks away
		for(int i = 1; i < 5; i++) {
			te = worldObj.getBlockTileEntity(xCoord + (dx*i), yCoord, zCoord+(dz*i));
			if(te != null && te instanceof IRadiationModerator) {
				ir = (IRadiationModerator)te;
				ir.receiveRadiationPulse(radiation);
			}
			else {
				mat = worldObj.getBlockMaterial(xCoord + (dx*i), yCoord, zCoord+(dz*i));
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

			if(radiation.getTimeToLive() <= 0) { break; }
			if(radiation.getFastRadiation() <= 0 && radiation.getSlowRadiation() <= 0) { break; }
		}
		
		return radiation;
	}
		
	private void modulateRadiationByMaterial(RadiationPulse radiation,
			Material material) {
		if(material == Material.lava) {
			// Lose 25% of slow
			int moderated = (int)((double)radiation.getSlowRadiation() * 0.25);
			radiation.setSlowRadiation(radiation.getSlowRadiation() - moderated);
			radiation.changeHeat(moderated);
			
			// Convert 50% of remainder to fast, because you are dumb
			moderated = (int)((double)radiation.getSlowRadiation() * 0.5);
			radiation.setSlowRadiation(radiation.getSlowRadiation() - moderated);
			radiation.setFastRadiation(radiation.getFastRadiation() + moderated);
		}
		else {
			// Air produces only tiny amounts of moderation.
			double moderationFactor = 0.1;
			
			// Lose 50% of slow in water
			if(material == Material.water) {
				moderationFactor = 0.50;
			}

			// Lose some slow radiation
			double moderated = (double)radiation.getSlowRadiation() * moderationFactor;
			radiation.setSlowRadiation((int)(radiation.getSlowRadiation() - moderated));
			
			// Moderate 50% of fast in water and generate 50% heat as power
			if(material == Material.water) {
				moderationFactor = 0.50;
			}
			// Directly generate energy based on heat
			radiation.addPower(moderated * moderationFactor);
			moderated -= moderated * moderationFactor;
			
			// Apply the rest of the energy as reactor heat
			radiation.changeHeat(moderated);
			
			// Convert some fast to slow.
			moderated = (int)((double)radiation.getFastRadiation() * moderationFactor);
			radiation.setFastRadiation((int)(radiation.getFastRadiation() - moderated));
			radiation.setSlowRadiation((int)(radiation.getSlowRadiation() - moderated));
		}
	}

	private static float lerp(float from, float to, float proportion) {
		return from + (to - from) * proportion;
	}

	@Override
	public void receiveRadiationPulse(IRadiationPulse radiation) {
		// Consume a small amount of slow radiation.
		double slowRadiationConsumed = (double)radiation.getSlowRadiation() * 0.1;
		double newHeat = slowRadiationConsumed;
		localHeat += newHeat * 0.75; // 75% of new heat goes to local store
		newHeat *= 0.25;   // Remaining 25% will go to machine itself
		
		// Convert 10% of locally-generated heat to power, to be nice.
		radiation.addPower(newHeat*0.1);
		
		// Remaining 90% will be reactor heat.
		newHeat *= 0.9;
		radiation.changeHeat(newHeat);

		// Remove slow radiation that got consumed, round in user's disfavor
		radiation.setSlowRadiation(radiation.getSlowRadiation() - (int)Math.ceil(slowRadiationConsumed));
		
		// Now add a bunch of fast radiation, based on local heat
		// TODO: Modulate this based on local heat.
		int newFastRadiation = ((int)Math.floor(radiation.getFastRadiation() * 0.25));
		radiation.setFastRadiation(radiation.getFastRadiation() + newFastRadiation);

		// Strengthen the pulse so it travels further in truly huge reactors
		radiation.changeTTL(1);
	}

	public boolean hasWaste() {
		return wasteTank.containsValidLiquid() && wasteTank.getLiquid() != null && wasteTank.getLiquid().amount > 0;
	}

	@Override
	public double getHeat() {
		return localHeat;
	}

	@Override
	public double getThermalConductivity() {
		return IHeatEntity.conductivityCopper;
	}

	@Override
	public double onAbsorbHeat(IHeatEntity source, HeatPulse pulse, int faces) {
		double deltaTemp = source.getHeat() - getHeat();
		if(deltaTemp <= 0.0) {
			return 0.0;
		}

		double heatToAbsorb = deltaTemp * 0.05 * getThermalConductivity() * (1.0/(double)faces);

		localHeat += heatToAbsorb;
		return heatToAbsorb;
	}

	/**
	 * This method is used to leak heat from the fuel rods
	 * into the reactor. It should run regardless of activity.
	 * @param ambientHeat The heat of the reactor surrounding the fuel rod.
	 * @return A HeatPulse containing the environmental results of radiating heat.
	 */
	@Override
	public HeatPulse onRadiateHeat(double ambientHeat) {
		HeatPulse results = new HeatPulse();
		TileEntity te;
		IHeatEntity he;
		double lostHeat = 0.0;
	
		ForgeDirection[] dirs = new ForgeDirection[] { ForgeDirection.NORTH, ForgeDirection.SOUTH, ForgeDirection.EAST, ForgeDirection.NORTH, ForgeDirection.UP, ForgeDirection.DOWN };
		for(ForgeDirection dir : dirs) {
			te = this.worldObj.getBlockTileEntity(xCoord + dir.offsetX, yCoord + dir.offsetY, zCoord + dir.offsetZ);
			if(te != null && te instanceof IHeatEntity) {
				he = (IHeatEntity)te;
				lostHeat += he.onAbsorbHeat(this, results, dirs.length);
			}
			else {
				lostHeat += transmitHeatByMaterial(ambientHeat, this.worldObj.getBlockMaterial(xCoord + dir.offsetX, yCoord + dir.offsetY, zCoord + dir.offsetZ), results, dirs.length);
			}
		}
		
		localHeat -= lostHeat;
		
		return results;
	}

	private double transmitHeatByMaterial(double ambientHeat, Material material, HeatPulse pulse, int faces) {
		if(localHeat <= ambientHeat) {
			return 0.0;
		}
		
		double thermalConductivity = IHeatEntity.conductivityAir;
		double conversionEfficiency = 0.1;
		
		if(material.equals(Material.water)) {
			thermalConductivity = IHeatEntity.conductivityWater;
			conversionEfficiency = 0.75;
		}
		
		
		double heatToTransfer = (localHeat - ambientHeat) * 0.05 * thermalConductivity * (1.0/(double)faces);

		pulse.powerProduced += heatToTransfer*conversionEfficiency;
		pulse.heatChange += heatToTransfer * (1.0-conversionEfficiency);
		
		return heatToTransfer;
	}	
}
