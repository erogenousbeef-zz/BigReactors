package erogenousbeef.bigreactors.common.tileentity;

import java.util.Random;

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

public class TileEntityFuelRod extends TileEntity implements ITankContainer, IRadiationModerator {

	public final static int maxTotalLiquid = LiquidContainerRegistry.BUCKET_VOLUME * 4;
	
	public final static int fuelTankIndex = 0;
	public final static int wasteTankIndex = 1;
	public final static int numTanks = 2;
	
	// 1 ingot = 1 bucket
	protected LiquidTank fuelTank = new LiquidTank(maxTotalLiquid);
	protected LiquidTank wasteTank = new LiquidTank(maxTotalLiquid);

	protected double localHeat;
	protected int ticksSinceLastFuelConsumption;
	protected final int averageTicksToConsumeFuel = 40;
	
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

	public void radiate() {
		Random rand = new Random(); // todo: fix

		// Generate new heat based on internal fuel state, broadcast radiation pulse
		int heatGenerated = 0;
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
			fastNeutrons += lerp(10,20, fuelAmt/maxTotalLiquid);
			slowNeutrons += lerp(5, 10, fuelAmt/maxTotalLiquid);
			heatGenerated += fuel.amount / 100; // 1 heat per 100mB

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
			heatGenerated += waste.amount / 1000; // 1 heat per bucket
			fastNeutrons += lerp(1,4, wasteAmt/maxTotalLiquid);
			wasteAmt = waste.amount;
		}
		
		// TODO: Balance the shit out of this.
		//fastNeutrons = (int) ((double)fastNeutrons * radMultiplier * 4*(wasteAmt+fuelAmt / (double)maxTotalLiquid));
		//slowNeutrons = (int) ((double)slowNeutrons * radMultiplier * 4*(wasteAmt+fuelAmt / (double)maxTotalLiquid));

		
		// Now propagate radiation
		RadiationPulse radiation = new RadiationPulse(fastNeutrons, slowNeutrons, 3);
		
		// Hit ourself first.
		int heatProduced = 0;
		
		heatProduced += receivePulse(radiation);

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
				heatProduced += ir.receivePulse(radiation);
			}
			else {
				mat = worldObj.getBlockMaterial(xCoord + (dx*i), yCoord, zCoord+(dz*i));
				if(mat != null) {
					heatProduced += modulateRadiationByMaterial(radiation, mat);
				}
				else {
					// Durr..?
					heatProduced += modulateRadiationByMaterial(radiation, Material.air);
				}
			}
			
			if(radiation.getFastRadiation() <= 0 && radiation.getSlowRadiation() <= 0) { break; }
		}
		
		// Modulate local heat. Dump it into the reactor.
		heatProduced += (int)localHeat * 0.01;
		
		// In the case of cool fuel rods, some heat will be "lost".
		localHeat -= Math.max(1.0, localHeat * 0.01);
		
		// Now dump heat into the reactor
		int dy = 1;
		te = worldObj.getBlockTileEntity(xCoord, yCoord+dy, zCoord);
		while(te != null && !(te instanceof TileEntityReactorPart)) {
			dy++;
			te = worldObj.getBlockTileEntity(xCoord, yCoord+dy, zCoord);
		}
		if(te instanceof TileEntityReactorPart) {
			((TileEntityReactorPart)te).addHeat(heatProduced);
		}
		
	}	
	
	private int modulateRadiationByMaterial(RadiationPulse radiation,
			Material material) {
		int heatProduced = 0;
		
		if(material == Material.lava) {
			// Lose 25% of slow
			int moderated = (int)((double)radiation.getSlowRadiation() * 0.25);
			radiation.setSlowRadiation(radiation.getSlowRadiation() - moderated);
			heatProduced = moderated;
			
			// Convert 50% of remainder to fast, because you are dumb
			moderated = (int)((double)radiation.getSlowRadiation() * 0.5);
			radiation.setSlowRadiation(radiation.getSlowRadiation() - moderated);
			radiation.setFastRadiation(radiation.getFastRadiation() + moderated);
		}
		else {
			double moderationFactor = 0.1;
			
			// Lose 25% of slow in water
			if(material == Material.water) {
				moderationFactor = 0.25;
			}

			// Lose some slow radiation
			int moderated = (int)((double)radiation.getSlowRadiation() * moderationFactor);
			radiation.setSlowRadiation(radiation.getSlowRadiation() - moderated);
			heatProduced = moderated;
			
			// Moderate 50% of fast in water
			if(material == Material.water) {
				moderationFactor = 0.50;
			}

			// Convert some fast to slow.
			moderated = (int)((double)radiation.getFastRadiation() * moderationFactor);
			radiation.setFastRadiation(radiation.getFastRadiation() - moderated);
			radiation.setSlowRadiation(radiation.getSlowRadiation() - moderated);
		}
		
		// Reduce TTL
		radiation.changeTTL(-1);
		return heatProduced;
	}

	private static float lerp(float from, float to, float proportion) {
		return from + (to - from) * proportion;
	}

	@Override
	public int receivePulse(IRadiationPulse radiation) {
		double slowRadiationConsumed = (double)radiation.getSlowRadiation() / 2.0;
		double newHeat = slowRadiationConsumed;
		localHeat += newHeat * 0.75; // 75% of new heat goes to local store
		newHeat -= newHeat * 0.75;   // Remaining 25% will go to machine itself

		// Remove slow radiation that got consumed, round in user's disfavor
		radiation.setSlowRadiation(radiation.getSlowRadiation() - (int)Math.ceil(slowRadiationConsumed));
		
		// Now add a bunch of fast radiation, based on local heat
		// TODO: Modulate this based on local heat.
		int newFastRadiation = ((int)Math.floor(radiation.getFastRadiation() * 0.25));
		radiation.setFastRadiation(radiation.getFastRadiation() + newFastRadiation);

		// Do not change TTL. Assume the radiation got "strengthened" by going through the rod.
		return (int)newHeat;
	}

	public boolean hasWaste() {
		return wasteTank.containsValidLiquid() && wasteTank.getLiquid() != null && wasteTank.getLiquid().amount > 0;
	}
	
}
