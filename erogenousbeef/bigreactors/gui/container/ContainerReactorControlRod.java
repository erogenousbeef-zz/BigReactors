package erogenousbeef.bigreactors.gui.container;

import erogenousbeef.bigreactors.common.tileentity.TileEntityReactorControlRod;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

public class ContainerReactorControlRod extends Container {

	protected TileEntityReactorControlRod entity;
	
	public ContainerReactorControlRod(TileEntityReactorControlRod controlRod, EntityPlayer player) {
		super();
		
		entity = controlRod;
		entity.beginUpdatingPlayer(player);
	}
	
	@Override
	public boolean canInteractWith(EntityPlayer entityplayer) {
		return true;
	}
	
	@Override
    public void onCraftGuiClosed(EntityPlayer player) {
		super.onCraftGuiClosed(player);
		
		entity.stopUpdatingPlayer(player);
	}
}
