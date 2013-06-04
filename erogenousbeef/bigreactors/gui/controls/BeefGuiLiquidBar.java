package erogenousbeef.bigreactors.gui.controls;

import net.minecraft.util.Icon;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.liquids.ILiquidTank;
import net.minecraftforge.liquids.LiquidStack;
import erogenousbeef.bigreactors.client.gui.BeefGuiBase;
import erogenousbeef.bigreactors.common.tileentity.base.TileEntityPoweredInventoryLiquid;
import erogenousbeef.bigreactors.gui.IBeefTooltipControl;

public class BeefGuiLiquidBar extends BeefGuiProgressBarVertical implements
		IBeefTooltipControl {

	TileEntityPoweredInventoryLiquid _entity;
	int tankIdx;
	
	public BeefGuiLiquidBar(BeefGuiBase container, int x, int y,
			TileEntityPoweredInventoryLiquid entity, int tankIdx) {
		super(container, x, y);
		
		this._entity = entity;
		this.tankIdx = tankIdx;
	}

	@Override
	protected Icon getProgressBarIcon() {
		ILiquidTank[] tanks = this._entity.getTanks(ForgeDirection.UNKNOWN);
		if(tanks != null && tankIdx < tanks.length) {
			LiquidStack tankLiquid = tanks[tankIdx].getLiquid();
			if(tankLiquid != null) {
				return tankLiquid.getRenderingIcon();
			}
		}
		return null;
	}
	
	@Override
	protected String getTextureSheet() {
		ILiquidTank[] tanks = this._entity.getTanks(ForgeDirection.UNKNOWN);
		if(tanks != null && tankIdx < tanks.length) {
			LiquidStack tankLiquid = tanks[tankIdx].getLiquid();
			if(tankLiquid != null) {
				return tankLiquid.getTextureSheet();
			}
		}
		return null;
		
	}
	
	@Override
	protected float getProgress() {
		ILiquidTank[] tanks = this._entity.getTanks(ForgeDirection.UNKNOWN);
		if(tanks != null && tankIdx < tanks.length) {
			LiquidStack tankLiquid = tanks[tankIdx].getLiquid();
			if(tankLiquid != null) {
				return (float)tankLiquid.amount / (float)tanks[tankIdx].getCapacity();
			}
		}
		return 0.0f;
	}
	
	@Override
	public String getTooltip() {
		ILiquidTank[] tanks = this._entity.getTanks(ForgeDirection.UNKNOWN);
		if(tanks != null && tankIdx < tanks.length) {
			LiquidStack tankLiquid = tanks[tankIdx].getLiquid();
			if(tankLiquid != null) {
				return String.format("%d / %d mB", tankLiquid.amount, tanks[tankIdx].getCapacity());
			}
			else {
				return String.format("0 / %d mB", tanks[tankIdx].getCapacity());
			}
		}
		return null;
	}

}
