package erogenousbeef.bigreactors.utils.intermod;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import cpw.mods.fml.common.event.FMLInterModComms;

/**
 * Helper class for sending IMC messages to other mods.
 * MUST be called on both client & server.
 * MUST be called during init(), not preInit() or postInit()
 * 
 * @author Erogenous Beef
 *
 */
public class IMCHelper {

	protected static void sendInterModMessage(String to, String type, NBTTagCompound message) {
		FMLInterModComms.sendMessage(to, type, message);
	}
	
	public static class ThermalExpansion {
		public static void addPulverizerRecipe(int energy, ItemStack input, ItemStack primaryOutput) {

			if (input == null || primaryOutput == null) {
				return;
			}
			NBTTagCompound toSend = new NBTTagCompound();

			toSend.setInteger("energy", energy);
			toSend.setTag("input", new NBTTagCompound());
			toSend.setTag("primaryOutput", new NBTTagCompound());
			toSend.setTag("secondaryOutput", new NBTTagCompound());

			input.writeToNBT(toSend.getCompoundTag("input"));
			primaryOutput.writeToNBT(toSend.getCompoundTag("primaryOutput"));

			FMLInterModComms.sendMessage("ThermalExpansion", "PulverizerRecipe", toSend);
		}
		
		public static void addSmelterRecipe(int energy, ItemStack primaryInput, ItemStack secondaryInput, ItemStack primaryOutput) {

			if (primaryInput == null || secondaryInput == null || primaryOutput == null) {
				return;
			}
			NBTTagCompound toSend = new NBTTagCompound();

			toSend.setInteger("energy", energy);
			toSend.setTag("primaryInput", new NBTTagCompound());
			toSend.setTag("secondaryInput", new NBTTagCompound());
			toSend.setTag("primaryOutput", new NBTTagCompound());
			toSend.setTag("secondaryOutput", new NBTTagCompound());

			primaryInput.writeToNBT(toSend.getCompoundTag("primaryInput"));
			secondaryInput.writeToNBT(toSend.getCompoundTag("secondaryInput"));
			primaryOutput.writeToNBT(toSend.getCompoundTag("primaryOutput"));

			FMLInterModComms.sendMessage("ThermalExpansion", "SmelterRecipe", toSend);
		}
	}
	
	/// MineFactory Reloaded
	public static class MFR {
		public static void addOreToMiningLaserFocus(ItemStack stack, int color) {
	        NBTTagCompound laserOreMsg = new NBTTagCompound();
	        stack.writeToNBT(laserOreMsg);
	        laserOreMsg.setInteger("value", color);
	        IMCHelper.sendInterModMessage("MineFactoryReloaded", "registerLaserOre", laserOreMsg);

		}
		
		public static void setMiningLaserFocusPreferredOre(ItemStack stack, int color) {
			
		}
	}
	
	/// Applied Energistics 2
	public static class AE2 {
		public static void addGrinderRecipe(ItemStack input, ItemStack output, int turns) {
			NBTTagCompound msg = new NBTTagCompound();
			NBTTagCompound in = new NBTTagCompound();
			NBTTagCompound out = new NBTTagCompound();

			input.writeToNBT( in );
			output.writeToNBT( out );

			msg.setTag( "in", in );
			msg.setTag( "out", out );
			msg.setInteger( "turns", turns );
			
			sendInterModMessage("appliedenergistics2", "add-grindable", msg);
		}
	}		
}
