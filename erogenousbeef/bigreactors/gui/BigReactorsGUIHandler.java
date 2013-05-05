package erogenousbeef.bigreactors.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import cpw.mods.fml.common.network.IGuiHandler;
import erogenousbeef.bigreactors.client.gui.GuiReactorStatus;
import erogenousbeef.bigreactors.common.block.BlockReactorPart;
import erogenousbeef.bigreactors.common.tileentity.TileEntityReactorPart;

public class BigReactorsGUIHandler implements IGuiHandler {

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world,
			int x, int y, int z) {
		
		TileEntity te = world.getBlockTileEntity(x, y, z);
		if(te == null) {
			return null;
		}
		
		return null;
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world,
			int x, int y, int z) {
		TileEntity te = world.getBlockTileEntity(x, y, z);
		if(te == null) {
			return null;
		}
		
		if(te instanceof TileEntityReactorPart && BlockReactorPart.isController(te.blockMetadata)) {
			// TODO: Return new GUI here
			TileEntityReactorPart part = (TileEntityReactorPart)te;
			if(part.isConnected()) {
				return new GuiReactorStatus(part.getReactorController());
			}
			return null;
		}
		
		return null;
	}

}
