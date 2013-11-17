package erogenousbeef.bigreactors.gui.controls;

import cofh.api.energy.IEnergyHandler;
import net.minecraft.block.Block;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.Icon;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeDirection;
import erogenousbeef.bigreactors.client.gui.BeefGuiBase;
import erogenousbeef.bigreactors.common.block.BlockBRSmallMachine;
import erogenousbeef.bigreactors.common.tileentity.base.TileEntityPoweredInventory;
import erogenousbeef.bigreactors.gui.IBeefTooltipControl;

public class BeefGuiPowerBar extends BeefGuiProgressBarVertical implements
		IBeefTooltipControl {

	IEnergyHandler _entity;
	
	public BeefGuiPowerBar(BeefGuiBase container, int x, int y, IEnergyHandler entity) {
		super(container, x, y);
		_entity = entity;
	}

	@Override
	protected Icon getProgressBarIcon() {
		return BlockBRSmallMachine.getPowerIcon();
	}
	
	@Override
	protected float getProgress() {
		return _entity.getEnergyStored(ForgeDirection.UNKNOWN) / _entity.getMaxEnergyStored(ForgeDirection.UNKNOWN);
	}
	
	@Override
	public String getTooltip() {
		return String.format("%d / %d RF", _entity.getEnergyStored(ForgeDirection.UNKNOWN), _entity.getMaxEnergyStored(ForgeDirection.UNKNOWN));
	}
	
	@Override
	protected ResourceLocation getResourceLocation() {
		return net.minecraft.client.renderer.texture.TextureMap.locationBlocksTexture;
	}
}
