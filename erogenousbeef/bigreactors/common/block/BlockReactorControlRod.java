package erogenousbeef.bigreactors.common.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import erogenousbeef.bigreactors.common.BRLoader;
import erogenousbeef.bigreactors.common.BRUtilities;
import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.common.tileentity.TileEntityReactorControlRod;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockReactorControlRod extends BlockContainer {

	protected Icon topIcon;
	
	public BlockReactorControlRod(int id, Material material) {
		super(id, material);
		
		this.setHardness(1.0f);
		this.setUnlocalizedName("blockReactorControlRod");
		this.setCreativeTab(BigReactors.TAB);
	}

	@Override
	public TileEntity createNewTileEntity(World world) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TileEntity createTileEntity(World world, int metadata) {
		return new TileEntityReactorControlRod();
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister par1IconRegister)
	{
		// TODO: fix
		this.blockIcon = par1IconRegister.registerIcon(BigReactors.TEXTURE_NAME_PREFIX + "tile.blockBRSmallMachine");
		this.topIcon = par1IconRegister.registerIcon(BigReactors.TEXTURE_NAME_PREFIX + getUnlocalizedName());
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIcon(int side, int metadata)
	{
		if(side == 1) { return this.topIcon; }
		
		return this.blockIcon;
	}

	@Override
	public boolean renderAsNormalBlock() { return false; }
	
	@Override
	public boolean isOpaqueCube() { return false; }
	
	// DEBUG CODE BELOW HERE
	
	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityPlayer, int side, float hitX, float hitY, float hitZ) {
		if(entityPlayer.isSneaking()) {
			return false;
		}
		
		// Debug: Direct insertion of ingots
		ItemStack currentItem = entityPlayer.inventory.getCurrentItem();
		TileEntity te = world.getBlockTileEntity(x, y, z);
		if(currentItem != null && te != null && te instanceof TileEntityReactorControlRod) {
			TileEntityReactorControlRod rod = ((TileEntityReactorControlRod)te);
			
			int ingotsUsed = 0;
			if(rod.addFuel(currentItem, TileEntityReactorControlRod.fuelPerIngot * currentItem.stackSize, false) > 0) {
				int fuelAdded = rod.addFuel(currentItem, TileEntityReactorControlRod.fuelPerIngot * currentItem.stackSize, true);
				ingotsUsed += Math.ceil((double)fuelAdded / (double) TileEntityReactorControlRod.fuelPerIngot);
			}
			else if(rod.addWaste(currentItem, TileEntityReactorControlRod.fuelPerIngot * currentItem.stackSize, false) > 0) {
				int wasteAdded = rod.addWaste(currentItem, TileEntityReactorControlRod.fuelPerIngot * currentItem.stackSize, true);
				ingotsUsed += Math.ceil((double)wasteAdded / (double) TileEntityReactorControlRod.fuelPerIngot);
			}

			if(ingotsUsed > 0) {
				currentItem = BRUtilities.consumeItem(currentItem, ingotsUsed);
				return true;
			}
		}
		
		// Open debug GUI
		entityPlayer.openGui(BRLoader.instance, 0, world, x, y, z);
		return true;
	}
}
