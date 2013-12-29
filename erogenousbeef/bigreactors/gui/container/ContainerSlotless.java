package erogenousbeef.bigreactors.gui.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

/**
 * Generic container for entities which need to know about players for updates, but do not bind slots.
 * @author Erogenous Beef
 */
public class ContainerSlotless extends Container {
	protected ISlotlessUpdater entity;
	
	public ContainerSlotless(ISlotlessUpdater theEntity, EntityPlayer player) {
		super();
		
		entity = theEntity;
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
