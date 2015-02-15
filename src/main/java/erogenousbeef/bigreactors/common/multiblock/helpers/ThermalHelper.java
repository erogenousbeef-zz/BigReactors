package erogenousbeef.bigreactors.common.multiblock.helpers;

import net.minecraft.nbt.NBTTagCompound;
import erogenousbeef.bigreactors.utils.StaticUtils;

/**
 * Keeps track of the thermal energy in a mass.
 * Mass units are the equivalent of a block of iron.
 * NOTE: Not meant for persistence.
 * 
 * @author Erogenous Beef
 */
public class ThermalHelper {

	private int m_Mass;
	private float m_Rf;
	
	public ThermalHelper() {
		m_Mass = 0;
		m_Rf = 0f;
	}
	
	public void setRf(float newRf) {
		m_Rf = Math.max(0, newRf);
	}
	
	public void setMass(int newMass) {
		m_Mass = Math.max(0, newMass);
	}
	
	public float getTemp() {
		if(m_Mass <= 0) {
			if(m_Rf < 0) { return Float.NEGATIVE_INFINITY; }
			else if(m_Rf > 0) { return Float.POSITIVE_INFINITY; }
			else { return 0f; }
		}
		else {
			return StaticUtils.Energy.getTemp(m_Mass, m_Rf);
		}
	}
	
	public float getRf() { return m_Rf; }
	public int getMass() { return m_Mass; }
	
	public void merge(ThermalHelper other) {
		m_Rf = Math.max(m_Rf, other.m_Rf);
		m_Mass = Math.max(m_Mass, other.m_Mass);
	}
}
