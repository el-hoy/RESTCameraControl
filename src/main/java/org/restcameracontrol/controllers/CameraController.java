package org.restcameracontrol.controllers;

import java.net.URLConnection;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.restcameracontrol.beans.CameraAbilities;
import org.restcameracontrol.beans.CameraInfo;
import org.restcameracontrol.beans.CameraWidget;
import org.restcameracontrol.beans.FileInfo;
import org.restcameracontrol.beans.Response;
import org.restcameracontrol.services.CameraService;
import org.restcameracontrol.services.TaskService;
import org.restcameracontrol.utils.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cameras")
public class CameraController {

	private Logger log = Logger.getLogger(CameraController.class);
	
	@Autowired
	private CameraService cameraService;

	@Autowired
	private TaskService taskService;
	
	@RequestMapping(value = "", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Response> listCameras() {
		ResponseEntity<Response> response = null;
		
		try {
			log.info("Listing cameras");
			
			List<CameraInfo> cameras = cameraService.listCameras();
			
			log.info("Cameras listed");
			response = new ResponseEntity<Response>(new Response(cameras), HttpStatus.OK);
		} catch (Exception ex) {
			response = new ResponseEntity<Response>(Util.generateExceptionResponse(ex), HttpStatus.OK);
		}
		
		return response;
	}
	
	@RequestMapping(value = "/{cameraId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Response> cameraInfo(@PathVariable Integer cameraId) {
		ResponseEntity<Response> response = null;
		
		try {
			log.info("Getting camera info");
			
			CameraInfo cameraInfo = cameraService.cameraInfo(cameraId);
			
			log.info("Camera info got");
			response = new ResponseEntity<Response>(new Response(cameraInfo), HttpStatus.OK);
		} catch (Exception ex) {
			response = new ResponseEntity<Response>(Util.generateExceptionResponse(ex), HttpStatus.OK);
		}
		
		return response;
	}
	
	@RequestMapping(value = "/{cameraId}/status", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Response> cameraStatus(@PathVariable Integer cameraId) {
		ResponseEntity<Response> response = null;
		
		try {
			log.info("Getting camera status");
			
			Map<String, Object> cameraStatus = cameraService.cameraStatus(cameraId);
			
			log.info("Camera status got");
			response = new ResponseEntity<Response>(new Response(cameraStatus), HttpStatus.OK);
		} catch (Exception ex) {
			response = new ResponseEntity<Response>(Util.generateExceptionResponse(ex), HttpStatus.OK);
		}
		
		return response;
	}
	
	@RequestMapping(value = "/{cameraId}/summary", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Response> getCameraSummary(@PathVariable Integer cameraId) {
		ResponseEntity<Response> response = null;
		
		try {
			log.info("Getting camera summary");
			
			String summary = cameraService.getCameraSummary(cameraId);
			
			log.info("Camera summary got");
			response = new ResponseEntity<Response>(new Response(summary), HttpStatus.OK);
		} catch (Exception ex) {
			response = new ResponseEntity<Response>(Util.generateExceptionResponse(ex), HttpStatus.OK);
		}
		
		return response;
	}
	
	@RequestMapping(value = "/{cameraId}/capture", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Response> capture(@PathVariable Integer cameraId,
			@RequestParam(value="server-folder", required=false) String serverFolder,
			@RequestParam(value="delete-camera", defaultValue="false") boolean deleteFromCamera,
			@RequestParam(value="return-content", defaultValue="false") boolean returnContent) {
		ResponseEntity<Response> response = null;
		
		try {
			log.info("Capturing photo");
			
			FileInfo fileResponse = cameraService.capture(cameraId, serverFolder, deleteFromCamera, returnContent);
			
			log.info("Photo captured");
			response = new ResponseEntity<Response>(new Response(fileResponse), HttpStatus.OK);
		} catch (Exception ex) {
			response = new ResponseEntity<Response>(Util.generateExceptionResponse(ex), HttpStatus.OK);
		}
		
		return response;
	}
	
	@RequestMapping(value = "/{cameraId}/capture", method = RequestMethod.GET)
	public ResponseEntity<Object> getCapture(@PathVariable Integer cameraId,
			@RequestParam(value="delete-camera", defaultValue="false") boolean deleteFromCamera) {
		ResponseEntity<Object> response = null;
		
		try {
			log.info("Capturing photo");
			
			FileInfo fileResponse = cameraService.capture(cameraId, null, deleteFromCamera, true);
			
			String contentType = URLConnection.guessContentTypeFromName(fileResponse.getName());
			if (contentType == null || contentType.trim() == "")
				contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
			HttpHeaders responseHeaders = new HttpHeaders();
			responseHeaders.add("Content-Disposition", "filename=" + fileResponse.getName());
			responseHeaders.add("Content-Type",contentType);
			
			response = new ResponseEntity<Object>(fileResponse.getContent(), responseHeaders, HttpStatus.OK);
			log.info("Photo captured");
		} catch (Exception ex) {
			HttpHeaders responseHeaders = new HttpHeaders();
			responseHeaders.add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
			response = new ResponseEntity<Object>(Util.generateExceptionResponse(ex), responseHeaders, HttpStatus.OK);
		}
		
		return response;
	}
	
	@RequestMapping(value = "/{cameraId}/preview", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Response> capturePreview(@PathVariable Integer cameraId,
			@RequestParam(value="server-folder", required=false) String serverFolder,
			@RequestParam(value="return-content", defaultValue="false") boolean returnContent,
			@RequestParam(value="release-finder", defaultValue="true") boolean releaseViewFinder) {
		ResponseEntity<Response> response = null;
		
		try {
			log.info("Capturing photo preview");
			
			FileInfo fileResponse = cameraService.capturePreview(cameraId, serverFolder, returnContent, releaseViewFinder);
			
			log.info("Photo preview captured");
			
			response = new ResponseEntity<Response>(new Response(fileResponse), HttpStatus.OK);
		} catch (Exception ex) {
			response = new ResponseEntity<Response>(Util.generateExceptionResponse(ex), HttpStatus.OK);
		}
		
		return response;
	}
	
	@RequestMapping(value = "/{cameraId}/preview", method = RequestMethod.GET)
	public ResponseEntity<Object> getPreview(@PathVariable Integer cameraId,
			@RequestParam(value="release-finder", defaultValue="true") boolean releaseViewFinder) {
		ResponseEntity<Object> response = null;
		
		try {
			log.info("Capturing photo preview");
			
			FileInfo fileResponse = cameraService.capturePreview(cameraId, null, true, releaseViewFinder);
			
			String contentType = URLConnection.guessContentTypeFromName(fileResponse.getName());
			if (contentType == null || contentType.trim() == "")
				contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
			HttpHeaders responseHeaders = new HttpHeaders();
			responseHeaders.add("Content-Disposition", "filename=" + fileResponse.getName());
			responseHeaders.add("Content-Type",contentType);
			
			response = new ResponseEntity<Object>(fileResponse.getContent(), responseHeaders, HttpStatus.OK);

			log.info("Photo preview captured");
		} catch (Exception ex) {
			HttpHeaders responseHeaders = new HttpHeaders();
			responseHeaders.add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
			response = new ResponseEntity<Object>(Util.generateExceptionResponse(ex), responseHeaders, HttpStatus.OK);
		}
		
		return response;
	}
	
	@RequestMapping(value = "/{cameraId}/sequence", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Response> sequenceCapture(@PathVariable Integer cameraId,
			@RequestParam(value="initial-wait", defaultValue="0") int initialWait,
			@RequestParam(value="shots-number", defaultValue="1") int shotsNumber,
			@RequestParam(value="lapse-between-shots", defaultValue="0") int lapseBetweenShots,
			@RequestParam(value="server-folder", required=false) String serverFolder,
			@RequestParam(value="delete-camera", defaultValue="false") boolean deleteFromCamera,
			@RequestParam(value="sequence-id", required=false) String sequenceId,
			@RequestParam(value="return-content", defaultValue="false") boolean returnContent)
	{
		ResponseEntity<Response> response = null;
		
		try {
			log.info("Capturing photo sequence");
			
			List<FileInfo> capturesInfo = taskService.sequenceCapture(cameraId, initialWait, shotsNumber,
					lapseBetweenShots, serverFolder, deleteFromCamera, sequenceId, returnContent);
			
			log.info("Photo sequence captured");
			response = new ResponseEntity<Response>(new Response(capturesInfo), HttpStatus.OK);
		} catch (Exception ex) {
			response = new ResponseEntity<Response>(Util.generateExceptionResponse(ex), HttpStatus.OK);
		}
		
		return response;
	}
	
	@RequestMapping(value = "/{cameraId}/widgets", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Response> getWidgetsInfo(@PathVariable Integer cameraId) {
		ResponseEntity<Response> response = null;

		try {
			log.info("Getting widgets info");
			Map<String, Object> cameraWidgets = cameraService.getWidgetsInfo(cameraId);
			response = new ResponseEntity<Response>(new Response(cameraWidgets), HttpStatus.OK);
			log.info("Widgets info got");
		} catch (Exception ex) {
			response = new ResponseEntity<Response>(Util.generateExceptionResponse(ex), HttpStatus.OK);
		}
		return response;
	}
	
	@RequestMapping(value = "/{cameraId}/widgets/{widgetName}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Response> getWidgetInfo(@PathVariable Integer cameraId, @PathVariable String widgetName) {
		ResponseEntity<Response> response = null;

		try {
			log.info("Getting widget info");
			CameraWidget cameraWidget = cameraService.getWidgetInfo(cameraId, widgetName);
			response = new ResponseEntity<Response>(new Response(cameraWidget), HttpStatus.OK);
			log.info("Widget info got");
		} catch (Exception ex) {
			response = new ResponseEntity<Response>(Util.generateExceptionResponse(ex), HttpStatus.OK);
		}
		return response;
	}
	
	@RequestMapping(value = "/{cameraId}/widgets/{widgetName}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Response> setWidgetValue(@PathVariable Integer cameraId, @PathVariable String widgetName,
			@RequestParam(value="value", required=true) String widgetValue) {
		ResponseEntity<Response> response = null;

		try {
			log.info("Setting widget value");
			cameraService.setWidgetValue(cameraId, widgetName, widgetValue);
			response = new ResponseEntity<Response>(new Response(), HttpStatus.OK);
			log.info("Widget value set");
		} catch (Exception ex) {
			response = new ResponseEntity<Response>(Util.generateExceptionResponse(ex), HttpStatus.OK);
		}
		return response;
	}
	
	@RequestMapping(value = "/{cameraId}/movie", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Response> captureMovie(@PathVariable Integer cameraId,
			@RequestParam(value="seconds", defaultValue="5") int seconds,
			@RequestParam(value="autofocus", defaultValue="false") boolean autofocus,
			@RequestParam(value="release-finder", defaultValue="true") boolean releaseViewFinder) {
		ResponseEntity<Response> response = null;
		
		try {
			log.info("Capturing movie");
			
			cameraService.captureMovie(cameraId, seconds, autofocus, releaseViewFinder);
			
			log.info("Movie captured");
			response = new ResponseEntity<Response>(new Response(), HttpStatus.OK);
		} catch (Exception ex) {
			response = new ResponseEntity<Response>(Util.generateExceptionResponse(ex), HttpStatus.OK);
		}
		
		return response;
	}
	
	@RequestMapping(value = "/{cameraId}/movie/start", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Response> startMovie(@PathVariable Integer cameraId,
			@RequestParam(value="autofocus", defaultValue="false") boolean autofocus) {
		ResponseEntity<Response> response = null;
		
		try {
			log.info("Starting movie");
			
			cameraService.startMovie(cameraId, autofocus);
			
			log.info("Movie started");
			response = new ResponseEntity<Response>(new Response(), HttpStatus.OK);
		} catch (Exception ex) {
			response = new ResponseEntity<Response>(Util.generateExceptionResponse(ex), HttpStatus.OK);
		}
		
		return response;
	}
	
	@RequestMapping(value = "/{cameraId}/movie/stop", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Response> stopMovie(@PathVariable Integer cameraId,
			@RequestParam(value="release-finder", defaultValue="true") boolean releaseViewFinder) {
		ResponseEntity<Response> response = null;
		
		try {
			log.info("Stopping movie");
			
			cameraService.stopMovie(cameraId, releaseViewFinder);
			
			log.info("Movie stopped");
			response = new ResponseEntity<Response>(new Response(), HttpStatus.OK);
		} catch (Exception ex) {
			response = new ResponseEntity<Response>(Util.generateExceptionResponse(ex), HttpStatus.OK);
		}
		
		return response;
	}
	
	@RequestMapping(value = "/{cameraId}/bulb", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Response> captureBulb(@PathVariable Integer cameraId,
			@RequestParam(value="seconds", defaultValue="5") int seconds) {
		ResponseEntity<Response> response = null;
		
		try {
			log.info("Capturing bulb");
			
			cameraService.captureBulb(cameraId, seconds);
			
			response = new ResponseEntity<Response>(new Response(), HttpStatus.OK);
			log.info("Bulb captured");
		} catch (Exception ex) {
			response = new ResponseEntity<Response>(Util.generateExceptionResponse(ex), HttpStatus.OK);
		}
		
		return response;
	}
	
	@RequestMapping(value = "/{cameraId}/abilities", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Response> getAbilities(@PathVariable Integer cameraId) {
		ResponseEntity<Response> response = null;
		
		try {
			log.info("Capturing movie");
			
			CameraAbilities abilities = cameraService.getAbilities(cameraId);
			
			log.info("Movie captured");
			response = new ResponseEntity<Response>(new Response(abilities), HttpStatus.OK);
		} catch (Exception ex) {
			response = new ResponseEntity<Response>(Util.generateExceptionResponse(ex), HttpStatus.OK);
		}
		
		return response;
	}
	
	@RequestMapping(value = "/{cameraId}/focus", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Response> manualFocus(@PathVariable Integer cameraId,
			@RequestParam(name="step", required=true) String step,
			@RequestParam(value="release-finder", defaultValue="true") boolean releaseViewFinder) {
		ResponseEntity<Response> response = null;

		try {
			log.info("Manual focusing");
			cameraService.manualFocus(cameraId, step, releaseViewFinder);
			response = new ResponseEntity<Response>(new Response(), HttpStatus.OK);
			log.info("Manual focused");
		} catch (Exception ex) {
			response = new ResponseEntity<Response>(Util.generateExceptionResponse(ex), HttpStatus.OK);
		}
		return response;
	}
	
	@RequestMapping(value = "/close", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Response> closeCameras() {
		ResponseEntity<Response> response = null;
		
		try {
			log.info("Closing cameras");
			
			cameraService.closeCameras();
			
			log.info("Cameras closed");
			response = new ResponseEntity<Response>(new Response(), HttpStatus.OK);
		} catch (Exception ex) {
			response = new ResponseEntity<Response>(Util.generateExceptionResponse(ex), HttpStatus.OK);
		}
		
		return response;
	}
	
	@RequestMapping(value = "/{cameraId}/close", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Response> closeCamera(@PathVariable Integer cameraId) {
		ResponseEntity<Response> response = null;

		try {
			log.info("Closing camera");
			cameraService.closeCamera(cameraId);
			response = new ResponseEntity<Response>(new Response(), HttpStatus.OK);
			log.info("Camera closed");
		} catch (Exception ex) {
			response = new ResponseEntity<Response>(Util.generateExceptionResponse(ex), HttpStatus.OK);
		}
		return response;
	}
	
	@RequestMapping(value = "", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Response> resetCameras() {
		ResponseEntity<Response> response = null;
		
		try {
			log.info("Resetting cameras");
			
			cameraService.resetCameras();
			
			log.info("Cameras reset");
			response = new ResponseEntity<Response>(new Response(), HttpStatus.OK);
		} catch (Exception ex) {
			response = new ResponseEntity<Response>(Util.generateExceptionResponse(ex), HttpStatus.OK);
		}
		
		return response;
	}
	
	@RequestMapping(value = "/{cameraId}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Response> resetCamera(@PathVariable Integer cameraId) {
		ResponseEntity<Response> response = null;

		try {
			log.info("Resetting camera");
			cameraService.resetCamera(cameraId);
			response = new ResponseEntity<Response>(new Response(), HttpStatus.OK);
			log.info("Camera resetted");
		} catch (Exception ex) {
			response = new ResponseEntity<Response>(Util.generateExceptionResponse(ex), HttpStatus.OK);
		}
		return response;
	}

}
