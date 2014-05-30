package welfare93.bigreactors.packet;

import java.io.IOException;

import erogenousbeef.bigreactors.common.multiblock.MultiblockReactor;
import erogenousbeef.bigreactors.common.multiblock.interfaces.IMultiblockNetworkHandler;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityReactorControlRod;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityReactorPart;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityReactorRedNetPort;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityReactorRedstonePort;
import erogenousbeef.bigreactors.common.tileentity.base.TileEntityBeefBase;
import erogenousbeef.bigreactors.common.tileentity.base.TileEntityInventory;
import erogenousbeef.bigreactors.common.tileentity.base.TileEntityPoweredInventoryFluid;
import erogenousbeef.bigreactors.gui.IBeefGuiEntity;
import erogenousbeef.bigreactors.net.Packets;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

public class MainPacket extends AbstractPacket {
	public MainPacket()
	{}
	public MainPacket(int id,int x,int y,int z,ByteBuf value)
	{
		this.x=x; this.y=y; this.z=z; this.value=value;this.id=id;
	}
	ByteBuf value;
	int x,y,z,id;
	@Override
	public void handleServerSide(EntityPlayer player) {
		TileEntity te = ((EntityPlayer)player).worldObj.getTileEntity(x, y, z);
		if(te==null) return;
			switch(id) {
			case Packets.MultiblockActivateButton:
			case Packets.ReactorEjectButton:
			case Packets.AccessPortButton:
			case Packets.ReactorWasteEjectionSettingUpdate:
			case Packets.MultiblockTurbineFullUpdate:
			case Packets.MultiblockTurbineGovernorUpdate:
			case Packets.MultiblockTurbineVentUpdate:
				if(te instanceof IMultiblockNetworkHandler) {
					((IMultiblockNetworkHandler)te).onNetworkPacket(id, value);}break;
			case Packets.BeefGuiButtonPress:
				if(te != null & te instanceof IBeefGuiEntity) {
					String buttonName = PacketHandler.decodeString(value);
					((IBeefGuiEntity)te).onReceiveGuiButtonPress(buttonName, value);
				}
				break;
			case Packets.ControlRodSetName:
				if(te instanceof TileEntityReactorControlRod) {
				String newName = PacketHandler.decodeString(value);
				((TileEntityReactorControlRod)te).setName(newName);
				}
				break;
			case Packets.RedNetSetData:
				if(te instanceof TileEntityReactorRedNetPort) {
					((TileEntityReactorRedNetPort)te).decodeSettings(value, true);
				}
				break;
			case Packets.RedstoneSetData:
				if(te instanceof TileEntityReactorRedstonePort) {
					int newCircuit = value.readInt();
					int newLevel = value.readInt();
					boolean newGt = value.readBoolean();
					boolean pulse = value.readBoolean();
					((TileEntityReactorRedstonePort)te).onReceiveUpdatePacket(newCircuit, newLevel, newGt, pulse);
				}
				break;
			}
	}

	@Override
	public void handleClientSide(EntityPlayer player) {
		TileEntity te = ((EntityPlayer)player).worldObj.getTileEntity(x, y, z);
		switch(id) {
		case Packets.ReactorControllerFullUpdate:
		case Packets.MultiblockTurbineFullUpdate:
			if(te instanceof IMultiblockNetworkHandler) {
				((IMultiblockNetworkHandler)te).onNetworkPacket(id, value);}break;
		case Packets.ReactorWasteEjectionSettingUpdate: 
			if(te instanceof TileEntityReactorPart) {
				MultiblockReactor.WasteEjectionSetting newSetting = MultiblockReactor.WasteEjectionSetting.values()[value.readInt()];
				((TileEntityReactorPart)te).getReactorController().setWasteEjection(newSetting);
			}break;
		case Packets.SmallMachineUIUpdate:
			if(te != null && te instanceof TileEntityBeefBase) {
				//NBTTagCompound tagCompound = NBTTagCompound(value);
				//((TileEntityBeefBase)te).onReceiveUpdate(tagCompound); 
				//TODO:WTF?
		}break;
		case Packets.SmallMachineRotationUpdate:
			if(te instanceof TileEntityBeefBase) {
				((TileEntityBeefBase)te).rotateTowards(ForgeDirection.getOrientation(value.readInt()));
				((EntityPlayer)player).worldObj.markBlockForUpdate(x, y, z);				
			}
			break;
		case Packets.SmallMachineInventoryExposureUpdate: 
			if(te != null && te instanceof TileEntityInventory) {
				((TileEntityInventory)te).setExposedInventorySlotReference(value.readInt(), value.readInt());
				((EntityPlayer)player).worldObj.markBlockForUpdate(x, y, z);
			}
			break;
		case Packets.SmallMachineFluidExposureUpdate: 
			if(te instanceof TileEntityPoweredInventoryFluid) {
				((TileEntityPoweredInventoryFluid)te).setExposedTank(ForgeDirection.getOrientation(value.readInt()), value.readInt());
				((EntityPlayer)player).worldObj.markBlockForUpdate(x, y, z);
			}
			break;
		case Packets.ControlRodUpdate:
			if(te instanceof TileEntityReactorControlRod) {
				short controlRodInsertion = value.readShort();
				((TileEntityReactorControlRod)te).onControlRodUpdate(controlRodInsertion);
			}
			break;
			case Packets.RedNetSetData:
				if(te instanceof TileEntityReactorRedNetPort) {
				((TileEntityReactorRedNetPort)te).decodeSettings(value,false);
			}
				break;
			case Packets.RedstoneSetData:
				if(te instanceof TileEntityReactorRedstonePort) {
					int newCircuit = value.readInt();
					int newLevel = value.readInt();
					boolean newGt = value.readBoolean();
					boolean pulse = value.readBoolean();
					((TileEntityReactorRedstonePort)te).onReceiveUpdatePacket(newCircuit, newLevel, newGt, pulse);
				}
				break;
		
		}
	}

	@Override
	public void decodeInto(ChannelHandlerContext ctx, ByteBuf slice) {
		id=slice.readInt();
		x=slice.readInt();
		y=slice.readInt();
		z=slice.readInt();
		value=slice.copy();
	}

	@Override
	public void encodeInto(ChannelHandlerContext ctx, ByteBuf buffer) {
		buffer.writeInt(id);
		buffer.writeInt(x);
		buffer.writeInt(y);
		buffer.writeInt(z);
		buffer.writeBytes(value);
	}
	
	
	
}
