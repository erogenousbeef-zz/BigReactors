package erogenousbeef.bigreactors.common.tileentity;

import java.io.DataInputStream;
import java.io.IOException;

import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import erogenousbeef.bigreactors.client.gui.GuiReactorControlRod;
import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.common.tileentity.base.TileEntityBeefBase;
import erogenousbeef.bigreactors.gui.container.ContainerReactorControlRod;
import erogenousbeef.bigreactors.net.PacketWrapper;
import erogenousbeef.bigreactors.net.Packets;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet132TileEntityData;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.liquids.ILiquidTank;
import net.minecraftforge.liquids.ITankContainer;
import net.minecraftforge.liquids.LiquidContainerRegistry;
import net.minecraftforge.liquids.LiquidStack;
import net.minecraftforge.liquids.LiquidTank;

public class TileEntityReactorControlRod extends TileEntityBeefBase {
	public final static int maxTotalLiquidPerBlock = LiquidContainerRegistry.BUCKET_VOLUME * 4;
	public final static int maxFuelRodsBelow = 32;
	public final static short maxInsertion = 100;
	public final static short minInsertion = 0;

	public final static int fuelTankIndex = 0;
	public final static int wasteTankIndex = 1;
	public final static int numTanks = 2;

	protected boolean isAssembled = false;
	protected boolean tryAssembleOnNextFrame = true;
	
	// 1 ingot = 1 bucket = 1000 internal fuel
	public static final int fuelPerIngot = 1000;
	public static final int fuelPerBucket = 1000;

	protected ItemStack fuelItem;
	protected int fuelAmount;
	
	protected ItemStack wasteItem;
	protected int wasteAmount;

	protected double localHeat;
	protected int ticksSinceLastFuelConsumption;
	protected static final int averageTicksToConsumeFuel = 1200; // 60 secs (20 ticks / sec)

	protected int minFuelRodY;
	protected short controlRodInsertion; // 0 = retracted fully, 100 = inserted fully
	
	public TileEntityReactorControlRod() {
		super();
	
		isAssembled = false;

		fuelItem = null;
		fuelAmount = 0;

		wasteItem = null;
		wasteAmount = 0;

		localHeat = 0.0;
		ticksSinceLastFuelConsumption = 0;
		minFuelRodY = 0;
		controlRodInsertion = minInsertion;
	}

	public ItemStack getFuelType() {
		if(fuelItem == null) { return null; }
		else {
			return fuelItem.copy();
		}
	}
	
	public ItemStack getWasteType() {
		if(fuelItem == null) {
			return null;
		} else {
			return wasteItem.copy();
		}
	}
	
	public boolean isFull() {
		return wasteAmount + fuelAmount >= getSizeOfFuelTank();
	}
	
	public boolean isEmpty() {
		return wasteAmount + fuelAmount <= 0;
	}
	
	public int getSizeOfFuelTank() {
		if(!this.isAssembled) { return 0; }
		else {
			return maxTotalLiquidPerBlock * (yCoord - minFuelRodY);
		}
	}
	
	/**
	 * Attempt to add some fuel to the fuel rod.
	 * 
	 * @param fuelType An itemstack containing the type of fuel to add.
	 * @param amount The amount of fuel to add, in internal units (1 ingot = 1000)
	 * @param doAdd If true, actually adds the amount to the internal store. If false, just calculates the amount to add and returns that.
	 * @return Returns the amount of fuel added to this rod (or that would have been added, if doAdd is false)
	 */
	public int addFuel(ItemStack fuelType, int amount, boolean doAdd) {
		if(fuelType == null) {
			return 0;
		}
		
		int amountToAdd = 0;
		if(this.fuelItem != null) {
			if(!this.fuelItem.isItemEqual(fuelType)) {
				return 0;
			}
			
			amountToAdd = Math.min(amount, getSizeOfFuelTank() - (wasteAmount+fuelAmount));
			if(doAdd) {
				this.fuelAmount += amountToAdd;
			}
		}
		else {
			if(!this.isAcceptedFuel(fuelType)) {
				return 0;
			}
			
			amountToAdd = Math.min(amount, getSizeOfFuelTank() - (wasteAmount+fuelAmount));
			
			if(amountToAdd <= 0) {
				return 0;
			}

			if(doAdd) {
				this.fuelItem = fuelType.copy();
				this.fuelAmount = amountToAdd;
			}
		}
		
		if(amountToAdd > 0 && doAdd) {
			this.worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		}

		return amountToAdd;
	}

	public boolean isAcceptedFuel(ItemStack candidateFuel) {
		// TODO: make this suck less, use the registry
		if(candidateFuel.itemID == BigReactors.ingotGeneric.itemID) {
			return candidateFuel.getItemDamage() == 0 || candidateFuel.getItemDamage() == 2;
		}
		
		return false;
	}
	
	/**
	 * Attempt to remove fuel from this rod.
	 * @param fuelType The type of fuel to remove, or null for whatever's in there.
	 * @param amount The amount of fuel to remove.
	 * @param doRemove If true, actually removes the fuel. Otherwise, just calculates how much can be removed.
	 * @return The amount of fuel removed; only actually removed if doRemove is set to true.
	 */
	public int removeFuel(ItemStack fuelType, int amount, boolean doRemove) {
		if(fuelAmount <= 0 || amount <= 0) {
			return 0;
		}
		
		if(fuelType == null || this.fuelItem.isItemEqual(fuelType)) {
			int amtToRemove = Math.min(amount, fuelAmount);
			if(doRemove) {
				fuelAmount -= amount;
				if(fuelAmount <= 0) {
					fuelAmount = 0;
					this.fuelItem = null;
				}
			}
			
			if(amtToRemove > 0 && doRemove) {
				this.worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
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
	public int addWaste(ItemStack wasteType, int amount, boolean doAdd) {
		if(wasteType == null) {
			return 0;
		}
		
		int amountToAdd = 0;
		if(this.wasteItem != null) {
			if(!this.wasteItem.isItemEqual(wasteType)) {
				return 0;
			}
			
			amountToAdd = Math.min(amount, getSizeOfFuelTank() - (wasteAmount+fuelAmount));
			if(doAdd) {
				this.wasteAmount += amountToAdd;
			}
		}
		else {
			if(!this.isAcceptedWaste(wasteType)) {
				return 0;
			}
			
			amountToAdd = Math.min(amount, getSizeOfFuelTank() - (wasteAmount+fuelAmount));
			
			if(amountToAdd <= 0) {
				return 0;
			}

			if(doAdd) {
				this.wasteItem = wasteType.copy();
				this.wasteAmount = amountToAdd;
			}
		}
		
		if(amountToAdd > 0 && doAdd) {
			this.worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		}

		return amountToAdd;
	}

	public boolean isAcceptedWaste(ItemStack candidateWaste) {
		// TODO: make this suck less, use the registry
		if(candidateWaste.itemID == BigReactors.ingotGeneric.itemID) {
			return candidateWaste.getItemDamage() == 1;
		}
		
		return false;
	}
	
	/**
	 * Attempt to remove fuel from this rod.
	 * @param wasteType The type of fuel to remove, or null for whatever's in there.
	 * @param amount The amount of fuel to remove.
	 * @param doRemove If true, actually removes the fuel. Otherwise, just calculates how much can be removed.
	 * @return The amount of fuel removed; only actually removed if doRemove is set to true.
	 */
	public int removeWaste(ItemStack wasteType, int amount, boolean doRemove) {
		if(wasteAmount <= 0 || amount <= 0) {
			return 0;
		}
		
		if(wasteType == null || this.wasteItem.isItemEqual(wasteType)) {
			int amtToRemove = Math.min(amount, wasteAmount);
			if(doRemove) {
				wasteAmount -= amount;
				if(wasteAmount <= 0) {
					wasteAmount = 0;
					this.wasteItem = null;
				}
			}
			
			if(amtToRemove > 0 && doRemove) {
				this.worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
			}
			return amtToRemove;
		}

		return 0;
	}
	
	@Override
	public void updateEntity() {
		super.updateEntity();
		
		if(this.tryAssembleOnNextFrame) {
			tryAssembleOnNextFrame = false;
			tryAssemble();
		}
	}
	
	// Save/Load
	@Override
	public void readFromNBT(NBTTagCompound data) {
		super.readFromNBT(data);
		
		if(data.hasKey("localHeat")) {
			this.localHeat = data.getDouble("localHeat");
		}
		
		if(data.hasKey("ticksSinceLastFuelConsumption")) {
			this.ticksSinceLastFuelConsumption = data.getInteger("ticksSinceLastFuelConsumption");
		}
		
		this.fuelAmount = 0;
		this.fuelItem = null;
		if(data.hasKey("fuelAmount") && data.hasKey("fuelData")) {
			this.fuelAmount = data.getInteger("fuelAmount");
			this.fuelItem = ItemStack.loadItemStackFromNBT(data.getCompoundTag("fuelData"));
		}

		this.wasteAmount = 0;
		this.wasteItem = null;
		if(data.hasKey("wasteAmount") && data.hasKey("wasteData")) {
			this.wasteAmount = data.getInteger("wasteAmount");
			this.wasteItem = ItemStack.loadItemStackFromNBT(data.getCompoundTag("wasteData"));
		}
		
		if(data.hasKey("isAssembled")) {
			// Can't do this straight-away on chunk load, unfortunately
			this.tryAssembleOnNextFrame = data.getBoolean("isAssembled");
		}
		
		if(data.hasKey("controlRodInsertion")) {
			this.controlRodInsertion = data.getShort("controlRodInsertion");
		}
	}
	
	@Override
	public void writeToNBT(NBTTagCompound data) {
		super.writeToNBT(data);
		
		data.setDouble("localHeat", this.localHeat);
		data.setInteger("ticksSinceLastFuelConsumption", this.ticksSinceLastFuelConsumption);
		data.setBoolean("isAssembled", this.isAssembled);
		data.setShort("controlRodInsertion", this.controlRodInsertion);
		
		if(this.fuelItem != null && this.fuelAmount > 0) {
			NBTTagCompound fuelData = new NBTTagCompound();
			this.fuelItem.writeToNBT(fuelData);
			data.setInteger("fuelAmount", fuelAmount);
			data.setCompoundTag("fuelData", fuelData);
		}
		
		if(this.wasteItem != null && this.wasteAmount > 0) {
			NBTTagCompound wasteData = new NBTTagCompound();
			this.wasteItem.writeToNBT(wasteData);
			data.setInteger("wasteAmount", wasteAmount);
			data.setCompoundTag("wasteData", wasteData);
		}
	}
	
	public void tryAssemble() {
		if(isAssembled) { return; } // TODO: ???

		// Look for at least one fuel rod beneath us
		minFuelRodY = this.yCoord - 1;
		int blocksChecked = 0;
		while(this.worldObj.getBlockId(xCoord, minFuelRodY, zCoord) == BigReactors.blockYelloriumFuelRod.blockID && blocksChecked <= maxFuelRodsBelow) {
			blocksChecked++;
			minFuelRodY--;
		}
		
		minFuelRodY++;
		
		// If we end up back at ourself, we can't assemble.
		if(minFuelRodY == this.yCoord) {
			this.onDisassembled();
		}
		else {
			this.onAssembled();
		}
	}

	private void onAssembled() {
		// TODO: Change all fuel rods beneath us to "assembled" state

		if(!this.isAssembled) {
			this.isAssembled = true;
			System.out.println("System assembled");
			sendControlRodUpdate();
		}
	}

	private void onDisassembled() {
		// TODO: Change all fuel rods beneath us to "disassembled" state
		if(this.isAssembled) {
			this.isAssembled = false;
			System.out.println("System disassembled");
			sendControlRodUpdate();
		}
	}

	// TileEntityBeefBase
	
	@Override
	public GuiScreen getGUI(EntityPlayer player) {
		return new GuiReactorControlRod(getContainer(player), this);
	}

	@Override
	public Container getContainer(EntityPlayer player) {
		return new ContainerReactorControlRod(this, player);
	}

	@Override
	protected void onReceiveGuiButtonPress(String buttonName,
			DataInputStream dataStream) throws IOException {
		System.out.println("onReceiveGuiButtonPress::" + buttonName);
		if(buttonName.equals("assemble")) {
			if(!this.isAssembled) {
				tryAssemble();
			}
			else {
				this.onDisassembled();
			}
		}
		else if(buttonName.equals("rodInsert")) {
			setControlRodInsertion((short)(this.controlRodInsertion + 10));
		}
		else if(buttonName.equals("rodRetract")) {
			setControlRodInsertion((short)(this.controlRodInsertion - 10));
		}
		else if(buttonName.equals("dump")) {
			this.fuelAmount = 0;
			this.fuelItem = null;
			this.wasteAmount = 0;
			this.wasteItem = null;
			this.worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		}
	}

	public int getFuelAmount() {
		if(this.fuelItem == null) { return 0; }
		return this.fuelAmount;
	}

	public int getWasteAmount() {
		if(this.wasteItem == null) { return 0; }
		return this.wasteAmount;
	}

	public int getTotalContainedAmount() {
		return this.getFuelAmount() + this.getWasteAmount();
	}

	public double getHeat() {
		return this.localHeat;
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
	
	// Updates
	protected void sendControlRodUpdate() {
		if(this.worldObj == null || this.worldObj.isRemote) { return; }

		Packet p = PacketWrapper.createPacket(BigReactors.CHANNEL, Packets.ControlRodUpdate,
				new Object[] { xCoord, yCoord, zCoord, isAssembled, minFuelRodY, controlRodInsertion });
		
		PacketDispatcher.sendPacketToAllAround(xCoord, yCoord, zCoord, 50, worldObj.provider.dimensionId, p);
	}
	
	@SideOnly(Side.CLIENT)
	public void onControlRodUpdate(boolean isAssembled, int minFuelRodY, short controlRodInsertion) {
		this.minFuelRodY = minFuelRodY;
		if(this.isAssembled != isAssembled) {
			if(isAssembled) {
				onAssembled();
			} else {
				onDisassembled();
			}
		}

		this.controlRodInsertion = controlRodInsertion;
	}

	public int getColumnHeight() {
		return yCoord - minFuelRodY;
	}
	
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox()
    {
    	if(!this.isAssembled || this.getColumnHeight() < 1) {
    		return super.getRenderBoundingBox();
    	}

    	return AxisAlignedBB.getAABBPool().getAABB(xCoord, yCoord - getColumnHeight(), zCoord, xCoord + 1, yCoord + 1, zCoord + 1);
    }
}
