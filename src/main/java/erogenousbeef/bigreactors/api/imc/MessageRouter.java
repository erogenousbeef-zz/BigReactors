package erogenousbeef.bigreactors.api.imc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import cpw.mods.fml.common.event.FMLInterModComms.IMCEvent;
import cpw.mods.fml.common.event.FMLInterModComms.IMCMessage;
import erogenousbeef.bigreactors.common.BRLog;

/**
 * This class routes IMC messages to their designated handlers.
 * @author Erogenous Beef
 */
public class MessageRouter {

	protected static Map<String, Method> handlers = new HashMap<String, Method>();
	
	public static void route(IMCEvent event) {
		for(IMCMessage message : event.getMessages()) {
			Method handler = handlers.get(message.key);
			if(handler != null) {
				try {
					handler.invoke(message);
				} catch (IllegalAccessException e) {
					BRLog.warning("IllegalAccessException while handling message <%s>, ignoring. Error: %s", message.key, e.getMessage());
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					BRLog.warning("IllegalArgumentException while handling message <%s>, ignoring. Error: %s", message.key, e.getMessage());
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					BRLog.warning("InvocationTargetException while handling message <%s>, ignoring. Error: %s", message.key, e.getMessage());
					e.printStackTrace();
				}
			}
			else {
				BRLog.warning("Received an InterModComms event with an unrecognized key <%s>", message.key);
			}
		}
	}

	public static void register(String key, Method handler) {
		handlers.put(key, handler);
	}
	
}
