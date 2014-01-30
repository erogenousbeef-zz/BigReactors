package erogenousbeef.bigreactors.gui.controls;

import net.minecraft.util.Icon;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import erogenousbeef.bigreactors.client.gui.BeefGuiBase;
import erogenousbeef.bigreactors.common.interfaces.IMultipleFluidHandler;
import erogenousbeef.bigreactors.common.tileentity.base.TileEntityPoweredInventoryFluid;
import erogenousbeef.bigreactors.gui.IBeefTooltipControl;

public class BeefGuiFluidBar extends BeefGuiIconProgressBar implements
		IBeefTooltipControl {

	IMultipleFluidHandler _entity;
	int tankIdx;
	
	public BeefGuiFluidBar(BeefGuiBase container, int x, int y,
			IMultipleFluidHandler entity, int tankIdx) {
		super(container, x, y);
		
		this._entity = entity;
		this.tankIdx = tankIdx;
	}

	@Override
	protected Icon getProgressBarIcon() {
		FluidTankInfo[] tanks = this._entity.getTankInfo();
		if(tanks != null && tankIdx < tanks.length) {
			if(tanks[tankIdx].fluid != null) {
				return tanks[tankIdx].fluid.getFluid().getIcon();
			}
		}
		return null;
	}
	
	@Override
	protected float getProgress() {
		FluidTankInfo[] tanks = this._entity.getTankInfo();
		if(tanks != null && tankIdx < tanks.length) {
			FluidStack tankFluid = tanks[tankIdx].fluid;
			if(tankFluid != null) {
				return (float)tankFluid.amount / (float)tanks[tankIdx].capacity;
			}
		}
		return 0.0f;
	}
	
	@Override
	public String[] getTooltip() {
		if(!visible) { return null; }

		FluidTankInfo[] tanks = this._entity.getTankInfo();
		if(tanks != null && tankIdx < tanks.length) {
			FluidStack tankFluid = tanks[tankIdx].fluid;
			if(tankFluid != null) {
				String fluidName = tankFluid.getFluid().getLocalizedName();
				if(tankFluid.getFluid().getID() == FluidRegistry.WATER.getID()) {
					fluidName = "Water";
				}
				else if(tankFluid.getFluid().getID() == FluidRegistry.LAVA.getID()) {
					fluidName = "Lava";
				}

				return new String[] { fluidName, String.format("%d / %d mB", tankFluid.amount, tanks[tankIdx].capacity) };
			}
			else {
				return new String[] { "Empty", String.format("0 / %d mB", tanks[tankIdx].capacity) };
			}
		}
		return null;
	}

	@Override
	protected ResourceLocation getResourceLocation() {
		return net.minecraft.client.renderer.texture.TextureMap.locationBlocksTexture;
	}
	
	@Override
	protected boolean drawGradationMarks() { return true; }
}
