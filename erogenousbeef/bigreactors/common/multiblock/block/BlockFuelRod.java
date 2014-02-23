package erogenousbeef.bigreactors.common.multiblock.block;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityReactorFuelRod;

public class BlockFuelRod extends BlockContainer {

	public static int renderId;

	@SideOnly(Side.CLIENT)
	private Icon iconFuelRodSide;
	@SideOnly(Side.CLIENT)
	private Icon iconFuelRodTopBottom;
	
	public BlockFuelRod(int id, Material material) {
		super(id, material);
		
		setHardness(4f);
		setLightValue(0.9f);
		setLightOpacity(1);
		setCreativeTab(BigReactors.TAB);
		setUnlocalizedName("yelloriumFuelRod");
		setTextureName(BigReactors.TEXTURE_NAME_PREFIX + "yelloriumFuelRod");
	}

	@Override
	public int getRenderType() {
		return renderId;
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
		return new TileEntityReactorFuelRod();
	}
	
	/*
	 * TODO Have to make my own particle for this. :/
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(World world, int x, int y, int z, Random par5Random)
    {
    	TileEntity te = world.getBlockTileEntity(x, y, z);
    	if(te instanceof TileEntityReactorFuelRod) {
    		TileEntityReactorFuelRod fuelRod = (TileEntityReactorFuelRod)te;
    		MultiblockReactor reactor = fuelRod.getReactorController();
    		if(reactor != null && reactor.isActive() && reactor.getFuelConsumedLastTick() > 0) {
    			int numParticles = par5Random.nextInt(4) + 1;
    			while(numParticles > 0) {
                    world.spawnParticle(BigReactors.isValentinesDay ? "heart" : "crit",
                    		fuelRod.xCoord + 0.5D,
                    		fuelRod.yCoord + 0.5D,
                    		fuelRod.zCoord + 0.5D,
                    		par5Random.nextFloat() * 3f - 1.5f,
                    		par5Random.nextFloat() * 3f - 1.5f,
                    		par5Random.nextFloat() * 3f - 1.5f);
    				numParticles--;
    			}
    		}
    	}
    }
     */
}
