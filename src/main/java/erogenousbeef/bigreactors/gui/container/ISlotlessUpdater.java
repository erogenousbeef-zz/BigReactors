package erogenousbeef.bigreactors.gui.container;

import net.minecraft.entity.player.EntityPlayer;

public interface ISlotlessUpdater {

	/**
	 * Called when your updater should begin updating a player.
	 * @param player The player to begin updating.
	 */
	void beginUpdatingPlayer(EntityPlayer player);
	
	/**
	 * Called when your updater should cease updating a player.
	 * @param player The player to cease updating.
	 */
	void stopUpdatingPlayer(EntityPlayer player);
	
	/**
	 * @param player The player to check.
	 * @return True if the player is able to interact with this object, false otherwise.
	 */
	boolean isUseableByPlayer(EntityPlayer player);
}
