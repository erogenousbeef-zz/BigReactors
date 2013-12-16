package erogenousbeef.bigreactors.gui.container;

import erogenousbeef.bigreactors.common.tileentity.TileEntitySteamCreator;
import erogenousbeef.bigreactors.common.tileentity.TileEntityReactorControlRod;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

public class ContainerHeatGenerator extends Container {
	protected TileEntitySteamCreator entity;
	
	public ContainerHeatGenerator(TileEntitySteamCreator generator, EntityPlayer player) {
		super();
		
		entity = generator;
		entity.beginUpdatingPlayer(player);
	}
	
	@Override
	public boolean canInteractWith(EntityPlayer entityplayer) {
		return entity.isUseableByPlayer(entityplayer);
	}
	
	@Override
    public void onContainerClosed(EntityPlayer player) {
		super.onContainerClosed(player);
		
		entity.stopUpdatingPlayer(player);
	}
}
