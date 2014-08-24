package erogenousbeef.bigreactors.common.interfaces;

import net.minecraft.util.IIcon;
import cofh.api.tileentity.IReconfigurableSides;

public interface IBeefReconfigurableSides extends IReconfigurableSides {

	/**
	 * Return the icon which should be used for a given side.
	 * Note: Passes the unrotated world side.
	 */
	public IIcon getIconForSide(int referenceSide);
	
}
