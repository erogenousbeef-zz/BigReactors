package erogenousbeef.bigreactors.gui;

import io.netty.buffer.ByteBuf;

import java.io.DataInputStream;
import java.io.IOException;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

/**
 * An internal interface for my internal GUI framework.
 * Handles network UI updates and basic button presses.
 */
public interface IBeefGuiEntity {
	/**
	 * Retrieve the GUIScreen for this block
	 * @param player The player opening the screen
	 * @return A GUIScreen to show
	 */
	public GuiScreen getGUI(EntityPlayer player);
	
	/**
	 * Retrieve the Container object for this block.
	 * @param player The player opening the GUI/container.
	 * @return A Container for use by the server/
	 */
	public Container getContainer(EntityPlayer player);
	
	/**
	 * Call when a player should begin receiving UI updates
	 * @param player Player who should begin receiving UI updates
	 */
	public void beginUpdatingPlayer(EntityPlayer player);
	
	/**
	 * Call when a player should stop receiving UI updates
	 * @param player Player who should no longer receive UI updates
	 */
	public void stopUpdatingPlayer(EntityPlayer player);
	
	/**
	 * Called on the server when a player presses a button in the UI
	 * @param buttonName Name of the button, as specified in client code
	 * @param dataStream Stream containing additional parameters, if any
	 * @throws IOException
	 */
	public void onReceiveGuiButtonPress(String buttonName, ByteBuf dataStream);
}
