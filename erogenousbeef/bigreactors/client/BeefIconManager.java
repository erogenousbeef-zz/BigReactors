package erogenousbeef.bigreactors.client;

import java.util.HashMap;

import erogenousbeef.bigreactors.common.BigReactors;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.Icon;

/**
 * Manages Icons that are not registered via blocks or items.
 * Useful for fluid icons and GUI icons.
 * 
 * @author Erogenous Beef
 * 
 */
public class BeefIconManager {

	public static final int TERRAIN_TEXTURE = 0;
	public static final int ITEM_TEXTURE = 1;
	
	private HashMap<String, Integer> nameToIdMap;
	private HashMap<Integer, Icon> idToIconMap;
	
	protected String[] iconNames = null;
	
	public BeefIconManager() {
		nameToIdMap = new HashMap<String, Integer>();
		idToIconMap = new HashMap<Integer, Icon>();
	}
	
	public void registerIcons(TextureMap textureMap) {
		if(iconNames == null) { return; }

		for(int i = 0; i < iconNames.length; i++) {
			nameToIdMap.put(iconNames[i], i);
			idToIconMap.put(i, textureMap.registerIcon(BigReactors.TEXTURE_NAME_PREFIX + getPath() + iconNames[i]));
		}
	}
	
	protected String getPath() { return ""; }
	
	public Icon getIcon(String name) {
		if(name == null || name.isEmpty()) { return null; }
		
		Integer id = nameToIdMap.get(name);
		if(id == null) {
			return null;
		}
		
		return idToIconMap.get(id);
	}
	
	public Icon getIcon(int id) {
		return idToIconMap.get(id);
	}
	
	public int getTextureType() { return TERRAIN_TEXTURE; }
	
}