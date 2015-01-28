package erogenousbeef.bigreactors.common.multiblock.tileentity;

import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.common.multiblock.MultiblockTurbine;
import erogenousbeef.bigreactors.common.multiblock.helpers.RotorInfo;
import erogenousbeef.bigreactors.utils.StaticUtils;
import erogenousbeef.core.common.CoordTriplet;
import erogenousbeef.core.multiblock.MultiblockControllerBase;

public class TileEntityTurbineRotorBearing extends
		TileEntityTurbinePartStandard {

	RotorInfo rotorInfo = null;
	Integer displayList = null;
	float angle = 0f;
	
	@SideOnly(Side.CLIENT)
	public Integer getDisplayList() { return displayList; }
	
	@SideOnly(Side.CLIENT)
	public void setDisplayList(int newList) { displayList = newList; }
	
	@SideOnly(Side.CLIENT)
	public void clearDisplayList() { displayList = null; }
	
	@SideOnly(Side.CLIENT)
	public float getAngle() { return angle; }
	
	@SideOnly(Side.CLIENT)
	public void setAngle(float newAngle) { angle = newAngle; }

	protected AxisAlignedBB boundingBox;
	
	@Override
	public void onMachineAssembled(MultiblockControllerBase controller) {
		super.onMachineAssembled(controller);
		displayList = null;
		calculateRotorInfo();
	}
	
	@SideOnly(Side.CLIENT)
	public RotorInfo getRotorInfo() {
		return rotorInfo;
	}
	
	public AxisAlignedBB getAABB() { return boundingBox; }
	
	private void calculateRotorInfo() {
		// Calculate bounding box
		MultiblockTurbine turbine = getTurbine();
		CoordTriplet minCoord = turbine.getMinimumCoord();
		CoordTriplet maxCoord = turbine.getMaximumCoord();

		boundingBox = AxisAlignedBB.getBoundingBox(minCoord.x, minCoord.y, minCoord.z, maxCoord.x + 1, maxCoord.y + 1, maxCoord.z + 1);
		
		if(worldObj.isRemote) {
			// Calculate rotor info
			rotorInfo = new RotorInfo();
			rotorInfo.rotorDirection = getOutwardsDir().getOpposite();
			switch(rotorInfo.rotorDirection) {
				case DOWN:
				case UP:
				case UNKNOWN:
					rotorInfo.rotorLength = maxCoord.y - minCoord.y - 1;
					break;
				case EAST:
				case WEST:
					rotorInfo.rotorLength = maxCoord.x - minCoord.x - 1;
					break;
				case NORTH:
				case SOUTH:
					rotorInfo.rotorLength = maxCoord.z - minCoord.z - 1;
					break;
			}
			
			CoordTriplet currentCoord = getWorldLocation();
			CoordTriplet bladeCoord = new CoordTriplet(0,0,0);

			ForgeDirection[] dirsToCheck = StaticUtils.neighborsBySide[rotorInfo.rotorDirection.ordinal()];
			rotorInfo.bladeLengths = new int[rotorInfo.rotorLength][4];

			int rotorPosition = 0;
			currentCoord.translate(rotorInfo.rotorDirection);

			while(rotorPosition < rotorInfo.rotorLength) {
				// Current block is a rotor
				// Get list of normals
				int bladeLength;
				ForgeDirection bladeDir;
				for(int bladeIdx = 0; bladeIdx < dirsToCheck.length; bladeIdx++) {
					bladeDir = dirsToCheck[bladeIdx];
					bladeCoord.copy(currentCoord);
					bladeCoord.translate(bladeDir);
					bladeLength = 0;
					while(worldObj.getBlock(bladeCoord.x, bladeCoord.y, bladeCoord.z) == BigReactors.blockTurbineRotorPart && bladeLength < 32) {
						bladeLength++;
						bladeCoord.translate(bladeDir);
					}
					
					rotorInfo.bladeLengths[rotorPosition][bladeIdx] = bladeLength;
				}
				
				rotorPosition++;
				currentCoord.translate(rotorInfo.rotorDirection);
			}
		}
	}
}
