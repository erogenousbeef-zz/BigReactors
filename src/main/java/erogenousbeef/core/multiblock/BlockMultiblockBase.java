package erogenousbeef.core.multiblock;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;

/*
 * Base class for multiblock-capable blocks. This is only a reference implementation
 * and can be safely ignored.
 */
public abstract class BlockMultiblockBase extends BlockContainer {

	//TODO: par1 == new id
	protected BlockMultiblockBase(String par1,Material par2Material) {
		super(par2Material);
	}
}
