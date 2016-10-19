package erogenousbeef.bigreactors.common.multiblock.tileentity;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import cofh.lib.util.helpers.BlockHelper;
import cofh.lib.util.helpers.ItemHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import erogenousbeef.bigreactors.api.data.SourceProductMapping;
import erogenousbeef.bigreactors.api.registry.Reactants;
import erogenousbeef.bigreactors.client.gui.GuiReactorAccessPort;
import erogenousbeef.bigreactors.common.BRLog;
import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.common.data.StandardReactants;
import erogenousbeef.bigreactors.common.multiblock.interfaces.INeighborUpdatableEntity;
import erogenousbeef.bigreactors.gui.container.ContainerReactorAccessPort;
import erogenousbeef.bigreactors.utils.AdjacentInventoryHelper;
import erogenousbeef.core.multiblock.MultiblockControllerBase;

public class TileEntityReactorAccessPort extends TileEntityReactorPart implements ISidedInventory, INeighborUpdatableEntity {

	protected ItemStack[] _inventories;
	protected boolean isInlet;
	protected AdjacentInventoryHelper adjacencyHelper;

	public static final int SLOT_INLET = 0;
	public static final int SLOT_OUTLET = 1;
	public static final int NUM_SLOTS = 2;

	private static final int[] kInletExposed = {SLOT_INLET};
	private static final int[] kOutletExposed = {SLOT_OUTLET};

	public TileEntityReactorAccessPort() {
		super();

		_inventories = new ItemStack[getSizeInventory()];
		isInlet = true;
	}

	// Return the name of the reactant to which the item in the input slot
	public String getInputReactantType() {
		ItemStack inputItem = getStackInSlot(SLOT_INLET);
		if(inputItem == null) { return null; }
		SourceProductMapping mapping = Reactants.getSolidToReactant(inputItem);
		return mapping != null ? mapping.getProduct() : null;
	}

	// Returns the potential amount of reactant which can be produced from this port.
	public int getInputReactantAmount() {
		ItemStack inputItem = getStackInSlot(SLOT_INLET);
		if(inputItem == null) { return 0; }

		SourceProductMapping mapping = Reactants.getSolidToReactant(inputItem);
		return mapping != null ? mapping.getProductAmount(inputItem.stackSize) : 0;
	}

	/**
	 * Consume items from the input slot.
	 * Returns the amount of reactant produced.
	 * @param reactantDesired The amount of reactant desired, in reactant units (mB)
	 * @return The amount of reactant actually produced, in reactant units (mB)
	 */
	public int consumeReactantItem(int reactantDesired) {
		ItemStack inputItem = getStackInSlot(SLOT_INLET);
		if(inputItem == null) { return 0; }

		SourceProductMapping mapping = Reactants.getSolidToReactant(inputItem);
		if(mapping == null) { return 0; }

		int sourceItemsToConsume = Math.min(inputItem.stackSize, mapping.getSourceAmount(reactantDesired));

		if(sourceItemsToConsume <= 0) { return 0; }

		decrStackSize(SLOT_INLET, sourceItemsToConsume);
		return mapping.getProductAmount(sourceItemsToConsume);
	}

	/**
	 * Try to emit a given amount of reactant as a solid item.
	 * Will either match the item type already present, or will select
	 * whatever type allows the most reactant to be ejected right now.
	 * @param reactantType Type of reactant to emit.
	 * @param amount
	 * @return
	 */
	public int emitReactant(String reactantType, int amount) {
		if(reactantType == null || amount <= 0) { return 0; }

		ItemStack outputItem = getStackInSlot(SLOT_OUTLET);
		if(outputItem != null && outputItem.stackSize >= getInventoryStackLimit()) {
			// Already full?
			return 0;
		}

		// If we have an output item, try to produce more of it, given its mapping
		if(outputItem != null) {
			// Find matching mapping
			SourceProductMapping mapping = Reactants.getSolidToReactant(outputItem);
			if(mapping == null || !reactantType.equals(mapping.getProduct())) {
				// Items are incompatible!
				return 0;
			}

			// We're using the original source item >> reactant mapping here
			// This means that source == item, and product == reactant
			int amtToProduce = mapping.getSourceAmount(amount);
			amtToProduce = Math.min(amtToProduce, getInventoryStackLimit() - outputItem.stackSize);
			if(amtToProduce <= 0) {	return 0; }

			// Do we actually produce any reactant at this reduced amount?
			int reactantToConsume = mapping.getProductAmount(amtToProduce);
			if(reactantToConsume <= 0) { return 0; }

			outputItem.stackSize += amtToProduce;
			onItemsReceived();

			return reactantToConsume;
		}

		// Ok, we have no items. We need to figure out candidate mappings.
		// Below here, we're using the reactant >> source mappings.
		// This means that source == reactant, and product == item.
		SourceProductMapping bestMapping = null;

		List<SourceProductMapping> mappings = Reactants.getReactantToSolids(reactantType);
		if(mappings != null) {
			int bestReactantAmount = 0;
			for(SourceProductMapping mapping: mappings) {
				// How much product can we produce?
				int potentialProducts = mapping.getProductAmount(amount);

				// And how much reactant will that consume?
				int potentialReactant = mapping.getSourceAmount(potentialProducts);

				if(bestMapping == null || bestReactantAmount < potentialReactant) {
					bestMapping = mapping;
					bestReactantAmount = potentialReactant;
				}
			}
		}

		if(bestMapping == null) {
			BRLog.warning("There are no mapped item types for reactant %s. Using cyanite instead.", reactantType);
			bestMapping = StandardReactants.cyaniteMapping;
		}

		int itemsToProduce = Math.min(bestMapping.getProductAmount(amount), getInventoryStackLimit());
		if(itemsToProduce <= 0) {
			// Can't produce even one ingot? Ok then.
			return 0;
		}

		// And clamp again in case we could produce more than 64 items
		int reactantConsumed = bestMapping.getSourceAmount(itemsToProduce);
		itemsToProduce = bestMapping.getProductAmount(reactantConsumed);

		ItemStack newItem = ItemHelper.getOre(bestMapping.getProduct());
		if(newItem == null) {
			BRLog.warning("Could not find item for oredict entry %s, using cyanite instead.", bestMapping.getSource());
			newItem = BigReactors.ingotGeneric.getItemStackForType("ingotCyanite");
		}
		else {
			newItem = newItem.copy(); // Don't stomp the oredict
		}

		newItem.stackSize = itemsToProduce;
		setInventorySlotContents(SLOT_OUTLET, newItem);
		onItemsReceived();

		return reactantConsumed;
	}

	// Multiblock overrides
	@Override
	public void onMachineAssembled(MultiblockControllerBase controller) {
		super.onMachineAssembled(controller);

		adjacencyHelper = new AdjacentInventoryHelper(this.getOutwardsDir());
		checkForAdjacentInventories();
	}

	@Override
	public void onMachineBroken() {
		super.onMachineBroken();
		adjacencyHelper = null;
	}

	// TileEntity overrides

	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
		_inventories = new ItemStack[getSizeInventory()];
		if(tag.hasKey("Items")) {
			NBTTagList tagList = tag.getTagList("Items", 10);
			for(int i = 0; i < tagList.tagCount(); i++) {
				NBTTagCompound itemTag = tagList.getCompoundTagAt(i);
				int slot = itemTag.getByte("Slot") & 0xff;
				if(slot <= _inventories.length) {
					ItemStack itemStack = new ItemStack((Block)null,0,0);
					itemStack.readFromNBT(itemTag);
					_inventories[slot] = itemStack;
				}
			}
		}

		if(tag.hasKey("isInlet")) {
			this.isInlet = tag.getBoolean("isInlet");
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);
		NBTTagList tagList = new NBTTagList();

		for(int i = 0; i < _inventories.length; i++) {
			if((_inventories[i]) != null) {
				NBTTagCompound itemTag = new NBTTagCompound();
				itemTag.setByte("Slot", (byte)i);
				_inventories[i].writeToNBT(itemTag);
				tagList.appendTag(itemTag);
			}
		}

		if(tagList.tagCount() > 0) {
			tag.setTag("Items", tagList);
		}

		tag.setBoolean("isInlet", isInlet);
	}

	// MultiblockTileEntityBase
	@Override
	protected void encodeDescriptionPacket(NBTTagCompound packetData) {
		super.encodeDescriptionPacket(packetData);

		packetData.setBoolean("inlet", isInlet);
	}

	@Override
	protected void decodeDescriptionPacket(NBTTagCompound packetData) {
		super.decodeDescriptionPacket(packetData);

		if(packetData.hasKey("inlet")) {
			setInlet(packetData.getBoolean("inlet"));
		}
	}

	// IInventory

	@Override
	public int getSizeInventory() {
		return NUM_SLOTS;
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		return _inventories[slot];
	}

	@Override
	public ItemStack decrStackSize(int slot, int amount) {
		if(_inventories[slot] != null)
		{
			if(_inventories[slot].stackSize <= amount)
			{
				ItemStack itemstack = _inventories[slot];
				_inventories[slot] = null;
				markDirty();
				return itemstack;
			}
			ItemStack newStack = _inventories[slot].splitStack(amount);
			if(_inventories[slot].stackSize == 0)
			{
				_inventories[slot] = null;
			}

            markDirty();
			return newStack;
		}
		else
		{
			return null;
		}
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int slot) {
		return null;
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack itemstack) {
		_inventories[slot] = itemstack;
		if(itemstack != null && itemstack.stackSize > getInventoryStackLimit())
		{
			itemstack.stackSize = getInventoryStackLimit();
		}

        markDirty();
	}

	@Override
	public String getInventoryName() {
		return "Access Port";
	}

	@Override
	public boolean hasCustomInventoryName() {
		return false;
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer) {
		if(worldObj.getTileEntity(xCoord, yCoord, zCoord) != this)
		{
			return false;
		}
		return entityplayer.getDistanceSq(xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D) <= 64D;
	}

	@Override
	public void openInventory() {
	}

	@Override
	public void closeInventory() {
	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack itemstack) {
		if(itemstack == null) { return true; }

		if(slot == SLOT_INLET) {
			return Reactants.isFuel(itemstack);
		}
		else if(slot == SLOT_OUTLET) {
			return Reactants.isWaste(itemstack);
		}
		else {
			return false;
		}
	}

	// ISidedInventory

	@Override
	public int[] getAccessibleSlotsFromSide(int side) {
		if(isInlet()) {
			return kInletExposed;
		}
		else {
			return kOutletExposed;
		}
	}

	@Override
	public boolean canInsertItem(int slot, ItemStack itemstack, int side) {
		return isItemValidForSlot(slot, itemstack);
	}

	@Override
	public boolean canExtractItem(int slot, ItemStack itemstack, int side) {
		return isItemValidForSlot(slot, itemstack);
	}

	// IMultiblockGuiHandler
	@Override
	public Object getContainer(InventoryPlayer inventoryPlayer) {
		return new ContainerReactorAccessPort(this, inventoryPlayer);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public Object getGuiElement(InventoryPlayer inventoryPlayer) {
		return new GuiReactorAccessPort(new ContainerReactorAccessPort(this, inventoryPlayer), this);
	}

	/**
	 * Called when stuff has been placed in the access port
	 */
	public void onItemsReceived() {
		distributeItems();
		markChunkDirty();
	}

	public boolean isInlet() { return this.isInlet; }

	public void setInlet(boolean shouldBeInlet) {
		if(isInlet == shouldBeInlet) { return; }

		isInlet = shouldBeInlet;

		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);

		if(!worldObj.isRemote) {
			distributeItems();
			markChunkDirty();
		}

		notifyNeighborsOfTileChange();
	}

	protected void distributeItems() {
		if(worldObj.isRemote) { return; }
		if(adjacencyHelper == null) { return; }

		if(this.isInlet()) { return; }

		_inventories[SLOT_OUTLET] = adjacencyHelper.distribute(_inventories[SLOT_OUTLET]);
		markChunkDirty();
	}

	protected void checkForAdjacentInventories() {
		ForgeDirection outDir = getOutwardsDir();

		if(adjacencyHelper == null && outDir != ForgeDirection.UNKNOWN) {
			adjacencyHelper = new AdjacentInventoryHelper(outDir);
		}

		if(adjacencyHelper != null && outDir != ForgeDirection.UNKNOWN) {
			TileEntity te = worldObj.getTileEntity(xCoord + outDir.offsetX, yCoord + outDir.offsetY, zCoord + outDir.offsetZ);
			if(adjacencyHelper.set(te)) {
				distributeItems();
			}
		}
	}

	protected void markChunkDirty() {
		worldObj.markTileEntityChunkModified(xCoord, yCoord, zCoord, this);
	}

	// INeighborUpdateableEntity
	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z,
			Block neighborBlock) {
		checkForAdjacentInventories();
	}

	@Override
	public void onNeighborTileChange(IBlockAccess world, int x, int y, int z,
			int neighborX, int neighborY, int neighborZ) {
		int side = BlockHelper.determineAdjacentSide(this, neighborX, neighborY, neighborZ);
		if(side == getOutwardsDir().ordinal()) {
			checkForAdjacentInventories();
		}
	}
}
