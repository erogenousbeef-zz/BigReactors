package erogenousbeef.bigreactors.gui.controls;

import net.minecraft.block.Block;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.RenderEngine;
import net.minecraft.util.Icon;
import erogenousbeef.bigreactors.api.IBeefPowerStorage;
import erogenousbeef.bigreactors.client.gui.BeefGuiBase;
import erogenousbeef.bigreactors.common.block.BlockBRSmallMachine;
import erogenousbeef.bigreactors.common.tileentity.base.TileEntityPoweredInventory;
import erogenousbeef.bigreactors.gui.IBeefTooltipControl;

public class BeefGuiPowerBar extends BeefGuiProgressBarVertical implements
		IBeefTooltipControl {

	IBeefPowerStorage _entity;
	
	public BeefGuiPowerBar(BeefGuiBase container, int x, int y, IBeefPowerStorage entity) {
		super(container, x, y);
		_entity = entity;
	}

	@Override
	protected Icon getProgressBarIcon() {
		return BlockBRSmallMachine.getPowerIcon();
	}
	
	@Override
	protected float getProgress() {
		return (float)_entity.getEnergyStored() / (float)_entity.getMaxEnergyStored();
	}
	
	@Override
	public String getTooltip() {
		return String.format("%d / %d MJ", _entity.getEnergyStored(), _entity.getMaxEnergyStored());
	}
}
