package erogenousbeef.bigreactors.common.multiblock.helpers;

import net.minecraftforge.common.ForgeDirection;

public class RotorInfo {
	// Location of bearing
	public int x, y, z;
	
	// Rotor direction
	public ForgeDirection rotorDirection = ForgeDirection.UNKNOWN;
	
	// Rotor length
	public int rotorLength = 0;
	
	// Array of arrays, containing rotor lengths
	public int[][] bladeLengths = null;
}
