package erogenousbeef.bigreactors.common.multiblock.tileentity;

import io.netty.buffer.ByteBuf;

import java.io.IOException;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fluids.FluidStack;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import erogenousbeef.bigreactors.client.gui.GuiReactorControlRod;
import erogenousbeef.bigreactors.common.multiblock.MultiblockReactor;
import erogenousbeef.bigreactors.gui.IBeefGuiEntity;
import erogenousbeef.bigreactors.gui.container.ContainerReactorControlRod;
import erogenousbeef.bigreactors.net.CommonPacketHandler;
import erogenousbeef.bigreactors.net.message.ControlRodUpdateMessage;
import erogenousbeef.core.multiblock.MultiblockControllerBase;
import erogenousbeef.core.multiblock.MultiblockValidationException;
import erogenousbeef.core.multiblock.rectangular.RectangularMultiblockTileEntityBase;

public class TileEntityReactorControlRod extends RectangularMultiblockTileEntityBase implements IBeefGuiEntity {
	public final static short maxInsertion = 100;
	public final static short minInsertion = 0;

	// Radiation
	protected short controlRodInsertion; // 0 = retracted fully, 100 = inserted fully
	
	// User settings
	protected String name;

	// Backwards Compatibility
	private FluidStack cachedFuel;
	
	public TileEntityReactorControlRod() {
		super();
	
		controlRodInsertion = minInsertion;
		
		name = "";
		
		cachedFuel = null;
	}
	
	// Data accessors
	public short getControlRodInsertion() {
		return this.controlRodInsertion;
	}
	
	public FluidStack getCachedFuel() { return cachedFuel; }
	
	public void setControlRodInsertion(short newInsertion) {
		if(newInsertion > maxInsertion || newInsertion < minInsertion || newInsertion == controlRodInsertion) { return; }
		if(!isConnected()) { return; }

		this.controlRodInsertion = (short)Math.max(Math.min(newInsertion, maxInsertion), minInsertion);
		this.sendControlRodUpdate();
	}
	
	// Fuel Handling

	// TileEntity stuff
	// Save/Load
	@Override
	public void readFromNBT(NBTTagCompound data) {
		super.readFromNBT(data);
		this.readLocalDataFromNBT(data);
		
		if(data.hasKey("fuelFluidStack")) {
			this.cachedFuel = FluidStack.loadFluidStackFromNBT(data.getCompoundTag("fuelFluidStack"));
		}
	}
	
	@Override
	public void writeToNBT(NBTTagCompound data) {
		super.writeToNBT(data);
		this.writeLocalDataToNBT(data);
	}

	// Player updates via IBeefGuiEntity
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
	
	// Network Messages
	public void onClientControlRodChange(int amount) {
		setControlRodInsertion((short)(this.controlRodInsertion + amount));
	}

	// Control Rod Updates
	protected void sendControlRodUpdate() {
		if(this.worldObj == null || this.worldObj.isRemote) { return; }

        CommonPacketHandler.INSTANCE.sendToAllAround(new ControlRodUpdateMessage(xCoord, yCoord, zCoord, controlRodInsertion), new NetworkRegistry.TargetPoint(worldObj.provider.dimensionId, xCoord, yCoord, zCoord, 50));
	}
	
	@SideOnly(Side.CLIENT)
	public void onControlRodUpdate(short controlRodInsertion) {
		this.controlRodInsertion = controlRodInsertion;
	}

	private void readLocalDataFromNBT(NBTTagCompound data) {
		if(data.hasKey("controlRodInsertion")) {
			this.controlRodInsertion = data.getShort("controlRodInsertion");
		}
		
		if(data.hasKey("name")) {
			this.name = data.getString("name");
		}
		else {
			this.name = "";
		}
	}
	
	private void writeLocalDataToNBT(NBTTagCompound data) {
		data.setShort("controlRodInsertion", controlRodInsertion);
		
		if(!this.name.isEmpty()) {
			data.setString("name", this.name);
		}
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
		TileEntity teBelow = this.worldObj.getTileEntity(xCoord, yCoord - 1, zCoord);
		if(!(teBelow instanceof TileEntityReactorFuelRod)) {
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
		packet.setTag("reactorControlRod", localData);
	}
	
	@Override
	protected void decodeDescriptionPacket(NBTTagCompound packet) {
		super.decodeDescriptionPacket(packet);
		
		if(packet.hasKey("reactorControlRod")) {
			NBTTagCompound localData = packet.getCompoundTag("reactorControlRod");
			this.readLocalDataFromNBT(localData);
			
			if(worldObj != null && worldObj.isRemote) {
				this.worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
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
