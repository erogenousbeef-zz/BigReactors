package erogenousbeef.bigreactors.common.interfaces;

import net.minecraft.entity.player.EntityPlayer;

/**
 * Implement this class on tile entities which can be activated by a wrench or
 * hammer-type tool.
 * @author Erogenous Beef
 */
public interface IWrenchable {
	/**
	 * Called when a player hits the machine with a wrench/hammer.
	 * 
	 * @param player The player hitting the machine.
	 * @param hitSide The side on which the machine was hit.
	 * @return True if the machine handled the wrench hit, false otherwise.
	 */
	public boolean onWrench(EntityPlayer player, int hitSide);
}
