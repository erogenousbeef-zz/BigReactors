package erogenousbeef.bigreactors.api;

public interface IRadiationSource {
	/**
	 * Called once per tick by a reactor when active,
	 * allowing the radiation source to scatter local radiation.
	 * @return An IRadiationPulse object describing the results of the radiation pulse.
	 */
	public IRadiationPulse radiate();
}
