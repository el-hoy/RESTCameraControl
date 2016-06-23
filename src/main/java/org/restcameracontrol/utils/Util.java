package org.restcameracontrol.utils;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.restcameracontrol.beans.Response;
import org.restcameracontrol.controllers.CameraController;
import org.restcameracontrol.controllers.FileController;
import org.restcameracontrol.enums.ErrorType;
import org.restcameracontrol.enums.LoggerClass;
import org.restcameracontrol.exceptions.ApplicationException;
import org.restcameracontrol.exceptions.LibraryException;
import org.restcameracontrol.loggers.GPLogger;
import org.restcameracontrol.services.CameraService;
import org.restcameracontrol.services.FileService;
import org.restcameracontrol.services.TaskService;

import com.angryelectron.libgphoto2.CameraAbilities;
import com.angryelectron.libgphoto2.GPPortInfo;

public class Util {

	private static Logger log = Logger.getLogger(Util.class);
	
	public static CameraAbilities.ByValue createCameraAbilitiesByValue(CameraAbilities abilities)
	{
		CameraAbilities.ByValue abilitiesByValue = new CameraAbilities.ByValue();
		
		abilitiesByValue.device_type = abilities.device_type;
		abilitiesByValue.file_operations = abilities.file_operations;
		abilitiesByValue.folder_operations = abilities.folder_operations;
		abilitiesByValue.id = abilities.id;
		abilitiesByValue.library = abilities.library;
		abilitiesByValue.model = abilities.model;
		abilitiesByValue.operations = abilities.operations;
		abilitiesByValue.port = abilities.port;
		abilitiesByValue.reserved2 = abilities.reserved2;
		abilitiesByValue.reserved3 = abilities.reserved3;
		abilitiesByValue.reserved4 = abilities.reserved4;
		abilitiesByValue.reserved5 = abilities.reserved5;
		abilitiesByValue.reserved6 = abilities.reserved6;
		abilitiesByValue.reserved7 = abilities.reserved7;
		abilitiesByValue.reserved8 = abilities.reserved8;
		abilitiesByValue.speed = abilities.speed;
		abilitiesByValue.status = abilities.status;
		abilitiesByValue.usb_class = abilities.usb_class;
		abilitiesByValue.usb_product = abilities.usb_product;
		abilitiesByValue.usb_protocol = abilities.usb_protocol;
		abilitiesByValue.usb_subclass = abilities.usb_subclass;
		abilitiesByValue.usb_vendor = abilities.usb_vendor;
		
		return abilitiesByValue;
	}
	
	public static GPPortInfo.ByValue createPortInfoByValue(GPPortInfo portInfo)
	{
		GPPortInfo.ByValue portInfoByValue = new GPPortInfo.ByValue();
		
		portInfoByValue.library_filename = portInfo.library_filename;
		portInfoByValue.name = portInfo.name;
		portInfoByValue.path = portInfo.path;
		portInfoByValue.type = portInfo.type;
		
		return portInfoByValue;
	}
	
	public static void setLogLevel(LoggerClass loggerClass, Level level)
	{
		switch (loggerClass) {
		case ALL:
			Logger.getLogger(CameraService.class).setLevel(level);
			Logger.getLogger(FileService.class).setLevel(level);
			Logger.getLogger(GPLogger.class).setLevel(level);
			Logger.getLogger(CameraController.class).setLevel(level);
			Logger.getLogger(FileController.class).setLevel(level);
			Logger.getLogger(TaskService.class).setLevel(level);
			break;
		case CameraService:
			Logger.getLogger(CameraService.class).setLevel(level);
			break;
		case FileService:
			Logger.getLogger(FileService.class).setLevel(level);
			break;
		case GPLogger:
			Logger.getLogger(GPLogger.class).setLevel(level);
			break;
		case RestCameraController:
			Logger.getLogger(CameraController.class).setLevel(level);
			break;
		case RestFileController:
			Logger.getLogger(FileController.class).setLevel(level);
			break;
		case TaskService:
			Logger.getLogger(TaskService.class).setLevel(level);
			break;
		case Util:
			Logger.getLogger(Util.class).setLevel(level);
			break;
		default:
			break;
		}
	}
	
	public static Level getLogLevel(LoggerClass loggerClass)
	{
		Level level = null;
		
		switch (loggerClass) {
		case ALL:
			level = Logger.getLogger(CameraService.class).getEffectiveLevel();
			Level levelAux = Logger.getLogger(FileService.class).getEffectiveLevel();
			if (level != null && levelAux != null && levelAux.isGreaterOrEqual(level))
				level = levelAux;
			
			levelAux = Logger.getLogger(GPLogger.class).getEffectiveLevel();
			if (level != null && levelAux != null && levelAux.isGreaterOrEqual(level))
				level = levelAux;
			
			levelAux = Logger.getLogger(CameraController.class).getEffectiveLevel();
			if (level != null && levelAux != null && levelAux.isGreaterOrEqual(level))
				level = levelAux;
			
			levelAux = Logger.getLogger(FileController.class).getEffectiveLevel();
			if (level != null && levelAux != null && levelAux.isGreaterOrEqual(level))
				level = levelAux;
			
			levelAux = Logger.getLogger(TaskService.class).getEffectiveLevel();
			if (level != null && levelAux != null && levelAux.isGreaterOrEqual(level))
				level = levelAux;
			break;
		case CameraService:
			level = Logger.getLogger(CameraService.class).getEffectiveLevel();
			break;
		case FileService:
			level = Logger.getLogger(FileService.class).getEffectiveLevel();
			break;
		case GPLogger:
			level = Logger.getLogger(GPLogger.class).getEffectiveLevel();
			break;
		case RestCameraController:
			level = Logger.getLogger(CameraController.class).getEffectiveLevel();
			break;
		case RestFileController:
			level = Logger.getLogger(FileController.class).getEffectiveLevel();
			break;
		case TaskService:
			level = Logger.getLogger(TaskService.class).getEffectiveLevel();
			break;
		case Util:
			level = Logger.getLogger(Util.class).getEffectiveLevel();
			break;
		default:
			break;
		}
		
		return level;
	}
	
	public static Response generateExceptionResponse(Throwable ex)
	{
		Response response = null;
		
		if (ex instanceof LibraryException)
		{
			log.error("Library error [code: " + ((LibraryException)ex).getCode() + ", message: " + ((LibraryException)ex).getFullMessage() + "]", ex);
			response = new Response(ErrorType.LIBRARY_ERROR, ((LibraryException)ex).getCode(), ((LibraryException)ex).getFullMessage());
		}
		else if (ex instanceof ApplicationException)
		{
			log.error("Application error [code: " + ((ApplicationException)ex).getCode() + ", message: " + ex.getMessage() + "]", ex);
			response = new Response(ErrorType.APPLICATION_ERROR, ((ApplicationException)ex).getCode(), ex.getMessage());
		}
		else
		{
			log.error("Generic error [message: " + ex.getMessage() + "]", ex);
			response = new Response(ErrorType.GENERIC_ERROR, -1, ex.getClass().getName() + ": " + ex.getMessage());
		}
		
		return response;
	}
	

}
