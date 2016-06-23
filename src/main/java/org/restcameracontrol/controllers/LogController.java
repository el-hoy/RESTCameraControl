package org.restcameracontrol.controllers;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.restcameracontrol.beans.Response;
import org.restcameracontrol.enums.LoggerClass;
import org.restcameracontrol.utils.Util;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/log")
public class LogController {

	private Logger log = Logger.getLogger(LogController.class);
	
	@RequestMapping(value = "/{loggerClass}/{level}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Response> setLogLevel(@PathVariable LoggerClass loggerClass,
			@PathVariable Level level) {
		ResponseEntity<Response> response = null;
		
		try {
			log.info("Setting log level");
			
			Util.setLogLevel(loggerClass, level);
			
			log.info("Log level set");
			response = new ResponseEntity<Response>(new Response(), HttpStatus.OK);
		} catch (Exception ex) {
			response = new ResponseEntity<Response>(Util.generateExceptionResponse(ex), HttpStatus.OK);
		}
		
		return response;
	}
	
	@RequestMapping(value = "/{loggerClass}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Response> getLogLevel(@PathVariable LoggerClass loggerClass) {
		ResponseEntity<Response> response = null;
		
		try {
			log.info("Getting log level");
			
			Level level = Util.getLogLevel(loggerClass);
			
			log.info("Log level got");
			response = new ResponseEntity<Response>(new Response(level != null ? level.toString() : null), HttpStatus.OK);
		} catch (Exception ex) {
			response = new ResponseEntity<Response>(Util.generateExceptionResponse(ex), HttpStatus.OK);
		}
		
		return response;
	}
}
