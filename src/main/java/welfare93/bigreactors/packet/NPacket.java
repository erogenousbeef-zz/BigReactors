package welfare93.bigreactors.packet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityReactorPart;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityReactorPartBase;
import erogenousbeef.core.multiblock.MultiblockControllerBase;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public class NPacket extends AbstractPacket{
	MultiblockControllerBase tag;int x,y,z;
	public NPacket(MultiblockControllerBase comp,int x,int y,int z)
	{
		tag=comp;this.x=x;this.y=y;this.z=z;
	}
	@Override
	public void handleServerSide(EntityPlayer player) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleClientSide(EntityPlayer player) {
		// TODO Auto-generated method stub
		TileEntity e=player.worldObj.getTileEntity(x, y, z);
		if(e instanceof TileEntityReactorPartBase)
		{
			((TileEntityReactorPartBase)e).controller=tag;
		}
	}

	@Override
	public void decodeInto(ChannelHandlerContext ctx, ByteBuf slice) {
		x=slice.readInt();
		y=slice.readInt();
		z=slice.readInt();
		int c=slice.readInt();
		byte[] bytes=new byte[c];Object oo=null;
		try{
		slice.getBytes(0,bytes,0,c);
		ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
		ObjectInput in = null;
		  in = new ObjectInputStream(bis);
		  try {
			oo = in.readObject();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} 
		
		    bis.close();
		 
		    if (in != null) {
		      in.close();}
		}
		catch(Exception e){}
		  tag=(MultiblockControllerBase)oo;
		
	}

	@Override
	public void encodeInto(ChannelHandlerContext ctx, ByteBuf buffer) {
		buffer.writeInt(x);
		buffer.writeInt(y);
		buffer.writeInt(z);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutput out = null;
		byte[] bytes=null;
		  try {
		  out = new ObjectOutputStream(bos);   
		  out.writeObject(tag);
		  bytes = bos.toByteArray();if (out != null) {
		      out.close();
		  }
		    bos.close();
			}
			catch(Exception e){}
		    buffer.writeInt(bytes.length);
		    buffer.writeBytes(bytes);
		
	}

}
