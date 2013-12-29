package erogenousbeef.bigreactors.client.gui;

import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;
import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.common.block.BlockBRSmallMachine;
import erogenousbeef.bigreactors.common.tileentity.TileEntityDebugTurbine;
import erogenousbeef.bigreactors.common.tileentity.base.TileEntityBeefBase;
import erogenousbeef.bigreactors.gui.controls.BeefGuiLabel;

public class GuiDebugTurbine extends BeefGuiSmallMachineBase {

	private TileEntityDebugTurbine turbine;
	
	public GuiDebugTurbine(Container container, TileEntityDebugTurbine tileEntity) {
		super(container, tileEntity);
		
		turbine = tileEntity;
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
