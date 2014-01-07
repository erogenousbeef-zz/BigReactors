package erogenousbeef.bigreactors.common.multiblock.interfaces;

/**
 * Implement this to receive once-per-tick updates from the multiblock.
 * @author Erogenous Beef
 *
 */
public interface ITickableMultiblockPart {

	/**
	 * Called once every tick from the reactor's main server tick loop.
	 */
	public void onMultiblockServerTick();
}
