package erogenousbeef.bigreactors.client.gui;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.network.PacketDispatcher;
import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.common.block.BlockBRSmallMachine;
import erogenousbeef.bigreactors.common.tileentity.TileEntitySteamCreator;
import erogenousbeef.bigreactors.common.tileentity.base.TileEntityInventory;
import erogenousbeef.bigreactors.gui.controls.BeefGuiFluidBar;
import erogenousbeef.bigreactors.gui.controls.BeefGuiLabel;
import erogenousbeef.bigreactors.gui.controls.BeefGuiPowerBar;
import erogenousbeef.bigreactors.gui.controls.BeefGuiProgressArrow;
import erogenousbeef.bigreactors.gui.controls.GuiIconButton;
import erogenousbeef.bigreactors.net.PacketWrapper;
import erogenousbeef.bigreactors.net.Packets;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeDirection;

public class GuiHeatGenerator extends BeefGuiSmallMachineBase {

	private TileEntitySteamCreator _entity;

	private BeefGuiLabel titleString;
	private BeefGuiLabel tempString;
	private BeefGuiLabel absorbedString;

	private GuiButton toggleActive;
	
	private BeefGuiFluidBar steamBar;
	private BeefGuiFluidBar fluidBar;
	private BeefGuiProgressArrow progressArrow;
	
	public GuiHeatGenerator(Container container, TileEntitySteamCreator entity) {
		super(container, entity);
		
		_entity = entity;
		xSize = 241;
		ySize = 175;
	}
	
	@Override
	public void initGui() {
		super.initGui();

		int leftX = guiLeft + 8;
		int topY = guiTop + 6;
		
		titleString = new BeefGuiLabel(this, "Water-Steam Thingus", leftX, topY);
		topY += titleString.getHeight() + 8;
		
		tempString = new BeefGuiLabel(this, "Temp: ???", leftX+22, topY);
		topY += tempString.getHeight() + 4;
		
		absorbedString = new BeefGuiLabel(this, "Int-NRG: ???", leftX+22, topY);
		topY += absorbedString.getHeight() + 4;

		fluidBar = new BeefGuiFluidBar(this, guiLeft + 8, guiTop + 16, _entity, 0);
		steamBar = new BeefGuiFluidBar(this, guiLeft + 148, guiTop + 16, _entity, 1);
		progressArrow = new BeefGuiProgressArrow(this, guiLeft + 76, guiTop + 41, 0, 178, _entity);

		registerControl(titleString);
		registerControl(tempString);
		registerControl(absorbedString);
		registerControl(fluidBar);
		registerControl(steamBar);
		registerControl(progressArrow);
		
		toggleActive = new GuiButton(10, guiLeft + 58, guiTop + 70, 60, 20, "On/Off");
		registerControl(toggleActive);

		createInventoryExposureButtons(guiLeft + 180, guiTop + 4);
	}

	@Override
	public void updateScreen() {
		super.updateScreen();

		tempString.setLabelText(String.format("Temperature: %.1fC", _entity.internalTemperature-273f));
		absorbedString.setLabelText(String.format("Int-NRG: %.2f", _entity.energyAbsorbed));
	}

	@Override
	public ResourceLocation getGuiBackground() {
		return new ResourceLocation(BigReactors.GUI_DIRECTORY + "CyaniteReprocessor.png");
	}
	
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float gameTicks) {
		super.drawScreen(mouseX, mouseY, gameTicks);
	}
	
	@Override
	protected void actionPerformed(GuiButton button) {
		super.actionPerformed(button);
		if(button.id == 10) {
			PacketDispatcher.sendPacketToServer(PacketWrapper.createPacket(BigReactors.CHANNEL, Packets.BeefGuiButtonPress,
					new Object[] { _entity.xCoord, _entity.yCoord, _entity.zCoord, "active", 0 }));
		}
	}
	
	@Override
	protected int getBlockMetadata() {
		return BlockBRSmallMachine.META_CYANITE_REPROCESSOR;
	}
}
