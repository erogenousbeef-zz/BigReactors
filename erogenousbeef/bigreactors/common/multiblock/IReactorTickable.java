package erogenousbeef.bigreactors.common.multiblock;

/**
 * Implement this to receive once-per-tick updates from the reactor.
 * @author Erogenous Beef
 *
 */
public interface IReactorTickable {

	/**
	 * Called once every tick from the reactor's main tick loop.
	 */
	public void onReactorTick();
}
