package erogenousbeef.bigreactors.common.item;

import java.util.ArrayList;
import java.util.List;

import cofh.core.util.oredict.OreDictionaryArbiter;
import cofh.lib.util.helpers.ItemHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import erogenousbeef.bigreactors.common.BRLog;
import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.common.interfaces.IBeefDebuggableTile;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

public class ItemBeefDebugTool extends ItemBase {

	public ItemBeefDebugTool() {
		super("beefDebugTool");
		setMaxStackSize(1);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer player, List infoList, boolean advancedTooltips) {
		super.addInformation(stack, player, infoList, advancedTooltips);
		infoList.add(StatCollector.translateToLocal("beefDebugTool.debuginfo"));
		infoList.add("");
		infoList.add(EnumChatFormatting.ITALIC + StatCollector.translateToLocal("beefDebugTool.queriesserver"));
		infoList.add(EnumChatFormatting.GREEN + "Shift: " + EnumChatFormatting.GRAY + EnumChatFormatting.ITALIC + StatCollector.translateToLocal("beefDebugTool.queryclient"));
	}

	@Override
	public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world,
			int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
		if(player.isSneaking() != world.isRemote) {
			return false;
		}
		
		String clientOrServer = world.isRemote ? "CLIENT":"SERVER";

		TileEntity te = world.getTileEntity(x, y, z);
		if(te instanceof IBeefDebuggableTile) {
			String result = ((IBeefDebuggableTile)te).getDebugInfo();
			if(result != null && !result.isEmpty()) {
				String[] results = result.split("\n");
				String initialMessage = String.format("[%s] Beef Debug Tool:", clientOrServer);
				player.addChatMessage(new ChatComponentText(initialMessage));
				for(String r : results) {
					player.addChatMessage(new ChatComponentText(r));
				}
				return true;
			}
		}

		Block b = world.getBlock(x, y, z);
		if(b != null) {
			ItemStack blockStack = new ItemStack(b, 1, world.getBlockMetadata(x,y,z));
			String oreName = ItemHelper.oreProxy.getOreName(blockStack);
			player.addChatMessage(new ChatComponentTranslation("beefDebugTool.canonicalore", world.isRemote?"CLIENT":"SERVER", b.getUnlocalizedName(), oreName));

			ArrayList<String> allOreNames = OreDictionaryArbiter.getAllOreNames(blockStack);
			if(allOreNames != null) {
				player.addChatMessage(new ChatComponentTranslation("beefDebugTool.allorenames", clientOrServer, allOreNames.size()));
				for(String on : allOreNames) {
					player.addChatMessage(new ChatComponentText(on));
				}
			}
			else {
				player.addChatMessage(new ChatComponentText("getAllOreNames returned null"));
			}
		}

		// Consume clicks by default
		return true;
	}

	@Override
	public boolean doesSneakBypassUse(World world, int x, int y, int z, EntityPlayer player)
	{
		return false;
	}
	
	/*
	@Override
	public boolean canHarvestBlock(Block block, ItemStack stack)
	{
		return false;
	}
	*/
}
