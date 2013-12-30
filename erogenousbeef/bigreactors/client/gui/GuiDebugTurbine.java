package erogenousbeef.bigreactors.client.gui;

import cpw.mods.fml.common.network.PacketDispatcher;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;
import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.common.block.BlockBRSmallMachine;
import erogenousbeef.bigreactors.common.tileentity.TileEntityDebugTurbine;
import erogenousbeef.bigreactors.common.tileentity.base.TileEntityBeefBase;
import erogenousbeef.bigreactors.gui.controls.BeefGuiFluidBar;
import erogenousbeef.bigreactors.gui.controls.BeefGuiLabel;
import erogenousbeef.bigreactors.gui.controls.BeefGuiPowerBar;
import erogenousbeef.bigreactors.net.PacketWrapper;
import erogenousbeef.bigreactors.net.Packets;

public class GuiDebugTurbine extends BeefGuiSmallMachineBase {

	private TileEntityDebugTurbine turbine;
	
	private BeefGuiLabel titleString;
	private BeefGuiLabel rpmString;
	private BeefGuiLabel energyGennedString;
	
	private BeefGuiPowerBar powerBar;
	private BeefGuiFluidBar steamBar;
	private BeefGuiFluidBar waterBar;

	private GuiButton toggleActive;

	public GuiDebugTurbine(Container container, TileEntityDebugTurbine tileEntity) {
		super(container, tileEntity);
		
		turbine = tileEntity;
		xSize = 245;
		ySize = 175;
	}

	@Override
	public void initGui() {
		super.initGui();

		int leftX = guiLeft + 8;
		int topY = guiTop + 6;
		
		titleString = new BeefGuiLabel(this, turbine.getInvName(), leftX, topY);
		topY += titleString.getHeight() + 8;
		
		leftX += 22;
		rpmString = new BeefGuiLabel(this, "Speed: 0 RPM", leftX, topY);
		topY += rpmString.getHeight() + 4;
		
		energyGennedString = new BeefGuiLabel(this, "Generating: 0 RF/t", leftX, topY);
		topY += energyGennedString.getHeight() + 4;
		
		steamBar = new BeefGuiFluidBar(this, guiLeft + 8, guiTop + 16, turbine, TileEntityDebugTurbine.TANK_STEAM);
		waterBar = new BeefGuiFluidBar(this, guiLeft + 126, guiTop + 16, turbine, TileEntityDebugTurbine.TANK_WATER);
		powerBar = new BeefGuiPowerBar(this, guiLeft + 148, guiTop + 16, turbine);
		
		registerControl(titleString);
		registerControl(rpmString);
		registerControl(energyGennedString);
		registerControl(steamBar);
		registerControl(waterBar);
		registerControl(powerBar);
		
		toggleActive = new GuiButton(10, guiLeft + 58, guiTop + 70, 60, 20, "Activate");
		registerControl(toggleActive);

		createInventoryExposureButtons(guiLeft + 180, guiTop + 4);
	}

	@Override
	public void updateScreen() {
		super.updateScreen();

		rpmString.setLabelText(String.format("Speed: %.0f RPM", turbine.getTurbineSpeed()));
		energyGennedString.setLabelText(String.format("Generating: %2.2f RF/t", turbine.getEnergyGeneratedLastTick()));
		toggleActive.displayString = turbine.isActive() ? "Deactivate":"Activate";
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

	@Override
	public ResourceLocation getGuiBackground() {
		return new ResourceLocation(BigReactors.GUI_DIRECTORY + "CyaniteReprocessor.png");
	}

}
