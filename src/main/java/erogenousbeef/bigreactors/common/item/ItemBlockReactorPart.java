package erogenousbeef.bigreactors.common.item;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.common.multiblock.block.BlockReactorPart;

public class ItemBlockReactorPart extends ItemBlock {

	public ItemBlockReactorPart(Block block)
	{
		super(block);
		this.setMaxDamage(0);
		this.setHasSubtypes(true);
	}

	@Override
	public int getMetadata(int damage)
	{
		return damage;
	}
	
	@Override
	public String getUnlocalizedName(ItemStack itemstack)
	{
		int metadata = 0;
		int damage = itemstack.getItemDamage();
		
		if (BlockReactorPart.isCasing(damage))
		{
			metadata = 0;
		}
		else if (BlockReactorPart.isController(damage)) {
			metadata = 1;
		}
		else if (BlockReactorPart.isPowerTap(damage)){
			metadata = 2;
		}
		else if (BlockReactorPart.isAccessPort(damage)) {
			metadata = 3;
		}
		else if (BlockReactorPart.isRedNetPort(damage)) {
			metadata = 4;
		}
		else if (BlockReactorPart.isComputerPort(damage)) {
			metadata = 5;
		}
		else if (BlockReactorPart.isCoolantPort(damage)) {
			metadata = 6;
		}

		return BigReactors.blockReactorPart.getUnlocalizedName() + "." + metadata;
	}

	@Override
	public String getUnlocalizedName()
	{
		return BigReactors.blockReactorPart.getUnlocalizedName() + ".0";
	}	
}
