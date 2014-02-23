package erogenousbeef.bigreactors.common.block;

import java.util.List;

import cpw.mods.fml.common.registry.GameRegistry;

import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.common.item.ItemIngot;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;
import net.minecraftforge.oredict.OreDictionary;

public class BlockBRMetal extends Block {

	public static final int METADATA_YELLORIUM 	= 0;
	public static final int METADATA_CYANITE 	= 1;
	public static final int METADATA_GRAPHITE 	= 2;
	public static final int METADATA_BLUTONIUM 	= 3;
	
	private static final String[] _subBlocks = new String[] { "blockYellorium", "blockCyanite", "blockGraphite", "blockBlutonium" };
	private static final String[] _materials = new String[] { "Yellorium", "Cyanite", "Graphite", "Blutonium" };
	private Icon[] _icons = new Icon[_subBlocks.length];
	private static final int NUM_BLOCKS = _subBlocks.length;
	
	public BlockBRMetal(int id) {
		super(id, Material.iron);
		this.setCreativeTab(BigReactors.TAB);
		this.setUnlocalizedName("brMetal");
		this.setHardness(2f);
	}

	@Override
	public Icon getIcon(int side, int metadata)
	{
		metadata = Math.max(0, Math.min(3, metadata));
		return _icons[metadata];
	}
	
	@Override
	public void registerIcons(IconRegister iconRegister)
	{
		for(int i = 0; i < NUM_BLOCKS; i++) {
			_icons[i] = iconRegister.registerIcon(BigReactors.TEXTURE_NAME_PREFIX + getUnlocalizedName() + "." + _subBlocks[i]);
		}
	}

	@Override
	public int damageDropped(int metadata)
	{
		return metadata;
	}

	@Override
	public void getSubBlocks(int id, CreativeTabs par2CreativeTabs, List par3List)
	{
		for(int i = 0; i < NUM_BLOCKS; i++) {
			par3List.add(new ItemStack(id, 1, i));
		}
	}
	
	public ItemStack getItemStackForMaterial(String name) {
		int i = 0;

		for(i = 0; i < NUM_BLOCKS; i++) {
			if(name.equals(_materials[i])) {
				break;
			}
		}
		
		return new ItemStack(blockID, 1, i);
	}
	
	public void registerOreDictEntries() {
		for(int i = 0; i < NUM_BLOCKS; i++) {
			OreDictionary.registerOre(_subBlocks[i], new ItemStack(blockID, 1, i));
		}
	}

	public void registerIngotRecipes(ItemIngot ingotItem) {
		for(int i = 0; i < NUM_BLOCKS; i++) {
			ItemStack block = new ItemStack(blockID, 1, i);
			ItemStack ingot = ingotItem.getIngotItemStackForMaterial(_materials[i]);
			GameRegistry.addShapelessRecipe(block, ingot, ingot, ingot, ingot, ingot, ingot, ingot, ingot, ingot);
			ingot.stackSize = 9;
			GameRegistry.addShapelessRecipe(ingot, block);
		}
	}
}
