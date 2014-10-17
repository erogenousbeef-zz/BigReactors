package erogenousbeef.bigreactors.api.data;

import net.minecraftforge.fluids.Fluid;

/**
 * Contains data about 
 * @author Erogenous Beef
 *
 */
public class FluidStateData {

	private String m_Name;
	private Fluid m_Liquid;
	private Fluid m_Gas;
	
	private int m_BoilingPoint;
	private int m_HeatOfVaporization; // Rf absorbed/liberated in liquid<>gas phase transition
	private int m_CoefficientOfExpansion;
	
	public FluidStateData(String name, Fluid liquid, Fluid gas, int bp, int hov, int coe) {
		assert(name != null);
		assert(liquid != null);
		assert(gas != null);
		assert(bp > 0);
		assert(hov > 0);
		assert(coe > 0);

		m_Name = name;
		m_Liquid = liquid;
		m_Gas = gas;
		m_BoilingPoint = bp;
		m_HeatOfVaporization = hov;
		m_CoefficientOfExpansion = coe;
	}

	public String getName() { return m_Name; }
	public Fluid getLiquid() { return m_Liquid; }
	public Fluid getGas() { return m_Gas; }
	
	public int getBoilingPoint() { return m_BoilingPoint; }
	public int getHeatOfVaporization() { return m_HeatOfVaporization; }
	public int getCoefficientOfExpansion() { return m_CoefficientOfExpansion; }
	
	public float getCondensationRf(int unitsOfCondensate) {
		return (float)unitsOfCondensate * m_HeatOfVaporization;
	}
	
	public float getEvaporationRf(int unitsOfEvaporite) {
		return (float)unitsOfEvaporite * (float)m_HeatOfVaporization;
	}
}
