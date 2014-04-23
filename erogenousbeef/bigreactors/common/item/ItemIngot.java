package erogenousbeef.bigreactors.common.item;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

public class ItemIngot extends ItemBase
{
	public static final int DUST_OFFSET = 4;
	public static final String[] TYPES = { "ingotYellorium", "ingotCyanite", "ingotGraphite", "ingotBlutonium",
											"dustYellorium", "dustCyanite", "dustGraphite", "dustBlutonium" };

	public static final String[] MATERIALS = { "Yellorium", "Cyanite", "Graphite", "Blutonium" };

	public ItemIngot(int id)
	{
		super("ingot", id);
		this.setHasSubtypes(true);
		this.setMaxDamage(0);
	}

	@Override
	protected int getNumberOfSubItems() {
		return TYPES.length;
	}
	
	@Override
	protected String[] getSubItemNames() {
		return TYPES;
	}

	@Override
	public int getMetadata(int damage)
	{
		return damage;
	}

	@Override
	public String getUnlocalizedName(ItemStack itemStack)
	{
		int idx = Math.max(TYPES.length, itemStack.getItemDamage());
		return "item." + TYPES[idx];
	}

	@Override
	public void getSubItems(int par1, CreativeTabs creativeTabs, List list)
	{
		for (int i = 0; i < TYPES.length; i++)
		{
			list.add(new ItemStack(this, 1, i));
		}
	}

	public static boolean isFuel(int itemDamage) {
		return itemDamage == 0 || itemDamage == 3;
	}

	public static boolean isWaste(int itemDamage) {
		return itemDamage == 1;
	}
	
	public static boolean isGraphite(int itemDamage) {
		return itemDamage == 2;
	}
	
	public ItemStack getItemStackForType(String typeName) {
		for(int i = 0; i < TYPES.length; i++) {
			if(TYPES[i].equals(typeName)) {
				return new ItemStack(this, 1, i);
			}
		}

		return null;
	}
	
	public ItemStack getIngotItemStackForMaterial(String name) {
		int i = 0;
		for(i = 0; i < MATERIALS.length; i++) {
			if(name.equals(MATERIALS[i])) {
				break;
			}
		}
		
		return new ItemStack(this, 1, i);
	}
}
