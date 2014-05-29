package erogenousbeef.core.common;


import org.apache.logging.log4j.Level;

import cpw.mods.fml.common.FMLLog;

public class BeefCoreLog {

	private static final String LOGCHANNEL = "BeefCore";
	
	public BeefCoreLog() {
		//FMLLog.makeLog(LOGCHANNEL);
	}
	
    public static void log(Level level, String format, Object... data)
    {
    	FMLLog.log(LOGCHANNEL, level,  format, data);
    }

    public static void severe(String format, Object... data)
    {
        log(Level.FATAL, format, data);
    }

    public static void warning(String format, Object... data)
    {
        log(Level.WARN, format, data);
    }

    public static void info(String format, Object... data)
    {
        log(Level.INFO, format, data);
    }

    public static void fine(String format, Object... data)
    {
        log(Level.INFO, format, data);
    }

    public static void finer(String format, Object... data)
    {
        log(Level.INFO, format, data);
    }

    public static void finest(String format, Object... data)
    {
        log(Level.INFO, format, data);
    }
}
