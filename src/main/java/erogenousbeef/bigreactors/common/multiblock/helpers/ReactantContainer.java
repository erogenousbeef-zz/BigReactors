package erogenousbeef.bigreactors.common.multiblock.helpers;

import cpw.mods.fml.common.network.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import erogenousbeef.bigreactors.api.registry.Reactants;
import erogenousbeef.bigreactors.common.BRLog;
import erogenousbeef.bigreactors.common.data.ReactantStack;
import erogenousbeef.bigreactors.common.multiblock.interfaces.IConditionalUpdater;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

/**
 * A helper class which allows multiple reactants to sit in one logical
 * container. It acts basically like a single tank containing multiple things,
 * and limiting the amount of stuff which can go into it based on the total
 * amount of stuff contained.
 * 
 * @author Erogenous Beef
 */
public abstract class ReactantContainer implements IConditionalUpdater {

	private ReactantStack[] tanks;
	private int capacity;
	private String[] tankNames;

	private int ticksSinceLastUpdate;
	private static final int minimumTicksBetweenUpdates = 60;
	private static final int minimumDevianceForUpdate = 50; // at least 50mB difference before we send a fueling update to the client

	int[] levelAtLastUpdate;

	private static final int FORCE_UPDATE = -1000;
	
	public ReactantContainer(String[] tankNames, int capacity) {
		assert(tankNames != null);
		assert(tankNames.length > 0);

		this.tankNames = tankNames;
		tanks = new ReactantStack[tankNames.length];
		levelAtLastUpdate = new int[tankNames.length];

		for(int i = 0; i < tanks.length; i++) {
			tanks[i] = null;
			levelAtLastUpdate[i] = FORCE_UPDATE;
		}
		
		this.capacity = capacity;
	}

	public int getCapacity() { return capacity; }

	public void setCapacity(int newCapacity) {
		int oldCapacity = capacity;
		capacity = newCapacity;

		clampContentsToCapacity();
	}	

	/// GETTERS
	public String getReactantType(int reactantIdx) {
		assert(reactantIdx >= 0 && reactantIdx < tanks.length);
		return tanks[reactantIdx] == null ? null : tanks[reactantIdx].getName();
	}
	
	public int getReactantAmount(int reactantIdx) {
		assert(reactantIdx >= 0 && reactantIdx < tanks.length);
		return tanks[reactantIdx] == null ? 0 : tanks[reactantIdx].amount;
	}
	
	/**
	 * @return Total amount of stuff contained, across all fluid tanks
	 */
	public int getTotalAmount() {
		int amt = 0;
		for(int i = 0; i < tanks.length; i++) {
			amt += getReactantAmount(i);
		}
		return amt;
	}
	
	/// SETTERS
	public void setReactant(int reactantIdx, ReactantStack newStack ) {
		assert(reactantIdx >= 0 && reactantIdx < tanks.length);
		tanks[reactantIdx] = newStack;
	}
	
	/// ADD/REMOVE HELPERS
	protected int addToStack(int idx, int fluidAmount) {
		if(tanks[idx] == null) {
			throw new IllegalArgumentException("Cannot add reactant with only an integer when tank is empty!");
		}
		
		int amtToAdd = Math.min(fluidAmount, getRemainingSpace());
		
		tanks[idx].amount += amtToAdd;
		return amtToAdd;
	}

	public int fill(int idx, String reactantName, int amount, boolean doFill) {
		assert(idx >= 0 && idx < tanks.length);
		if(reactantName == null || amount < 0) { return 0; }
		
		if(!canAddToStack(idx, reactantName)) { return 0; }
		
		int amtToAdd = Math.min(amount, getRemainingSpace());
		if(amtToAdd <= 0) { return 0; }
		if(!doFill) { return amtToAdd; }
		
		if(tanks[idx] == null) {
			tanks[idx] = new ReactantStack(reactantName, amtToAdd);
		}
		else {
			tanks[idx].amount += amtToAdd;
		}
		
		return amtToAdd;
	}
	
	public int fill(int idx, ReactantStack incoming, boolean doFill) {
		if(incoming == null) { return 0; }
		
		return fill(idx, incoming.getName(), incoming.amount, doFill);
	}
	
	/**
	 * Dump everything in a given reactant tank.
	 * @param idx Index of the tank to dump
	 */
	public int dump(int idx) {
		assert(idx >= 0 && idx < tanks.length);

		int amt = tanks[idx] != null ? tanks[idx].amount : 0;
		setReactant(idx, null);
		
		return amt;
	}
	
	/**
	 * Remove some amount of reactant, but don't bother returning it.
	 * We're just dumping it into the ether.
	 * @param idx Index of the tank to dump from
	 * @param amount Maximum amount to dump
	 */
	public int dump(int idx, int amount) {
		assert(idx >= 0 && idx < tanks.length);
		if(tanks[idx] != null) {
			if(tanks[idx].amount <= amount) {
				amount = tanks[idx].amount;
				setReactant(idx, null);
			}
			else {
				tanks[idx].amount -= amount;
			}
			return amount;
		}
		else {
			return 0;
		}
	}

	/// VALIDATION HELPERS
	protected abstract boolean isReactantValidForStack(int stackIdx, String reactant);

	protected boolean canAddToStack(int idx, String incoming) {
		if(idx < 0 || idx >= tanks.length || incoming == null) { return false; }
		else if(tanks[idx] == null) {return isReactantValidForStack(idx, incoming); }
		return tanks[idx].isReactantEqual(incoming);
	}
	
	public boolean canDrain(int idx, ReactantStack reactant) {
		if(reactant == null || idx < 0 || idx >= tanks.length) { return false; }

		if(tanks[idx] == null) { return false; }
		
		return tanks[idx].isReactantEqual(reactant) && tanks[idx].amount > 0;
	}

	/// SAVE/LOAD
	
	protected NBTTagCompound writeToNBT(NBTTagCompound destination) {
		ReactantStack stack;
		for(int i = 0; i < tankNames.length; i++) {
			stack = tanks[i];
			if(stack != null) {
				destination.setTag(tankNames[i], stack.writeToNBT(new NBTTagCompound()));
			}
		}
		
		return destination;
	}
	
	protected void readFromNBT(NBTTagCompound data) {
		for(int i = 0; i < tankNames.length; i++) {
			if(data.hasKey(tankNames[i])) {
				tanks[i] = ReactantStack.createFromNBT(data.getCompoundTag(tankNames[i]));
				levelAtLastUpdate[i] = tanks[i].amount;
			}
			else {
				tanks[i] = null;
				levelAtLastUpdate[i] = 0;
			}
		}
	}
	
	public void serialize(ByteBuf buffer) {
		for(int i = 0; i < tankNames.length; i++) {
			if(getReactantAmount(i) <= 0) {
				buffer.writeBoolean(false);
			}
			else {
				ByteBufUtils.writeUTF8String(buffer, tanks[i].getName());
				buffer.writeInt(tanks[i].amount);
			}
		}
	}
	
	public void deserialize(ByteBuf buffer) {
		for(int i = 0; i < tankNames.length; i++) {
			boolean hasData = buffer.readBoolean();
			if(hasData) {
				String reactantName = ByteBufUtils.readUTF8String(buffer);
				int amount = buffer.readInt();
				
				if(!Reactants.isKnown(reactantName)) {
					BRLog.warning("Read an unknown reactant <%s> from a network message; tank %s will remain empty", reactantName, tankNames[i]);
				}
				else {
					tanks[i] = new ReactantStack(reactantName, amount);
					levelAtLastUpdate[i] = amount;
				}
			}
		}
	}

	/// MULTIBLOCK HELPERS
	protected void merge(ReactantContainer other) {
		if(other.capacity > capacity) {
			capacity = other.capacity;
			tanks = other.tanks;
		}
	}
	
	/// Implementation: IConditionalUpdater
	public boolean shouldUpdate() {
		ticksSinceLastUpdate++;

		if(minimumTicksBetweenUpdates < ticksSinceLastUpdate) {
			int dev = 0;
			boolean shouldUpdate = false;
			for(int i = 0; i < tanks.length && !shouldUpdate; i++) {
				
				if(tanks[i] == null && levelAtLastUpdate[i] > 0) {
					shouldUpdate = true;
				}
				else if(tanks[i] != null) {
					if(levelAtLastUpdate[i] == FORCE_UPDATE) {
						shouldUpdate = true;
					}
					else {
						dev += Math.abs(tanks[i].amount - levelAtLastUpdate[i]);
					}
				}
				// else, both levels are zero, no-op
				
				if(dev >= minimumDevianceForUpdate) {
					shouldUpdate = true;
				}
			}
			
			if(shouldUpdate) {
				resetLastSeenLevels();
			}
			
			ticksSinceLastUpdate = 0;
			return shouldUpdate;
		}
		
		return false;
	}

	/// DATA HELPERS
	protected void resetLastSeenLevels() {
		for(int i = 0; i < tanks.length; i++) {
			if(tanks[i] == null) {
				levelAtLastUpdate[i] = 0;
			}
			else {
				levelAtLastUpdate[i] = tanks[i].amount;
			}
		}
	}

	/**
	 * @return The amount of space remaining in the container.
	 */
	public int getRemainingSpace() {
		return getCapacity() - getTotalAmount();
	}
	
	/**
	 * When a container is overfilled, forcibly "spill" some reactants
	 * to return to a non-overfull state.
	 * This is most often useful when dealing with machine merges.
	 */
	protected void clampContentsToCapacity() {
		if(getTotalAmount() > capacity) {
			int diff = getTotalAmount() - capacity;
			
			// Reduce stuff in the tanks. Start with waste, to be nice to players.
			for(int i = tanks.length - 1; i >= 0 && diff > 0; i--) {
				if(tanks[i] != null) {
					if(diff > tanks[i].amount) {
						diff -= tanks[i].amount;
						tanks[i] = null;
					}
					else {
						tanks[i].amount -= diff;
						diff = 0;
					}
				}
			}
		}
	}
}
