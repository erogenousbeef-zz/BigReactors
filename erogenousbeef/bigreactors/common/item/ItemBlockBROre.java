package erogenousbeef.bigreactors.common.item;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class ItemBlockBROre extends ItemBlock {

	private String[] ores = { "Yellorium Ore" };

	public ItemBlockBROre(int id)
	{
		super(id);
		this.setMaxDamage(0);
		this.setHasSubtypes(true);
	}

	@Override
	public int getMetadata(int damage)
	{
		return damage;
	}

	@Override
	public String getUnlocalizedName(ItemStack par1ItemStack)
	{
		return Block.blocksList[this.getBlockID()].getUnlocalizedName() + "." + (par1ItemStack.getItemDamage());
	}

	@Override
	public String getUnlocalizedName()
	{
		return Block.blocksList[this.getBlockID()].getUnlocalizedName() + ".0";
	}	

}
