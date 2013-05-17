package erogenousbeef.bigreactors.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.inventory.Container;
import net.minecraftforge.liquids.LiquidContainerRegistry;
import net.minecraftforge.liquids.LiquidStack;
import cpw.mods.fml.common.network.PacketDispatcher;
import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.common.block.BlockReactorPart;
import erogenousbeef.bigreactors.common.tileentity.TileEntityCyaniteReprocessor;
import erogenousbeef.bigreactors.common.tileentity.TileEntityReactorAccessPort;
import erogenousbeef.bigreactors.gui.controls.BeefGuiLabel;
import erogenousbeef.bigreactors.net.PacketWrapper;
import erogenousbeef.bigreactors.net.Packets;

public class GuiCyaniteReprocessor extends BeefGuiBase {

	private GuiButton _togglePort;
	private TileEntityCyaniteReprocessor _entity;

	private BeefGuiLabel titleString;
	private BeefGuiLabel powerStoredString;
	private BeefGuiLabel waterStoredString;
	private BeefGuiLabel progressString;
	
	public GuiCyaniteReprocessor(Container container, TileEntityCyaniteReprocessor entity) {
		super(container);
		
		_entity = entity;
	}
	
	@Override
	public void initGui() {
		super.initGui();
		
		int leftX = 4;
		int topY = 4;
		
		titleString = new BeefGuiLabel(this, _entity.getInvName(), leftX, topY);
		topY += titleString.getHeight() + 4;
		
		powerStoredString = new BeefGuiLabel(this, "Stored Power: -- updating --", leftX, topY);
		topY += powerStoredString.getHeight() + 4;
		
		waterStoredString = new BeefGuiLabel(this, "Stored Water: -- updating --", leftX, topY);
		topY += waterStoredString.getHeight() + 4;
		
		progressString = new BeefGuiLabel(this, "Progress: -- updating --", leftX, topY);
		topY += progressString.getHeight() + 4;
		
		registerControl(titleString);
		registerControl(powerStoredString);
		registerControl(waterStoredString);
		registerControl(progressString);
	}

	@Override
	protected String getGuiBackground() {
		return BigReactors.GUI_DIRECTORY + "CyaniteReprocessor.png";
	}

	@Override
	public void updateScreen() {
		super.updateScreen();

		powerStoredString.setLabelText(String.format("Stored Power: %d MJ", _entity.getEnergyStored()));
		
		LiquidStack waterStack = _entity.drain(0, _entity.getTankSize(0), false);
		
		
		float waterAmt = 0f;
		if(waterStack != null) {
			waterAmt = (float)waterStack.amount / (float)LiquidContainerRegistry.BUCKET_VOLUME;
		}
		waterStoredString.setLabelText(String.format("Stored Water: %1.1f Buckets", waterAmt));
		
		if(_entity.isActive()) {
			progressString.setLabelText(String.format("Progress: %2.1f", _entity.getCycleCompletion() * 100f));
		}
		else {
			progressString.setLabelText(String.format("Progress: Inactive"));
		}
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float gameTicks) {
		super.drawScreen(mouseX, mouseY, gameTicks);
	}	
}
