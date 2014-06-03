package erogenousbeef.bigreactors.client;

import java.util.EnumSet;

import net.minecraft.client.Minecraft;

import cpw.mods.fml.common.IScheduledTickHandler;
import cpw.mods.fml.common.TickType;
import erogenousbeef.core.multiblock.MultiblockRegistry;

public class BRRenderTickHandler implements IScheduledTickHandler {

	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData) {
	}

	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData) {
		ClientProxy.lastRenderTime = Minecraft.getSystemTime();
	}

	@Override
	public EnumSet<TickType> ticks() {
		return EnumSet.of(TickType.RENDER);
	}

	@Override
	public String getLabel() {
		return "BigReactors:BRRenderTickHandler";
	}

	@Override
	public int nextTickSpacing() {
		return 1;
	}

}
