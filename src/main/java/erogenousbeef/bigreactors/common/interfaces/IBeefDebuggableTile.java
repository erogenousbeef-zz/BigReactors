package erogenousbeef.bigreactors.common.interfaces;

import net.minecraft.entity.player.EntityPlayer;

/**
 * Implement this interface on tile entities which can be debugged via
 * the BeefDebugTool.
 * @author Erogenous Beef
 */
public interface IBeefDebuggableTile {
	public String getDebugInfo();
}
