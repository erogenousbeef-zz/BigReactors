package erogenousbeef.bigreactors.gui.controls;

import net.minecraft.util.Icon;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import erogenousbeef.bigreactors.client.gui.BeefGuiBase;
import erogenousbeef.bigreactors.common.tileentity.base.TileEntityPoweredInventoryFluid;
import erogenousbeef.bigreactors.gui.IBeefTooltipControl;

public class BeefGuiFluidBar extends BeefGuiProgressBarVertical implements
		IBeefTooltipControl {

	TileEntityPoweredInventoryFluid _entity;
	int tankIdx;
	
	public BeefGuiFluidBar(BeefGuiBase container, int x, int y,
			TileEntityPoweredInventoryFluid entity, int tankIdx) {
		super(container, x, y);
		
		this._entity = entity;
		this.tankIdx = tankIdx;
	}

	@Override
	protected Icon getProgressBarIcon() {
		FluidTankInfo[] tanks = this._entity.getTankInfo(ForgeDirection.UNKNOWN);
		if(tanks != null && tankIdx < tanks.length) {
			if(tanks[tankIdx].fluid != null) {
				return tanks[tankIdx].fluid.getFluid().getIcon();
			}
		}
		return null;
	}
	
	@Override
	protected float getProgress() {
		FluidTankInfo[] tanks = this._entity.getTankInfo(ForgeDirection.UNKNOWN);
		if(tanks != null && tankIdx < tanks.length) {
			FluidStack tankFluid = tanks[tankIdx].fluid;
			if(tankFluid != null) {
				return (float)tankFluid.amount / (float)tanks[tankIdx].capacity;
			}
		}
		return 0.0f;
	}
	
	@Override
	public String getTooltip() {
		FluidTankInfo[] tanks = this._entity.getTankInfo(ForgeDirection.UNKNOWN);
		if(tanks != null && tankIdx < tanks.length) {
			FluidStack tankFluid = tanks[tankIdx].fluid;
			if(tankFluid != null) {
				return String.format("%d / %d mB", tankFluid.amount, tanks[tankIdx].capacity);
			}
			else {
				return String.format("0 / %d mB", tanks[tankIdx].capacity);
			}
		}
		return null;
	}

	@Override
	protected ResourceLocation getResourceLocation() {
		return net.minecraft.client.renderer.texture.TextureMap.locationBlocksTexture;
	}
}
