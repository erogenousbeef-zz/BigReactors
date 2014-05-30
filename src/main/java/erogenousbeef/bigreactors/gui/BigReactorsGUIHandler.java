package erogenousbeef.bigreactors.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import cpw.mods.fml.common.network.IGuiHandler;
import erogenousbeef.bigreactors.common.multiblock.interfaces.IMultiblockGuiHandler;

public class BigReactorsGUIHandler implements IGuiHandler {

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world,
			int x, int y, int z) {
		
		TileEntity te = world.getTileEntity(x, y, z);
		if(te == null) {
			return null;
		}
		else if(te instanceof IMultiblockGuiHandler) {
			return ((IMultiblockGuiHandler)te).getContainer(player.inventory);
		}
		else if(te instanceof IBeefGuiEntity) {
			return ((IBeefGuiEntity)te).getContainer(player);
		}
		
		return null;
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world,
			int x, int y, int z) {
		TileEntity te = world.getTileEntity(x, y, z);
		if(te == null) {
			return null;
		}
		
		if(te instanceof IMultiblockGuiHandler) {
			IMultiblockGuiHandler part = (IMultiblockGuiHandler)te;
			return part.getGuiElement(player.inventory);
		}
		else if(te instanceof IBeefGuiEntity) {
			return ((IBeefGuiEntity)te).getGUI(player);
		}
		
		return null;
	}

}
