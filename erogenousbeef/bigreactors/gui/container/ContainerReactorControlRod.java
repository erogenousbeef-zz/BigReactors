package erogenousbeef.bigreactors.gui.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityReactorControlRod;

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
    public void onContainerClosed(EntityPlayer player) {
		super.onContainerClosed(player);
		
		entity.stopUpdatingPlayer(player);
	}
}
