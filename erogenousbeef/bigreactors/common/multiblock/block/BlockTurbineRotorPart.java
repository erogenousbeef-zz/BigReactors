package erogenousbeef.bigreactors.common.multiblock.block;

import java.util.List;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityTurbineRotorPart;

public class BlockTurbineRotorPart extends BlockContainer {

	public static final int METADATA_SHAFT = 0;
	public static final int METADATA_BLADE = 1;
	public static int renderId;

	private static final String[] _subBlocks = new String[] { "rotor",
																"blade"
															};

	private Icon[] _icons = new Icon[_subBlocks.length];

	public BlockTurbineRotorPart(int blockID, Material material) {
		super(blockID, material);

		setStepSound(soundMetalFootstep);
		setHardness(1.0f);
		setUnlocalizedName("blockTurbineRotorPart");
		this.setTextureName(BigReactors.TEXTURE_NAME_PREFIX + "blockTurbineRotorPart");
		setCreativeTab(BigReactors.TAB);
	}
	
	
	@Override
	public int getRenderType() {
		return renderId;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister par1IconRegister)
	{
		// Base icons
		for(int i = 0; i < _subBlocks.length; ++i) {
			_icons[i] = par1IconRegister.registerIcon(BigReactors.TEXTURE_NAME_PREFIX + getUnlocalizedName() + "." + _subBlocks[i]);
		}
	}
	
	@Override
	public Icon getIcon(int side, int metadata) {
		return _icons[metadata];
	}

	@Override
	public TileEntity createNewTileEntity(World world) {
		return null;
	}

	@Override
	public TileEntity createTileEntity(World world, int metadata) {
		return new TileEntityTurbineRotorPart();
	}

	@Override
	public boolean isOpaqueCube()
	{
		return false;
	}

	@Override
	public boolean renderAsNormalBlock()
	{
		return false;
	}

	@Override
	public int damageDropped(int metadata)
	{
		return metadata;
	}
	
	public ItemStack getItemStack(String name) {
		int metadata = -1;
		for(int i = 0; i < _subBlocks.length; i++) {
			if(_subBlocks[i].equals(name)) {
				metadata = i;
				break;
			}
		}
		
		if(metadata < 0) {
			throw new IllegalArgumentException("Unable to find a block with the name " + name);
		}
		
		return new ItemStack(blockID, 1, metadata);
	}
	
	@Override
	public void getSubBlocks(int par1, CreativeTabs par2CreativeTabs, List par3List)
	{
		for(int i = 0; i < _subBlocks.length; i++) {
			par3List.add(new ItemStack(blockID, 1, i));
		}
	}
	
	public int getRotorMass(int blockId, int metadata) {
		if(this.blockID == blockId) {
			switch(metadata) {
			// TODO: add masses when you add non-standard turbine parts
			default:
				return 10;
			}
		}
		
		return 0;
	}
	
	public static boolean isRotorBlade(int metadata) {
		return metadata == METADATA_BLADE;
	}
	
	public static boolean isRotorShaft(int metadata) {
		return metadata == METADATA_SHAFT;
	}
}
