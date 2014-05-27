package erogenousbeef.test.common;

import net.minecraft.item.ItemBlock;

public class ItemBlockMultiblockTester extends ItemBlock {

	public ItemBlockMultiblockTester(int id) {
		super(id);
		this.setMaxDamage(0);
		this.setHasSubtypes(false);
	}
}
