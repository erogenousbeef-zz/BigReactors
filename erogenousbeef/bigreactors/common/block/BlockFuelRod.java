package erogenousbeef.bigreactors.common.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.common.tileentity.TileEntityFuelRod;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockFuelRod extends BlockContainer {

	@SideOnly(Side.CLIENT)
	private Icon iconFuelRodSide;
	@SideOnly(Side.CLIENT)
	private Icon iconFuelRodTopBottom;
	
	public BlockFuelRod(int id, Material material) {
		super(id, material);
		
		this.setLightValue(0.9f);
		this.setLightOpacity(1);
		this.setCreativeTab(BigReactors.TAB);
		this.setUnlocalizedName("yelloriumFuelRod");
		this.setTextureName(BigReactors.TEXTURE_NAME_PREFIX + "yelloriumFuelRod");
	}

	@SideOnly(Side.CLIENT)
	@Override
	public Icon getIcon(int side, int metadata)
	{
		if(side == 0 || side == 1) { return this.iconFuelRodTopBottom; }
		
		return this.iconFuelRodSide;
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public Icon getBlockTexture(IBlockAccess iblockaccess, int x, int y, int z, int side) {
		if(side == 0 || side == 1) { return this.iconFuelRodTopBottom; }
		else { return this.iconFuelRodSide; }
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerIcons(IconRegister par1IconRegister)
	{
		this.iconFuelRodSide = par1IconRegister.registerIcon(BigReactors.TEXTURE_NAME_PREFIX + "fuelRod.side");
		this.iconFuelRodTopBottom = par1IconRegister.registerIcon(BigReactors.TEXTURE_NAME_PREFIX + "fuelRod.end");
	}
	
	@Override
	public boolean isOpaqueCube() { return false; }
	
	@Override
	public TileEntity createNewTileEntity(World world) {
		return null;
	}
	
	@Override
	public TileEntity createTileEntity(World world, int metadata) {
		return new TileEntityFuelRod();
	}
}
