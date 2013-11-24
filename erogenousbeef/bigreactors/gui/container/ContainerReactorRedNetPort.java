package erogenousbeef.bigreactors.gui.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

public class ContainerReactorRedNetPort extends Container {

	public ContainerReactorRedNetPort() {
		super();
	}
	
	@Override
	public boolean canInteractWith(EntityPlayer entityplayer) {
		return true;
	}

}
