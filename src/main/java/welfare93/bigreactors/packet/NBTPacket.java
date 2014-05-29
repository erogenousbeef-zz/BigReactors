package welfare93.bigreactors.packet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;

public class NBTPacket extends Packet{

	public NBTPacket()
	{
		
	}
	NBTTagCompound tag;
	public NBTPacket(NBTTagCompound comp)
	{
		tag=comp;
	}
	@Override
	public void readPacketData(PacketBuffer var1) throws IOException {
		int c=var1.readInt();
		byte[] bytes=new byte[c];Object o=null;
		var1.getBytes(0,bytes,0,c);
		ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
		ObjectInput in = null;
		  in = new ObjectInputStream(bis);
		  try {
			o = in.readObject();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} 
		
		    bis.close();
		 
		    if (in != null) {
		      in.close();}
		  tag=(NBTTagCompound)o;
	}

	@Override
	public void writePacketData(PacketBuffer var1) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutput out = null;
		byte[] bytes;
		  out = new ObjectOutputStream(bos);   
		  out.writeObject(tag);
		  bytes = bos.toByteArray();if (out != null) {
		      out.close();
		  }
		    bos.close();
		    var1.writeInt(bytes.length);
		var1.writeBytes(bytes);
	}

	@Override
	public void processPacket(INetHandler var1) {
	}

}
