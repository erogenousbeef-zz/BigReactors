package erogenousbeef.bigreactors.common.item;

import net.minecraft.item.ItemBlock;

public class ItemBlockBigReactors extends ItemBlock {

		public ItemBlockBigReactors(int id) {
			super(id);
		}
		
		@Override
		public int getMetadata(int i) { 
			return i;
		}
}
