package erogenousbeef.bigreactors.common.multiblock.helpers;

import erogenousbeef.bigreactors.api.IRadiationModerator;
import erogenousbeef.bigreactors.api.RadiationData;
import erogenousbeef.bigreactors.api.RadiationPacket;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityReactorControlRod;
import erogenousbeef.bigreactors.utils.StaticUtils;
import erogenousbeef.core.common.CoordTriplet;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.IFluidBlock;

/**
 * Helper for reactor radiation game logic
 * @author Erogenous Beef
 */
public class RadiationHelper {

	// Game Balance Values
	// TODO: Make these configurable
	public static final float fuelPerRadiationUnit = 0.001f; // fuel units used per fission event
	public static final float heatPerRadiationUnit = 0.1f; // C generated per fission event

	private float fertility;
	
	public RadiationHelper() {
		fertility = 1f;
	}

	public RadiationData radiate(World world, FuelContainer fuelContainer, TileEntityReactorControlRod source, int sourceY, float fuelHeat, float environmentHeat, int numControlRods) {
		// No fuel? No radiation!
		if(fuelContainer.getFuelAmount() <= 0) { return null; }

		// Determine radiation amount & intensity, heat amount, determine fuel usage
		RadiationData data = new RadiationData();

		// Base value for heat penalties. 0-1, caps at about 3000C;
		double heatPenaltyBase = Math.exp(-15*Math.exp(-0.0025*fuelHeat));

		// Raw amount - what's actually in the tanks
		// Effective amount - how 
		int baseFuelAmount = fuelContainer.getFuelAmount() + fuelContainer.getWasteAmount() / 100;
		float rawRadIntensity = (float)baseFuelAmount * 0.001f;
		
		// Intensity = how strong the radiation is, hardness = how energetic the radiation is (penetration)
		// Intensity is a function of fuel amount, with a slight bonus for higher-end amounts of fuel.
		// Aside from fuel consumption, everything else is scaled up slightly, providing a bonus to high concentrations of fuel.
		// The scaling factor is dependent on the size of each fuel rod, thus providing an incentive for taller reactors, as well as wider.
		float scaledRadIntensity = (float) Math.pow((rawRadIntensity/numControlRods), fuelContainer.getFuelReactivity()) * numControlRods;
		
		// We cut the raw intensity by control rod insertion, straight off percentagewise.
		scaledRadIntensity = scaledRadIntensity * (float)source.getControlRodInsertion() / 100f;

		// Now nerf actual radiation production based on heat. TODO: Necessary? Balance this.
		float effectiveRadIntensity = scaledRadIntensity * (1f + (float)(-0.95f*Math.exp(-10f*Math.exp(-0.0012f*fuelHeat))));
		
		// Radiation hardness starts at 20% and asymptotically approaches 100% as heat rises.
		// This will make radiation harder and harder to capture.
		float radHardness = 0.2f + (float)(0.8 * heatPenaltyBase);
		
		// Calculate based on propagation-to-self
		float rawFuelUsage = fuelPerRadiationUnit * rawRadIntensity / getFertilityModifier(); // Not a typo. Fuel usage is thus penalized at high heats.
		data.fuelHeatChange = heatPerRadiationUnit * effectiveRadIntensity;
		data.environmentHeatChange = 0f;

		// Propagate radiation to others
		CoordTriplet originCoord = new CoordTriplet(source.xCoord, sourceY, source.zCoord);
		CoordTriplet currentCoord = originCoord.copy();
		
		effectiveRadIntensity *= 0.25f; // We're going to do this four times, no need to repeat
		RadiationPacket radPacket = new RadiationPacket();

		for(ForgeDirection dir : StaticUtils.CardinalDirections) {
			radPacket.hardness = radHardness;
			radPacket.intensity = effectiveRadIntensity;
			int ttl = 4;

			currentCoord.translate(dir);
			
			while(ttl > 0 && radPacket.intensity > 0.0001f) {
				ttl--;
				performIrradiation(world, data, radPacket, currentCoord.x, currentCoord.y, currentCoord.z);
			}
		}
		
		// Apply changes
		this.fertility += data.fuelAbsorbedRadiation;
		data.fuelAbsorbedRadiation = 0f;
		
		// Inform fuelContainer
		fuelContainer.onRadiationUsesFuel(rawFuelUsage);
		data.fuelUsage = rawFuelUsage;
		
		return data;
	}
	
	private void performIrradiation(World world, RadiationData data, RadiationPacket radiation, int x, int y, int z) {
		TileEntity te = world.getBlockTileEntity(x, y, z);
		if(te instanceof IRadiationModerator) {
			((IRadiationModerator)te).moderateRadiation(data, radiation);
		}
		else if (world.isAirBlock(x, y, z)) {
			moderateByAir(data, radiation);
		}
		else {
			int blockID = world.getBlockId(x, y, z);
			if(blockID > 0 && blockID < Block.blocksList.length) {
				Block b = Block.blocksList[blockID];
				
				if(b instanceof IFluidBlock) {
					moderateByFluid(data, radiation, ((IFluidBlock)b).getFluid());
				}
				else {
					// Go by block id
					moderateByBlock(data, radiation, blockID, world.getBlockMetadata(x, y, z));
				}
			}
			else {
				// Weird-ass ID problem. Assume it's air.
				moderateByAir(data, radiation);
			}
			// Do it based on fluid?
		}
	}
	
	static final float airAbsorption = 0.1f;
	static final float airHeatEfficiency = 0.25f;
	static final float airModeration = 1.1f;

	private void moderateByAir(RadiationData data, RadiationPacket radiation) {
		applyModerationFactors(data, radiation, airAbsorption, airModeration, airHeatEfficiency);
	}
	
	private void moderateByBlock(RadiationData data, RadiationPacket radiation, int blockID, int metadata) {
		float absorption, heatEfficiency, moderation;

		if(blockID == Block.blockIron.blockID) {
			absorption = 0.5f;
			moderation = 1.4f;
			heatEfficiency = 0.75f;
		}
		else if(blockID == Block.blockGold.blockID) {
			absorption = 0.52f;
			moderation = 1.45f;
			heatEfficiency = 0.8f;
		}
		else if(blockID == Block.blockDiamond.blockID) {
			absorption = 0.55f;
			moderation = 1.5f;
			heatEfficiency = 0.85f;
		}
		else if(blockID == Block.blockEmerald.blockID) {
			absorption = 0.55f;
			moderation = 1.5f;
			heatEfficiency = 0.85f;
		}
		else {
			absorption = airAbsorption;
			heatEfficiency = airHeatEfficiency;
			moderation = airModeration;
		}
		
		applyModerationFactors(data, radiation, absorption, heatEfficiency, moderation);
	}
	
	private void moderateByFluid(RadiationData data, RadiationPacket radiation, Fluid fluid) {
		float absorption, heatEfficiency, moderation;
		String name = fluid.getName();

		if(name.equals("ender")) {
			absorption = 0.9f;
			moderation = 2.0f;
			heatEfficiency = 0.75f;
		}
		else if(name.equals("cryotheum")) {
			absorption = 0.66f;
			moderation = 4.0f;
			heatEfficiency = 0.6f;
		}
		else if(name.equals("redstone")) {
			absorption = 0.75f;
			moderation = 1.6f;
			heatEfficiency = 0.5f;
		}
		else if(name.equals("pyrotheum")) {
			absorption = 0.33f; // Not terribly absorptive
			moderation = 0.66f; // Makes your radiation harder!
			heatEfficiency = 0.7f; // But efficient...!
		}
		else if(name.equals("glowstone")) {
			absorption = 0.2f;
			moderation = 1.2f;
			heatEfficiency = 0.6f;
		}
		else {
			// Assume it's like water
			absorption = 0.33f;
			moderation = 1.33f;
			heatEfficiency = 0.5f;
		}

		applyModerationFactors(data, radiation, absorption, heatEfficiency, moderation);
	}
	
	private static void applyModerationFactors(RadiationData data, RadiationPacket radiation, float absorption, float heatEfficiency, float moderation) {
		float radiationAbsorbed = radiation.intensity * absorption * (1f - radiation.hardness);
		radiation.intensity = Math.max(0f, radiation.intensity - radiationAbsorbed);
		radiation.hardness /= moderation;
		data.environmentHeatChange += heatEfficiency * radiationAbsorbed * heatPerRadiationUnit;
	}
	
	// Data Access
	public float getFertility() { return fertility; }

	public float getFertilityModifier() {
		if(fertility <= 1f) { return 1f; }
		else {
			return (float)(Math.log10(fertility) + 1);
		}
	}

	public void setFertility(float newFertility) {
		if(Float.isNaN(newFertility) || Float.isInfinite(newFertility)) {
			fertility = 1f;
		}
		else if(newFertility < 0f) {
			fertility = 0f;
		}
		else {
			fertility = newFertility;
		}
	}

	// Save/Load
	public void readFromNBT(NBTTagCompound data) {
		if(data.hasKey("fertility")) {
			setFertility(data.getFloat("fertility"));
		}
	}
	
	public NBTTagCompound writeToNBT(NBTTagCompound data) {
		data.setFloat("fertility", fertility);
		return data;
	}
	
	public void merge(RadiationHelper other) {
		fertility = Math.max(fertility, other.fertility);
	}

}
