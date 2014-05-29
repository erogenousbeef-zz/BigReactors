package erogenousbeef.bigreactors.gui.container;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityReactorPart;

public class ContainerReactorController extends Container {

	TileEntityReactorPart part;
	
	public ContainerReactorController(TileEntityReactorPart reactorPart, EntityPlayer player) {
		part = reactorPart;
		if(player instanceof EntityPlayerMP)
		part.getReactorController().beginUpdatingPlayer(player);
	}
	
	@Override
	public boolean canInteractWith(EntityPlayer entityplayer) {
		return true;
	}

	@Override
	public void putStackInSlot(int slot, ItemStack stack) {
		return;
	}
	
	@Override
    public void onContainerClosed(EntityPlayer player) {
		super.onContainerClosed(player);
		
		if(part != null && part.getReactorController() != null)
			part.getReactorController().stopUpdatingPlayer(player);
	}
}
