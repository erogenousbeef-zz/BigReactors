package erogenousbeef.core.common;

import java.util.logging.Level;

import cpw.mods.fml.common.FMLLog;

public class BeefCoreLog {

	private static final String LOGCHANNEL = "BeefCore";
	
	public BeefCoreLog() {
		FMLLog.makeLog(LOGCHANNEL);
	}
	
    public static void log(Level level, String format, Object... data)
    {
    	FMLLog.log(LOGCHANNEL, level,  format, data);
    }

    public static void severe(String format, Object... data)
    {
        log(Level.SEVERE, format, data);
    }

    public static void warning(String format, Object... data)
    {
        log(Level.WARNING, format, data);
    }

    public static void info(String format, Object... data)
    {
        log(Level.INFO, format, data);
    }

    public static void fine(String format, Object... data)
    {
        log(Level.FINE, format, data);
    }

    public static void finer(String format, Object... data)
    {
        log(Level.FINER, format, data);
    }

    public static void finest(String format, Object... data)
    {
        log(Level.FINEST, format, data);
    }
}
