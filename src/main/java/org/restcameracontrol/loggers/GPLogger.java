package org.restcameracontrol.loggers;

import org.apache.log4j.Logger;

import com.angryelectron.libgphoto2.Gphoto2Library.GPLogFunc;
import com.angryelectron.libgphoto2.Gphoto2Library.GPLogLevel;
import com.angryelectron.libgphoto2.Gphoto2Library.va_list;
import com.sun.jna.Pointer;

public class GPLogger implements GPLogFunc {
	
	private Logger log = Logger.getLogger(GPLogger.class);
	
	@Override
	public void apply(int level, Pointer domain, Pointer format, va_list args, Pointer data) {
		
		String domainString = domain == null ? "<null>" : domain.getString(0);
		String formatString = format == null ? "<null>" : format.getString(0);
		String argsString = args == null || args.getPointer() == null ? "<null>" : args.getPointer().getString(0);
		String dataString = data == null ? "<null>" : data.getString(0);
		
		switch (level) {
		case GPLogLevel.GP_LOG_VERBOSE:
			log.debug("GP_LOG_VERBOSE: " + domainString + "; " + formatString + "; " + argsString + "; " + dataString);
			break;
		case GPLogLevel.GP_LOG_DEBUG:
			log.debug("GP_LOG_DEBUG: " + domainString + "; " + formatString + "; " + argsString + "; " + dataString);
			break;
		case GPLogLevel.GP_LOG_DATA:
			log.info("GP_LOG_DATA: " + domainString + "; " + formatString + "; " + argsString + "; " + dataString);
			break;
		case GPLogLevel.GP_LOG_ERROR:
			log.error("GP_LOG_ERROR: " + domainString + "; " + formatString + "; " + argsString + "; " + dataString);
			break;
		default:
			log.debug("GP_LOG: " + level + "; " + domainString + "; " + formatString + "; " + argsString + "; " + dataString);
			break;
		}
		
	}

}
