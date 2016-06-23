package org.restcameracontrol.controllers;

import java.net.URLConnection;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.restcameracontrol.beans.FileInfo;
import org.restcameracontrol.beans.Response;
import org.restcameracontrol.enums.FileType;
import org.restcameracontrol.services.FileService;
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
@RequestMapping("/cameras/{cameraId}")
public class FileController {

	private Logger log = Logger.getLogger(FileController.class);
	
	@Autowired
	private FileService fileService;
	
	@RequestMapping(value = "/files/list", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Response> listFiles(@PathVariable Integer cameraId,
			@RequestParam(name="folder", defaultValue="/") String folder) {
		ResponseEntity<Response> response = null;

		try {
			log.info("Listing files");
			List<String> files = fileService.listFiles(cameraId, folder);
			response = new ResponseEntity<Response>(new Response(files), HttpStatus.OK);
			log.info("Files listed");
		} catch (Exception ex) {
			response = new ResponseEntity<Response>(Util.generateExceptionResponse(ex), HttpStatus.OK);
		}
		return response;
	}

	@RequestMapping(value = "/files/tree", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Response> getFilesTree(@PathVariable Integer cameraId,
			@RequestParam(name="folder", defaultValue="/") String folder) {
		ResponseEntity<Response> response = null;

		try {
			log.info("Getting files tree");
			Map<String,Object> files = fileService.getFilesTree(cameraId, folder);
			response = new ResponseEntity<Response>(new Response(files), HttpStatus.OK);
			log.info("Files tree got");
		} catch (Exception ex) {
			response = new ResponseEntity<Response>(Util.generateExceptionResponse(ex), HttpStatus.OK);
		}
		return response;
	}
	
	@RequestMapping(value = "/files", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Response> getFile(@PathVariable Integer cameraId,
			@RequestParam(name="path") String filePath,
			@RequestParam(name="type", defaultValue="NORMAL") FileType fileType) {
		ResponseEntity<Response> response = null;

		try {
			log.info("Getting file");
			FileInfo file = fileService.getFile(cameraId, filePath, fileType);
			response = new ResponseEntity<Response>(new Response(file), HttpStatus.OK);
			log.info("File got");
		} catch (Exception ex) {
			response = new ResponseEntity<Response>(Util.generateExceptionResponse(ex), HttpStatus.OK);
		}
		return response;
	}
	
	@RequestMapping(value = "/files/content", method = RequestMethod.GET)
	public ResponseEntity<Object> getFileContent(@PathVariable Integer cameraId,
			@RequestParam(name="path") String filePath,
			@RequestParam(name="type", defaultValue="NORMAL") FileType fileType) {
		ResponseEntity<Object> response = null;

		try {
			log.info("Getting file content");
			
			FileInfo file = fileService.getFile(cameraId, filePath, fileType);
			
			String contentType = URLConnection.guessContentTypeFromName(file.getName());
			if (contentType == null || contentType.trim() == "")
				contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
			HttpHeaders responseHeaders = new HttpHeaders();
			responseHeaders.add("Content-Disposition", "filename=" + file.getName());
			responseHeaders.add("Content-Type",contentType);
			
			response = new ResponseEntity<Object>(file.getContent(), responseHeaders, HttpStatus.OK);
			log.info("File content got");
		} catch (Exception ex) {
			HttpHeaders responseHeaders = new HttpHeaders();
			responseHeaders.add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
			response = new ResponseEntity<Object>(Util.generateExceptionResponse(ex), responseHeaders, HttpStatus.OK);
		}
		return response;
	}
	
	@RequestMapping(value = "/files", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Response> deleteFile(@PathVariable Integer cameraId,
			@RequestParam(name="path") String filePath) {
		ResponseEntity<Response> response = null;

		try {
			log.info("Deleting file");
			
			fileService.deleteFile(cameraId, filePath);
			response = new ResponseEntity<Response>(new Response(), HttpStatus.OK);
			
			log.info("File deleted");
		} catch (Exception ex) {
			response = new ResponseEntity<Response>(Util.generateExceptionResponse(ex), HttpStatus.OK);
		}
		return response;
	}
	
	@RequestMapping(value = "/files", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Response> putFile(@PathVariable Integer cameraId,
			@RequestParam(name="path") String filePath,
			@RequestParam(name="destiny") String destinyFolderPath) {
		ResponseEntity<Response> response = null;

		try {
			log.info("Putting file");
			
			fileService.putFile(cameraId, filePath, destinyFolderPath);
			response = new ResponseEntity<Response>(new Response(), HttpStatus.OK);
			
			log.info("File put");
		} catch (Exception ex) {
			response = new ResponseEntity<Response>(Util.generateExceptionResponse(ex), HttpStatus.OK);
		}
		return response;
	}
	
	@RequestMapping(value = "/folder/files", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Response> deleteFolderFiles(@PathVariable Integer cameraId,
			@RequestParam String folder) {
		ResponseEntity<Response> response = null;

		try {
			log.info("Deleting folder files");
			
			fileService.deleteFolderFiles(cameraId, folder);
			response = new ResponseEntity<Response>(new Response(), HttpStatus.OK);
			
			log.info("Folder files deleted");
		} catch (Exception ex) {
			response = new ResponseEntity<Response>(Util.generateExceptionResponse(ex), HttpStatus.OK);
		}
		return response;
	}
	
	@RequestMapping(value = "/folder", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Response> deleteFolder(@PathVariable Integer cameraId,
			@RequestParam String folder) {
		ResponseEntity<Response> response = null;

		try {
			log.info("Deleting folder");
			
			fileService.deleteFolder(cameraId, folder);
			response = new ResponseEntity<Response>(new Response(), HttpStatus.OK);
			
			log.info("Folder deleted");
		} catch (Exception ex) {
			response = new ResponseEntity<Response>(Util.generateExceptionResponse(ex), HttpStatus.OK);
		}
		return response;
	}
	
	@RequestMapping(value = "/folder", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Response> makeFolder(@PathVariable Integer cameraId,
			@RequestParam  String folder) {
		ResponseEntity<Response> response = null;

		try {
			log.info("Making folder");
			
			fileService.makeFolder(cameraId, folder);
			response = new ResponseEntity<Response>(new Response(), HttpStatus.OK);
			
			log.info("Folder made");
		} catch (Exception ex) {
			response = new ResponseEntity<Response>(Util.generateExceptionResponse(ex), HttpStatus.OK);
		}
		return response;
	}
	
}
