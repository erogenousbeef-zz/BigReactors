package erogenousbeef.bigreactors.common.multiblock.interfaces;

public interface IConditionalUpdater {

	/**
	 * Call this once per active tick.
	 * @return True if this data helper needs to send data to nearby players.
	 */
	public boolean shouldUpdate();
	
}
