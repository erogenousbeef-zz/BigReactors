package erogenousbeef.bigreactors.client;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;
import erogenousbeef.bigreactors.common.multiblock.MultiblockReactor;
import erogenousbeef.bigreactors.common.multiblock.interfaces.IMultiblockNetworkHandler;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityReactorControlRod;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityReactorPart;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityReactorRedNetPort;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityReactorRedstonePort;
import erogenousbeef.bigreactors.common.tileentity.base.TileEntityBeefBase;
import erogenousbeef.bigreactors.common.tileentity.base.TileEntityInventory;
import erogenousbeef.bigreactors.common.tileentity.base.TileEntityPoweredInventoryFluid;
import erogenousbeef.bigreactors.net.PacketWrapper;
import erogenousbeef.bigreactors.net.Packets;

public class ClientPacketHandler implements IPacketHandler {

	@Override
	public void onPacketData(INetworkManager manager,
			Packet250CustomPayload packet, Player player) {

		DataInputStream data = new DataInputStream(new ByteArrayInputStream(packet.data));
		int packetType = PacketWrapper.readPacketID(data);
		
		int x, y, z;
		switch(packetType) {
		case Packets.ReactorControllerFullUpdate:
		case Packets.MultiblockTurbineFullUpdate:
		{
			try {
				x = data.readInt();
				y = data.readInt();
				z = data.readInt();
				TileEntity te = ((EntityPlayer)player).worldObj.getBlockTileEntity(x, y, z);
				if(te instanceof IMultiblockNetworkHandler) {
					((IMultiblockNetworkHandler)te).onNetworkPacket(packetType, data);
				}
				else {
					throw new IOException("Invalid TileEntity for receipt of multiblock packet");
				}

			} catch (IOException e) {
				e.printStackTrace();
				// TODO: Crash all the things.
			}
		}
		break;
		case Packets.ReactorWasteEjectionSettingUpdate: {
				try {
					x = data.readInt();
					y = data.readInt();
					z = data.readInt();
					
					TileEntity te = ((EntityPlayer)player).worldObj.getBlockTileEntity(x, y, z);
					if(te instanceof TileEntityReactorPart) {
						MultiblockReactor.WasteEjectionSetting newSetting = MultiblockReactor.WasteEjectionSetting.values()[data.readInt()];
						((TileEntityReactorPart)te).getReactorController().setWasteEjection(newSetting);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		break;
		case Packets.SmallMachineUIUpdate: {
				try {
					x = data.readInt();
					y = data.readInt();
					z = data.readInt();
					
					TileEntity te = ((EntityPlayer)player).worldObj.getBlockTileEntity(x, y, z);
					if(te != null && te instanceof TileEntityBeefBase) {
						NBTTagCompound tagCompound = Packet.readNBTTagCompound(data);
						((TileEntityBeefBase)te).onReceiveUpdate(tagCompound);
					}
					else {
						throw new IOException("Invalid TileEntity for receipt of BeefBase UI Update packet");
					}
				} catch(IOException e) {
					e.printStackTrace();
					// TODO: Crash all the things.
				}
			}
		break;
		case Packets.SmallMachineRotationUpdate: {
			try {
				x = data.readInt();
				y = data.readInt();
				z = data.readInt();
				
				TileEntity te = ((EntityPlayer)player).worldObj.getBlockTileEntity(x, y, z);
				if(te != null && te instanceof TileEntityBeefBase) {
					((TileEntityBeefBase)te).rotateTowards(ForgeDirection.getOrientation(data.readInt()));
					((EntityPlayer)player).worldObj.markBlockForUpdate(x, y, z);
				}
				else {
					throw new IOException("Invalid TileEntity for receipt of BeefBase Rotation Update packet");
				}
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
		break;
		case Packets.SmallMachineInventoryExposureUpdate: {
			try {
				x = data.readInt();
				y = data.readInt();
				z = data.readInt();
				
				TileEntity te = ((EntityPlayer)player).worldObj.getBlockTileEntity(x, y, z);
				if(te != null && te instanceof TileEntityInventory) {
					((TileEntityInventory)te).setExposedInventorySlotReference(data.readInt(), data.readInt());
					((EntityPlayer)player).worldObj.markBlockForUpdate(x, y, z);
				}
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
		break;
		case Packets.SmallMachineFluidExposureUpdate: {
			try {
				x = data.readInt();
				y = data.readInt();
				z = data.readInt();
				
				TileEntity te = ((EntityPlayer)player).worldObj.getBlockTileEntity(x, y, z);
				if(te != null && te instanceof TileEntityPoweredInventoryFluid) {
					((TileEntityPoweredInventoryFluid)te).setExposedTank(ForgeDirection.getOrientation(data.readInt()), data.readInt());
					((EntityPlayer)player).worldObj.markBlockForUpdate(x, y, z);
				}
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
		break;
		case Packets.ControlRodUpdate: {
			try {
				x = data.readInt();
				y = data.readInt();
				z = data.readInt();
				
				TileEntity te = ((EntityPlayer)player).worldObj.getBlockTileEntity(x, y, z);
				if(te != null && te instanceof TileEntityReactorControlRod) {
					boolean isAssembled = data.readBoolean();
					int minFuelRodY = data.readInt();
					short controlRodInsertion = data.readShort();
					((TileEntityReactorControlRod)te).onControlRodUpdate(isAssembled, minFuelRodY, controlRodInsertion);
				}
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
		break;
		case Packets.RedNetSetData: {
			try {
				x = data.readInt();
				y = data.readInt();
				z = data.readInt();
				
				TileEntity te = ((EntityPlayer)player).worldObj.getBlockTileEntity(x, y, z);
				if(te instanceof TileEntityReactorRedNetPort) {
					((TileEntityReactorRedNetPort)te).decodeSettings(data, false);
				}
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
		break;
		case Packets.RedstoneSetData: {
			try {
				x = data.readInt();
				y = data.readInt();
				z = data.readInt();
				
				TileEntity te = ((EntityPlayer)player).worldObj.getBlockTileEntity(x, y, z);
				if(te instanceof TileEntityReactorRedstonePort) {
					int newCircuit = data.readInt();
					int newLevel = data.readInt();
					boolean newGt = data.readBoolean();
					boolean pulse = data.readBoolean();
					((TileEntityReactorRedstonePort)te).onReceiveUpdatePacket(newCircuit, newLevel, newGt, pulse);
				}
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
		break;
		}
	}
}
