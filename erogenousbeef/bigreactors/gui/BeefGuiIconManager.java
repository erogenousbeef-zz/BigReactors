package erogenousbeef.bigreactors.gui;

import erogenousbeef.bigreactors.client.BeefIconManager;

public class BeefGuiIconManager extends BeefIconManager {

	public static final int COOLANT_IN = 0;
	public static final int COOLANT_OUT = 1;
	public static final int COOLANT_TEMPERATURE = 2;
	public static final int ENERGY_OUTPUT = 3;
	public static final int FUEL_USAGE_RATE = 4;
	public static final int HOT_FLUID_IN = 5;
	public static final int HOT_FLUID_OUT = 6;
	public static final int OFF_OFF = 7;
	public static final int OFF_ON = 8;
	public static final int ON_OFF = 9;
	public static final int ON_ON = 10;
	public static final int TEMPERATURE = 11;
	public static final int WASTE_EJECT_OFF = 12;
	public static final int WASTE_EJECT_ON = 13;
	public static final int WASTE_REPLACE_OFF = 14;
	public static final int WASTE_REPLACE_ON = 15;
	public static final int WASTE_EJECT = 16;
	public static final int REACTIVITY = 17;
	public static final int ENERGY_STORED = 18;
	public static final int RPM = 19;
	
	public BeefGuiIconManager() {
		super();
		iconNames = new String[] {
				"coolantIn",
				"coolantOut",
				"coolantTemperature",
				"energyOutput",
				"fuelUsageRate",
				"hotFluidIn",
				"hotFluidOut",
				"Off_off",
				"Off_on",
				"On_off",
				"On_on",
				"temperature",
				"wasteEject_off",
				"wasteEject_on",
				"wasteReplace_off",
				"wasteReplace_on",
				"wasteEject",
				"reactivity",
				"energyStored",
				"rpm"
		};
	}
	
	@Override
	protected String getPath() { return "guiIcons/"; }
	
}
