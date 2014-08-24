package erogenousbeef.bigreactors.common;

import org.apache.logging.log4j.Level;

import cpw.mods.fml.common.FMLLog;

public class BRLog {

	private static final String LOGCHANNEL = "BigReactors";
	
    public static void log(Level level, String format, Object... data)
    {
    	FMLLog.log(LOGCHANNEL, level,  format, data);
    }

    public static void fatal(String format, Object... data)
    {
        log(Level.FATAL, format, data);
    }

    public static void error(String format, Object... data)
    {
    	log(Level.ERROR, format, data);
    }

    public static void warning(String format, Object... data)
    {
        log(Level.WARN, format, data);
    }

    public static void info(String format, Object... data)
    {
        log(Level.INFO, format, data);
    }

    public static void debug(String format, Object... data)
    {
        log(Level.DEBUG, format, data);
    }

    public static void trace(String format, Object... data)
    {
        log(Level.TRACE, format, data);
    }
}
