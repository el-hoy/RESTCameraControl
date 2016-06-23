package org.restcameracontrol.services;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.PreDestroy;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.restcameracontrol.beans.CameraInfo;
import org.restcameracontrol.beans.FileInfo;
import org.restcameracontrol.beans.SupportedFileOperations;
import org.restcameracontrol.beans.SupportedFolderOperations;
import org.restcameracontrol.beans.SupportedOperations;
import org.restcameracontrol.beans.SupportedPorts;
import org.restcameracontrol.enums.CameraStatus;
import org.restcameracontrol.exceptions.ApplicationException;
import org.restcameracontrol.exceptions.ApplicationException.Type;
import org.restcameracontrol.exceptions.LibraryException;
import org.restcameracontrol.loggers.GPLogger;
import org.restcameracontrol.utils.CameraWidgetUtil;
import org.restcameracontrol.utils.Util;
import org.restcameracontrol.validate.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import com.angryelectron.libgphoto2.Camera;
import com.angryelectron.libgphoto2.CameraAbilities;
import com.angryelectron.libgphoto2.CameraFilePath;
import com.angryelectron.libgphoto2.CameraText;
import com.angryelectron.libgphoto2.Gphoto2Library;
import com.angryelectron.libgphoto2.Gphoto2Library.CameraAbilitiesList;
import com.angryelectron.libgphoto2.Gphoto2Library.CameraCaptureType;
import com.angryelectron.libgphoto2.Gphoto2Library.CameraEventType;
import com.angryelectron.libgphoto2.Gphoto2Library.CameraFile;
import com.angryelectron.libgphoto2.Gphoto2Library.CameraFileType;
import com.angryelectron.libgphoto2.Gphoto2Library.CameraList;
import com.angryelectron.libgphoto2.Gphoto2Library.CameraWidget;
import com.angryelectron.libgphoto2.Gphoto2Library.CameraWidgetType;
import com.angryelectron.libgphoto2.Gphoto2Library.GPContext;
import com.angryelectron.libgphoto2.Gphoto2Library.GPLogLevel;
import com.angryelectron.libgphoto2.Gphoto2Library.GPPortInfoList;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.FloatByReference;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

public class CameraService {
	
	@Autowired
	Environment environment;
	
	@Autowired
	Hashtable<Integer, CameraInfo> cameras;

	private Logger log = Logger.getLogger(CameraService.class);
	
	private Gphoto2Library gphotoLibrary;
	
	public List<CameraInfo> listCameras() throws Exception {
		List<CameraInfo> camerasInfo = null;
		
		findCamera(null, false);
		
		camerasInfo = new LinkedList<CameraInfo>();
		camerasInfo.addAll(cameras.values());
		
		return camerasInfo;
	}
	
	public CameraInfo cameraInfo(Integer cameraId) throws Exception
	{
		CameraInfo cameraInfo = null;
		if ((cameraInfo = findCamera(cameraId)) == null)
			throw new ApplicationException(ApplicationException.Type.CAMERA_NOT_FOUND);
		
		return cameraInfo;
	}
	
	public Map<String, Object> cameraStatus(Integer cameraId) throws Exception
	{
		Map<String, Object> response = null; 
		
		CameraInfo cameraInfo = null;
		if ((cameraInfo = findCamera(cameraId)) == null)
			throw new ApplicationException(ApplicationException.Type.CAMERA_NOT_FOUND);
		
		response = new LinkedHashMap<String, Object>();
		
		CameraWidget[] widget = new CameraWidget[1]; 
		gphotoLibrary.gp_camera_get_config(cameraInfo.getCamera(), widget, cameraInfo.getContext());
		
		recursiveCheckWidget(widget[0], response, false);
		
		return response;
	}
	
	public String getCameraSummary(Integer cameraId) throws Exception
	{
		CameraInfo cameraInfo = null;
		if ((cameraInfo = findCamera(cameraId)) == null)
			throw new ApplicationException(ApplicationException.Type.CAMERA_NOT_FOUND);
		
		CameraText summary = new CameraText();
		int rc = gphotoLibrary.gp_camera_get_summary(cameraInfo.getCamera(), summary, cameraInfo.getContext());
		Validate.validateResult("gp_camera_get_summary", rc);
		
		return new String(summary.text, "UTF-8").trim();
	}
	
	public FileInfo capturePreview(Integer cameraId, String serverFolder, boolean returnContent,
			boolean releaseViewFinder) throws Exception
	{
		int rc = 0;
		FileInfo result = null;
		
		CameraInfo cameraInfo = null;
		if ((cameraInfo = findCamera(cameraId)) == null)
			throw new ApplicationException(ApplicationException.Type.CAMERA_NOT_FOUND);
		
		Camera camera = cameraInfo.getCamera();
		GPContext context = cameraInfo.getContext();
		
		CameraFile[] filePointer = new CameraFile[1];
		rc = gphotoLibrary.gp_file_new(filePointer);
		CameraFile file = filePointer[0];
		Validate.validateResult("gp_file_new", rc);
		rc = gphotoLibrary.gp_camera_capture_preview(camera, file, context);
		Validate.validateResult("gp_camera_capture_preview", rc);
		
		// Release viewFinder
		if (releaseViewFinder)
		{
			CameraWidget[] configurationPointer = new CameraWidget[1];
			CameraWidget[] widgetPointer = new CameraWidget[1];
			getWidgetConfiguration(camera, context, "viewfinder", configurationPointer, widgetPointer);
			CameraWidget configuration = configurationPointer[0];
			CameraWidget widgetViewFinder = widgetPointer[0];
			
			setWidgetValue(widgetViewFinder, "0");
			
			rc = gphotoLibrary.gp_camera_set_config(camera, configuration, context);
			Validate.validateResult("gp_camera_set_config", rc);
			rc = gphotoLibrary.gp_widget_unref(widgetViewFinder);
			Validate.validateResult("gp_widget_unref", rc);
		}
		
		String previewFileName = "rcc_" + cameraId + "_preview.jpg";
		
		if (serverFolder != null)
		{
			String serverPath = serverFolder + "/" +  previewFileName;
			rc = gphotoLibrary.gp_file_save(file, serverPath);
			Validate.validateResult("gp_file_save", rc);
		}
		
		String temporalFolder = environment.getProperty("temporalPath");
		String previewFilePath = temporalFolder + "/" + previewFileName;
		
		rc = gphotoLibrary.gp_file_save(file, previewFilePath);
		Validate.validateResult("gp_file_save", rc);
		
		rc = gphotoLibrary.gp_file_unref(file);
		Validate.validateResult("gp_file_unref", rc);

		result = new FileInfo();
		result.setName(previewFileName);
		result.setServerFolder(serverFolder);

		if (returnContent)
		{
			Path path = Paths.get(previewFilePath);
			result.setContent(Files.readAllBytes(path));
		}
		
		return result;
	}	
	
	public CameraInfo findCamera(Integer cameraId) throws Exception
	{
		return findCamera(cameraId, true);
	}
	
	public CameraInfo findCamera(Integer cameraId, boolean openCamera) throws Exception {
		CameraInfo response = null;

		int rc = 0;

		if (cameras.isEmpty()) {
			
			// Load attached cameras
			List<Camera> camerasList = listAttachedCameras();
			
			for (Camera camera : camerasList) {
				GPContext context = gphotoLibrary.gp_context_new();
				
				open(camera, context);
				
				org.restcameracontrol.beans.CameraWidget widget = getWidgetInfo(camera, "manufacturer",
						context);
				String manufacturer = widget.getValue();
				
				widget = getWidgetInfo(camera, "cameramodel", context);
				String model = widget.getValue();
				
				widget = getWidgetInfo(camera, "serialnumber", context);
				String serialNumber = StringUtils.strip(widget.getValue().trim(), "0");
				
				rc = gphotoLibrary.gp_camera_exit(camera, context);
				Validate.validateResult("gp_camera_exit", rc);
				
				CameraInfo cameraInfo = new CameraInfo(manufacturer, model, serialNumber);
				cameraInfo.setCamera(camera);
				cameraInfo.setContext(context);
				cameraInfo.setStatus(CameraStatus.CLOSE);
				
				cameras.put(cameraInfo.getId(), cameraInfo);
			}

		}

		if (!cameras.isEmpty()) {
			if (cameraId == null) {
				response = ((CameraInfo) cameras.values().toArray()[0]);
			} else {
				response = cameras.get(cameraId);
			}
		}

		if (openCamera && response != null && response.getStatus() == CameraStatus.CLOSE) {
			open(response);
		}

		return response;
	}
	
	public org.restcameracontrol.beans.CameraWidget getWidgetInfo(Integer cameraId, String widgetName) throws Exception
	{
		CameraInfo cameraInfo = null;
		try
		{
			if ((cameraInfo = findCamera(cameraId)) == null)
				throw new ApplicationException(ApplicationException.Type.CAMERA_NOT_FOUND);
		
			return getWidgetInfo(cameraInfo.getCamera(), widgetName, cameraInfo.getContext());
		}
		catch(Exception ex)
		{
			throw ex;
		}
	}
	
	public Map<String, Object> getWidgetsInfo(Integer cameraId) throws Exception
	{
		Map<String, Object> widgetsInfo = null;
		CameraInfo cameraInfo = null;
		
		try
		{
			if ((cameraInfo = findCamera(cameraId)) == null)
				throw new ApplicationException(ApplicationException.Type.CAMERA_NOT_FOUND);
		
			CameraWidget[] configPointer = new CameraWidget[1];
			getWidgetsConfiguration(cameraInfo.getCamera(), cameraInfo.getContext(), configPointer);
			
			widgetsInfo = new LinkedHashMap<String, Object>();
			recursiveCheckWidget(configPointer[0], (Map<String, Object>)widgetsInfo, true);
		}
		catch(Exception ex)
		{
			throw ex;
		}
		
		return widgetsInfo;
	}
	
	public void setWidgetValue(Integer cameraId, String widgetName, String widgetValue) throws Exception
	{
		int rc;
		
		CameraInfo cameraInfo = null;
		if ((cameraInfo = findCamera(cameraId)) == null)
			throw new ApplicationException(ApplicationException.Type.CAMERA_NOT_FOUND);
		
		Camera camera = cameraInfo.getCamera();
		GPContext context = cameraInfo.getContext();
		
		CameraWidget[] configurationPointer = new CameraWidget[1];
		CameraWidget[] widgetPointer = new CameraWidget[1];
		getWidgetConfiguration(camera, context, widgetName, configurationPointer, widgetPointer);
		CameraWidget configuration = configurationPointer[0];
		CameraWidget widget = widgetPointer[0];
		
		setWidgetValue(widget, widgetValue);
		
		rc = gphotoLibrary.gp_camera_set_config(camera, configuration, context);
		Validate.validateResult("gp_camera_set_config", rc);
		rc = gphotoLibrary.gp_widget_unref(widget);
		Validate.validateResult("gp_widget_unref", rc);
	}
	
	public FileInfo capture(Integer cameraId, String serverFolder, boolean deleteFromCamera, boolean returnContent) throws Exception
	{
		CameraInfo cameraInfo = null;
		if ((cameraInfo = findCamera(cameraId)) == null)
			throw new ApplicationException(ApplicationException.Type.CAMERA_NOT_FOUND);
		
		return capture(cameraInfo, serverFolder, deleteFromCamera, null, returnContent);
	}
	
	public FileInfo capture(CameraInfo cameraInfo, String serverFolder, 
			boolean deleteFromCamera, String captureId, boolean returnContent) throws Exception
	{
		int rc;
		FileInfo fileInfo = null;
		
		Camera camera = cameraInfo.getCamera();
		GPContext context = cameraInfo.getContext();
		
		CameraFilePath filePath = new CameraFilePath();
		
		rc = gphotoLibrary.gp_camera_capture(camera, CameraCaptureType.GP_CAPTURE_IMAGE, filePath, context);
		Validate.validateResult("gp_camera_capture", rc);
		
		// Nikon D5300, no necessary wait for capture complete
		//waitForEvent(camera, context, 15000, CameraEventType.GP_EVENT_CAPTURE_COMPLETE);
		
		String cameraFileName = new String(filePath.name).trim();
		String cameraFolderName = new String(filePath.folder).trim();
		
		fileInfo = new FileInfo();
		fileInfo.setName(cameraFileName);
		fileInfo.setCameraFolder(cameraFolderName);
		fileInfo.setServerFolder(serverFolder);
		
		if (returnContent || serverFolder != null)
		{
			CameraFile[] cameraFiles = new CameraFile[1];
			
			rc = gphotoLibrary.gp_file_new(cameraFiles);
			Validate.validateResult("gp_file_new", rc);
			CameraFile cameraFile = cameraFiles[0];
			
			rc = gphotoLibrary.gp_camera_file_get(camera, cameraFolderName,
					cameraFileName, CameraFileType.GP_FILE_TYPE_NORMAL,
					cameraFile, context);
			Validate.validateResult("gp_camera_file_get", rc);			
			
			if (serverFolder != null)
			{
				String serverFileName = captureId == null ? cameraFileName : captureId + cameraFileName;
				String serverFilePath = serverFolder.endsWith("/") ? serverFolder + serverFileName : serverFolder + "/" + serverFileName;
				
				rc = gphotoLibrary.gp_file_save(cameraFile, serverFilePath);
				Validate.validateResult("gp_file_save", rc);
			}
			
			if (returnContent)
			{
				String temporalFolder = environment.getProperty("temporalPath");
				
				String previewFileName = "rcc_" + cameraInfo.getId() + "." + FilenameUtils.getExtension(cameraFileName);
				String previewFilePath = temporalFolder + "/" + previewFileName;
				
				rc = gphotoLibrary.gp_file_save(cameraFile, previewFilePath);
				Validate.validateResult("gp_file_save", rc);
				
				Path path = Paths.get(previewFilePath);
				fileInfo.setContent(Files.readAllBytes(path));
			}
			
			rc = gphotoLibrary.gp_file_unref(cameraFile);
			Validate.validateResult("gp_file_unref", rc);
		}
		
		if (deleteFromCamera) {
			rc = gphotoLibrary.gp_camera_file_delete(camera, cameraFolderName,
					cameraFileName, context);
			Validate.validateResult("gp_camera_file_delete", rc);
		}
		
		return fileInfo;
	}

	public void captureMovie(Integer cameraId, int seconds, boolean autofocus, boolean releaseViewFinder) throws Exception
	{
		CameraInfo cameraInfo = null;
		if ((cameraInfo = findCamera(cameraId)) == null)
			throw new ApplicationException(ApplicationException.Type.CAMERA_NOT_FOUND);
		
		captureMovie(cameraInfo, seconds, autofocus, releaseViewFinder);
	}
	
	public void captureMovie(CameraInfo cameraInfo, int seconds, boolean autofocus, boolean releaseViewFinder) throws Exception
	{
		int rc;
		
		Camera camera = cameraInfo.getCamera();
		GPContext context = cameraInfo.getContext();
		
		CameraWidget[] configurationPointer = new CameraWidget[1];
		CameraWidget[] widgetPointer = new CameraWidget[1];
		CameraWidget configuration = null;
		CameraWidget widget = null;
		
		if (autofocus){
			getWidgetConfiguration(camera, context, "autofocusdrive", configurationPointer, widgetPointer);
			configuration = configurationPointer[0];
			widget = widgetPointer[0];
		
			setWidgetValue(widget, "1");
		
			rc = gphotoLibrary.gp_camera_set_config(camera, configuration, context);
			Validate.validateResult("gp_camera_set_config", rc);
			rc = gphotoLibrary.gp_widget_unref(configuration);
			Validate.validateResult("gp_widget_unref", rc);
			
			Thread.sleep(1000); // Wait for autofocus ends completely
		}
		
		getWidgetConfiguration(camera, context, "movie", configurationPointer, widgetPointer);
		configuration = configurationPointer[0];
		widget = widgetPointer[0];
		
		setWidgetValue(widget, "1");
		
		rc = gphotoLibrary.gp_camera_set_config(camera, configuration, context);
		Validate.validateResult("gp_camera_set_config", rc);
		
		Thread.sleep(seconds * 1000);
		
		setWidgetValue(widget, "0");
		
		rc = gphotoLibrary.gp_camera_set_config(camera, configuration, context);
		Validate.validateResult("gp_camera_set_config", rc);

		rc = gphotoLibrary.gp_widget_unref(configuration);
		Validate.validateResult("gp_widget_unref", rc);
		
		if (releaseViewFinder){
			getWidgetConfiguration(camera, context, "viewfinder", configurationPointer, widgetPointer);
			configuration = configurationPointer[0];
			widget = widgetPointer[0];
		
			setWidgetValue(widget, "0");
		
			rc = gphotoLibrary.gp_camera_set_config(camera, configuration, context);
			Validate.validateResult("gp_camera_set_config", rc);
			rc = gphotoLibrary.gp_widget_unref(configuration);
			Validate.validateResult("gp_widget_unref", rc);
		}
	}
	
	public void startMovie(Integer cameraId, boolean autofocus) throws Exception
	{
		if (autofocus){
			setWidgetValue(cameraId, "autofocusdrive", "1");
			Thread.sleep(1000);
		}
		setWidgetValue(cameraId, "movie", "1");
	}
	
	public void stopMovie(Integer cameraId, boolean releaseViewFinder) throws Exception
	{
		setWidgetValue(cameraId, "movie", "0");
		
		if (releaseViewFinder)
			setWidgetValue(cameraId, "viewfinder", "0");
	}
	
	public void captureBulb(Integer cameraId, int seconds) throws Exception
	{
		CameraInfo cameraInfo = null;
		if ((cameraInfo = findCamera(cameraId)) == null)
			throw new ApplicationException(ApplicationException.Type.CAMERA_NOT_FOUND);
		
		captureBulb(cameraInfo, seconds);
	}
	
	public void captureBulb(CameraInfo cameraInfo, int seconds) throws Exception
	{
		int rc;
		
		Camera camera = cameraInfo.getCamera();
		GPContext context = cameraInfo.getContext();
		
		CameraWidget[] configurationPointer = new CameraWidget[1];
		CameraWidget[] widgetPointer = new CameraWidget[1];
		getWidgetConfiguration(camera, context, "bulb", configurationPointer, widgetPointer);
		CameraWidget configuration = configurationPointer[0];
		CameraWidget widget = widgetPointer[0];
		
		setWidgetValue(widget, "1");
		
		rc = gphotoLibrary.gp_camera_set_config(camera, configuration, context);
		Validate.validateResult("gp_camera_set_config", rc);
		
		Thread.sleep(seconds * 1000);
		
		setWidgetValue(widget, "0");
		
		rc = gphotoLibrary.gp_camera_set_config(camera, configuration, context);
		Validate.validateResult("gp_camera_set_config", rc);
		
		rc = gphotoLibrary.gp_widget_unref(configuration);
		Validate.validateResult("gp_widget_unref", rc);
		
//		getWidgetConfiguration(camera, context, "viewfinder", configurationPointer, widgetPointer);
//		configuration = configurationPointer[0];
//		widget = widgetPointer[0];
//		
//		setWidgetValue(widget, "0");
//		
//		rc = gphotoLibrary.gp_camera_set_config(camera, configuration, context);
//		Validate.validateResult("gp_camera_set_config", rc);
//		rc = gphotoLibrary.gp_widget_unref(configuration);
//		Validate.validateResult("gp_widget_unref", rc);
	}
	
	public org.restcameracontrol.beans.CameraAbilities getAbilities(Integer cameraId) throws Exception
	{
		org.restcameracontrol.beans.CameraAbilities cameraAbilities = null;
		int rc = 0;
		
		CameraInfo cameraInfo = null;
		if ((cameraInfo = findCamera(cameraId)) == null)
			throw new ApplicationException(ApplicationException.Type.CAMERA_NOT_FOUND);
		
		CameraAbilities abilities = new CameraAbilities();
		rc = gphotoLibrary.gp_camera_get_abilities(cameraInfo.getCamera(), abilities);
		Validate.validateResult("gp_camera_get_abilities", rc);
		
		cameraAbilities = new org.restcameracontrol.beans.CameraAbilities();
		cameraAbilities.setModel(new String(abilities.model, StandardCharsets.UTF_8).trim());
		cameraAbilities.setId(new String(abilities.id, StandardCharsets.UTF_8).trim());
		cameraAbilities.setLibrary(new String(abilities.library, StandardCharsets.UTF_8).trim());
		cameraAbilities.setSupportedSpeeds(abilities.speed);
		
		SupportedPorts supportedPorts = new SupportedPorts();
		supportedPorts.setSerial((abilities.port & Gphoto2Library.GPPortType.GP_PORT_SERIAL) == Gphoto2Library.GPPortType.GP_PORT_SERIAL);
		supportedPorts.setDisk((abilities.port & Gphoto2Library.GPPortType.GP_PORT_DISK) == Gphoto2Library.GPPortType.GP_PORT_DISK);
		supportedPorts.setPtpIP((abilities.port & Gphoto2Library.GPPortType.GP_PORT_PTPIP) == Gphoto2Library.GPPortType.GP_PORT_PTPIP);
		supportedPorts.setUsb((abilities.port & Gphoto2Library.GPPortType.GP_PORT_USB) == Gphoto2Library.GPPortType.GP_PORT_USB);
		supportedPorts.setUsbDiskDirect((abilities.port & Gphoto2Library.GPPortType.GP_PORT_USB_DISK_DIRECT) == Gphoto2Library.GPPortType.GP_PORT_USB_DISK_DIRECT);
		supportedPorts.setUsbSCSI((abilities.port & Gphoto2Library.GPPortType.GP_PORT_USB_SCSI) == Gphoto2Library.GPPortType.GP_PORT_USB_SCSI);
		cameraAbilities.setSupportedPorts(supportedPorts);
		
		SupportedOperations supportedOperations = new SupportedOperations();
		supportedOperations.setAudioCapture((abilities.operations & Gphoto2Library.CameraOperation.GP_OPERATION_CAPTURE_AUDIO) == Gphoto2Library.CameraOperation.GP_OPERATION_CAPTURE_AUDIO);
		supportedOperations.setImageCapture((abilities.operations & Gphoto2Library.CameraOperation.GP_OPERATION_CAPTURE_IMAGE) == Gphoto2Library.CameraOperation.GP_OPERATION_CAPTURE_IMAGE);
		supportedOperations.setPreviewCapture((abilities.operations & Gphoto2Library.CameraOperation.GP_OPERATION_CAPTURE_PREVIEW) == Gphoto2Library.CameraOperation.GP_OPERATION_CAPTURE_PREVIEW);
		supportedOperations.setVideoCapture((abilities.operations & Gphoto2Library.CameraOperation.GP_OPERATION_CAPTURE_VIDEO) == Gphoto2Library.CameraOperation.GP_OPERATION_CAPTURE_VIDEO);
		supportedOperations.setConfig((abilities.operations & Gphoto2Library.CameraOperation.GP_OPERATION_CONFIG) == Gphoto2Library.CameraOperation.GP_OPERATION_CONFIG);
		cameraAbilities.setSupportedOperations(supportedOperations);
		
		SupportedFolderOperations supportedFolderOperations = new SupportedFolderOperations();
		supportedFolderOperations.setDeleteAll((abilities.folder_operations & Gphoto2Library.CameraFolderOperation.GP_FOLDER_OPERATION_DELETE_ALL) == Gphoto2Library.CameraFolderOperation.GP_FOLDER_OPERATION_DELETE_ALL);
		supportedFolderOperations.setMakeDir((abilities.folder_operations & Gphoto2Library.CameraFolderOperation.GP_FOLDER_OPERATION_MAKE_DIR) == Gphoto2Library.CameraFolderOperation.GP_FOLDER_OPERATION_MAKE_DIR);
		supportedFolderOperations.setPutFile((abilities.folder_operations & Gphoto2Library.CameraFolderOperation.GP_FOLDER_OPERATION_PUT_FILE) == Gphoto2Library.CameraFolderOperation.GP_FOLDER_OPERATION_PUT_FILE);
		supportedFolderOperations.setRemoveDir((abilities.folder_operations & Gphoto2Library.CameraFolderOperation.GP_FOLDER_OPERATION_REMOVE_DIR) == Gphoto2Library.CameraFolderOperation.GP_FOLDER_OPERATION_REMOVE_DIR);
		cameraAbilities.setSupportedFolderOperations(supportedFolderOperations);
		
		SupportedFileOperations supportedFileOperations = new SupportedFileOperations();
		supportedFileOperations.setAudio((abilities.file_operations & Gphoto2Library.CameraFileOperation.GP_FILE_OPERATION_AUDIO) == Gphoto2Library.CameraFileOperation.GP_FILE_OPERATION_AUDIO);
		supportedFileOperations.setDelete((abilities.file_operations & Gphoto2Library.CameraFileOperation.GP_FILE_OPERATION_DELETE) == Gphoto2Library.CameraFileOperation.GP_FILE_OPERATION_DELETE);
		supportedFileOperations.setExif((abilities.file_operations & Gphoto2Library.CameraFileOperation.GP_FILE_OPERATION_EXIF) == Gphoto2Library.CameraFileOperation.GP_FILE_OPERATION_EXIF);
		supportedFileOperations.setPreview((abilities.file_operations & Gphoto2Library.CameraFileOperation.GP_FILE_OPERATION_PREVIEW) == Gphoto2Library.CameraFileOperation.GP_FILE_OPERATION_PREVIEW);
		supportedFileOperations.setRaw((abilities.file_operations & Gphoto2Library.CameraFileOperation.GP_FILE_OPERATION_RAW) == Gphoto2Library.CameraFileOperation.GP_FILE_OPERATION_RAW);
		cameraAbilities.setSupportedFileOperations(supportedFileOperations);
		
		return cameraAbilities;
	}
	
	public void manualFocus(Integer cameraId, String focusStep, boolean releaseViewFinder) throws Exception
	{
		int rc;
		CameraInfo cameraInfo = null;
		CameraWidget focus = null;
		CameraWidget viewFinder = null;
		CameraWidget configurationFocus = null;
		CameraWidget configurationViewFinder = null;
		
		try
		{
			if ((cameraInfo = findCamera(cameraId)) == null)
				throw new ApplicationException(ApplicationException.Type.CAMERA_NOT_FOUND);
			
			CameraWidget[] configurationPointer = new CameraWidget[1];
			CameraWidget[] viewFinderPointer = new CameraWidget[1];
			Camera camera = cameraInfo.getCamera();
			GPContext context = cameraInfo.getContext();
			
			getWidgetConfiguration(camera, context, "viewfinder", configurationPointer, viewFinderPointer);
			configurationViewFinder = configurationPointer[0];
			viewFinder = viewFinderPointer[0];
			
			setWidgetValue(viewFinder, "1");
			
			rc = gphotoLibrary.gp_camera_set_config(camera, configurationViewFinder, context);
			Validate.validateResult("gp_camera_set_config", rc);
			
			CameraWidget[] focusPointer = new CameraWidget[1];
			getWidgetConfiguration(camera, context, "manualfocusdrive", configurationPointer, focusPointer);
			configurationFocus = configurationPointer[0];
			focus = focusPointer[0];
			
			setWidgetValue(focus, focusStep);
			
			rc = gphotoLibrary.gp_camera_set_config(camera, configurationFocus, context);
			Validate.validateResult("gp_camera_set_config", rc);
			
			if (releaseViewFinder)
			{
				setWidgetValue(viewFinder, "0");
				
				rc = gphotoLibrary.gp_camera_set_config(camera, configurationViewFinder, context);
				Validate.validateResult("gp_camera_set_config", rc);
			}
		}
		catch(Exception ex)
		{
			throw ex;
		}
		finally
		{	
			if (configurationFocus != null)
				gphotoLibrary.gp_widget_unref(configurationFocus);
			
			if (configurationViewFinder != null)
				gphotoLibrary.gp_widget_unref(configurationViewFinder);
		}
	}
	
	public void closeCamera(Integer cameraId) throws Exception
	{
		CameraInfo cameraInfo = null;
		if ((cameraInfo = findCamera(cameraId, false)) == null)
			throw new ApplicationException(ApplicationException.Type.CAMERA_NOT_FOUND);
		
		closeCamera(cameraInfo);
	}
	
	public void closeCameras() {
		if (cameras != null) {
			for (CameraInfo cameraInfo : cameras.values()) {
				closeCamera(cameraInfo);
			}
		}
	}
	
	public void resetCamera(Integer cameraId) throws Exception
	{
		CameraInfo cameraInfo = null;
		if ((cameraInfo = findCamera(cameraId, false)) == null)
			throw new ApplicationException(ApplicationException.Type.CAMERA_NOT_FOUND);
		
		resetCamera(cameraInfo);
	}
	
	@PreDestroy
	public void resetCameras() {
		if (cameras != null) {
			// Don't use a foreach in cameras.values() to avoid concurrency problems
			Enumeration<CameraInfo> elements = cameras.elements();
			while(elements.hasMoreElements())
			{
				CameraInfo cameraInfo = elements.nextElement();
				if (cameraInfo != null)
					resetCamera(cameraInfo);
			}
		}
	}
	
	// Private methods
	
	private void recursiveCheckWidget(CameraWidget widget,
			Map<String, Object> cameraInfo, boolean extendedInfo) throws Exception
	{
		recursiveCheckWidget(widget, cameraInfo, "", extendedInfo);
	}
	
	private void recursiveCheckWidget(CameraWidget widget,
			Map<String, Object> cameraInfo, String parentName, boolean extendedInfo) throws Exception
	{
		
		String[] name = new String[1];
		
		gphotoLibrary.gp_widget_get_name(widget, name);
		
		String nodeName = parentName + "/" + name[0];
		
		int children = gphotoLibrary.gp_widget_count_children(widget);
		if (children > 0)
		{
			CameraWidget[] widgetChild = new CameraWidget[1];
		
			for(int idx = 0; idx < children; idx++)
			{
				gphotoLibrary.gp_widget_get_child(widget, idx, widgetChild);
				
				recursiveCheckWidget(widgetChild[0], cameraInfo, nodeName, extendedInfo);
			}
		}
		else
		{
			if (extendedInfo)
			{
				org.restcameracontrol.beans.CameraWidget cameraWidget = getWidgetInfo(widget);
				cameraInfo.put(nodeName, cameraWidget);
			}
			else
			{
				String value = CameraWidgetUtil.getParameterValue(gphotoLibrary, widget);
				cameraInfo.put(nodeName, value);
			}
		}
	}
	
	@SuppressWarnings("unused")
	private PointerByReference waitForEvent(Camera camera,
			GPContext context, int timeout, int event) throws Exception {
		IntBuffer eventType = IntBuffer.allocate(1);
		PointerByReference data = new PointerByReference();
		int rc;

		/*
		 * need to loop, otherwise GP_EVENT_UNKNOWN is almost always returned
		 */
		while (true) {
			rc = gphotoLibrary.gp_camera_wait_for_event(camera, timeout, eventType, data, context);
			Validate.validateResult("gp_camera_wait_for_event", rc);
			if (eventType.get(0) == event) {
				return data;
			} else if (eventType.get(0) == CameraEventType.GP_EVENT_TIMEOUT) {
				throw new ApplicationException(Type.UNEXPECTED, "Timeout occured waiting for event " + event);
			}
		}
	}
	
	private void getWidgetsConfiguration(Camera camera, GPContext context, CameraWidget[] configPointer) throws LibraryException
	{
		int rc = 0;
		
		rc = gphotoLibrary.gp_camera_get_config(camera, configPointer, context);
		Validate.validateResult("gp_camera_get_config", rc);
	}
	
	private void getWidgetConfiguration(Camera camera, GPContext context,
			String widgetName, CameraWidget[] configPointer, CameraWidget[] widgetPointer) throws LibraryException
	{
		int rc = 0;
		
		rc = gphotoLibrary.gp_camera_get_config(camera, configPointer, context);
		Validate.validateResult("gp_camera_get_config", rc);
		
		Pointer name = new Memory(widgetName.length() + 1);
		name.setString(0, widgetName);
		rc = gphotoLibrary.gp_widget_get_child_by_name(configPointer[0], name, widgetPointer);
		Validate.validateResult("gp_widget_get_child_by_name", rc);
	}
	
	private void setWidgetValue(CameraWidget widget, String widgetValue) throws LibraryException, ApplicationException
	{
		int rc = 0;
		
		Pointer pValue = null;
		IntBuffer type = IntBuffer.allocate(4);
		rc = gphotoLibrary.gp_widget_get_type(widget, type);
		Validate.validateResult("gp_widget_get_type", rc);
		
		switch (type.get()) {
			case CameraWidgetType.GP_WIDGET_MENU:
			case CameraWidgetType.GP_WIDGET_TEXT:
			case CameraWidgetType.GP_WIDGET_RADIO:
				//char *
				pValue = new Memory(widgetValue.length() + 1);
				pValue.setString(0, widgetValue);
				break;
			case CameraWidgetType.GP_WIDGET_RANGE:
				//floats are 32-bits or 4 bytes
				float fValue = Float.parseFloat(widgetValue);
				pValue = new Memory(4);
				pValue.setFloat(0, fValue);
				break;
			case CameraWidgetType.GP_WIDGET_DATE:
			case CameraWidgetType.GP_WIDGET_TOGGLE:
				//ints are 32-bits or 4 bytes
				int iValue = Integer.parseInt(widgetValue);
				pValue = new Memory(4);
				pValue.setInt(0, iValue);
				break;
			default:
				throw new ApplicationException(Type.UNSUPPORTED_TYPE, "CameraWidgetType with value " + type.get());
		}
		rc = gphotoLibrary.gp_widget_set_value(widget, pValue);
		Validate.validateResult("gp_widget_set_value", rc);
	}
	
	private List<Camera> listAttachedCameras() throws LibraryException {
		
		int rc;
		
		GPContext context = gphotoLibrary.gp_context_new();
		
		// Create and load an Abilities List
		CameraAbilitiesList refAbilitiesList[] = new CameraAbilitiesList[1];
		rc = gphotoLibrary.gp_abilities_list_new(refAbilitiesList);
		Validate.validateResult("gp_abilities_list_new", rc);
		
		CameraAbilitiesList cameraAbilitiesList = refAbilitiesList[0];
		rc = gphotoLibrary.gp_abilities_list_load(cameraAbilitiesList, context);
		Validate.validateResult("gp_abilities_list_load", rc);
		
		// Create and load a Ports List
		GPPortInfoList refPortList[] = new GPPortInfoList[1];
		rc = gphotoLibrary.gp_port_info_list_new(refPortList);
		Validate.validateResult("gp_port_info_list_new", rc);
		
		GPPortInfoList portInfoList = refPortList[0];
		rc = gphotoLibrary.gp_port_info_list_load(portInfoList);
		Validate.validateResult("gp_port_info_list_load", rc);
		
		// Create and load a Cameras List from the Ports and Abilities lists.
		CameraList refCameraList[] = new CameraList[1];
		rc = gphotoLibrary.gp_list_new(refCameraList);
		Validate.validateResult("gp_list_new", rc);
		
		CameraList cameraList = refCameraList[0];
		rc = gphotoLibrary.gp_abilities_list_detect(cameraAbilitiesList, portInfoList, cameraList, context);
		Validate.validateResult("gp_abilities_list_detect", rc);
		
		// Convert the Cameras List into a list of Camera objects
		List<Camera> cList = new ArrayList<>();
		int size = gphotoLibrary.gp_list_count(cameraList);
		String[] model = new String[1];
		String[] port = new String[1];
		for (int i = 0; i < size; i++) {
			
			// Get model
			rc = gphotoLibrary.gp_list_get_name(cameraList, i, model);
			Validate.validateResult("gp_list_get_name", rc);
			
			/*
			 * Get Port
			 */
			rc = gphotoLibrary.gp_list_get_value(cameraList, i, port);
			Validate.validateResult("gp_list_get_value", rc);
			
			// Create new camera object
			PointerByReference refCamera = new PointerByReference();
			rc = gphotoLibrary.gp_camera_new(refCamera);
			Validate.validateResult("gp_camera_new", rc);
			Camera c = new Camera(refCamera.getValue());
			
			// Get List of Abilities for this Model, then associate the
			// Abilities with a Camera
			CameraAbilities cameraAbilities = new CameraAbilities();
			int modelIndex = gphotoLibrary.gp_abilities_list_lookup_model(cameraAbilitiesList, model[0]);
			rc = gphotoLibrary.gp_abilities_list_get_abilities(cameraAbilitiesList, modelIndex, cameraAbilities);
			Validate.validateResult("gp_abilities_list_get_abilities", rc);
			rc = gphotoLibrary.gp_camera_set_abilities(c, Util.createCameraAbilitiesByValue(cameraAbilities));
			Validate.validateResult("gp_camera_set_abilities", rc);
			
			// Set the port info to the camera
			// To avoid segmentation faults, directly find the address to the corresponding port info
			// Don't use gphotoLibrary.gp_port_info_list_lookup_path nor gphotoLibrary.gp_port_info_list_get_info
			int portInfoListSize = gphotoLibrary.gp_port_info_list_count(portInfoList);
			Pointer portInfoPointer = getPortInfoByPath(portInfoList, portInfoListSize, port[0]);
			rc = gphotoLibrary.gp_camera_set_port_info(c, portInfoPointer);
			Validate.validateResult("gp_camera_set_port_info", rc);
			
			// Add this Camera to the List
			cList.add(c);
		}
		
		gphotoLibrary.gp_context_unref(context);
		
		return cList;
		
	}
	
	private void open(CameraInfo cameraInfo) throws LibraryException
	{
		open(cameraInfo.getCamera(), cameraInfo.getContext());
		cameraInfo.setStatus(CameraStatus.OPEN);
	}
	
	private void open(Camera camera, GPContext context) throws LibraryException
	{
		try
		{
			int rc = gphotoLibrary.gp_camera_init(camera, context);
			Validate.validateResult("gp_camera_init", rc);
		}
		catch (Exception ex)
		{
			gphotoLibrary.gp_camera_unref(camera);
			throw ex;
		}
	}
	
	private void resetCamera(CameraInfo cameraInfo) {
		closeCamera(cameraInfo);
		gphotoLibrary.gp_context_unref(cameraInfo.getContext());
		gphotoLibrary.gp_camera_unref(cameraInfo.getCamera());
		cameras.remove(cameraInfo.getId());
	}
	
	private Pointer getPortInfoByPath(GPPortInfoList portInfoList, int portInfoListSize, String path)
	{
		Pointer result = null;
		int wordBytes = Integer.parseInt(environment.getProperty("wordBytes"));
		
		for(int idx = 0; idx < portInfoListSize; idx++)
		{
			Pointer portInfoPointer = portInfoList.getPointer().getPointer(0).getPointer(wordBytes * idx);
			
			String currentName = portInfoPointer.getPointer(wordBytes).getString(0);
			if (currentName == null || currentName.trim().isEmpty())
			{
				portInfoListSize++;
			}
			else
			{
				String currentPath = portInfoPointer.getPointer(2*wordBytes).getString(0);
				
				if (path.equals(currentPath))
				{
					result = portInfoPointer;
					break;
				}
			}
		}
		
		return result;
	}
	
	private void closeCamera(CameraInfo cameraInfo)
	{
		gphotoLibrary.gp_camera_exit(cameraInfo.getCamera(), cameraInfo.getContext());
		cameras.get(cameraInfo.getId()).setStatus(CameraStatus.CLOSE);
	}
	
	private org.restcameracontrol.beans.CameraWidget getWidgetInfo(Camera camera, String widgetName,
			GPContext context) throws Exception
	{	
		CameraWidget[] configurationPointer = new CameraWidget[1];
		CameraWidget[] widgetPointer = new CameraWidget[1];
		getWidgetConfiguration(camera, context, widgetName, configurationPointer, widgetPointer);
		CameraWidget widget = widgetPointer[0];

		org.restcameracontrol.beans.CameraWidget cameraWidget = getWidgetInfo(widget);
		
		int rc = gphotoLibrary.gp_widget_unref(configurationPointer[0]);
		Validate.validateResult("gp_widget_unref", rc);
		
		return cameraWidget;
	}
	
	private org.restcameracontrol.beans.CameraWidget getWidgetInfo(CameraWidget widget) throws Exception
	{	
		int rc = 0;
		org.restcameracontrol.beans.CameraWidget cameraWidget = null;
		
		IntBuffer typeBuffer = IntBuffer.allocate(4);
		rc = gphotoLibrary.gp_widget_get_type(widget, typeBuffer);
		Validate.validateResult("gp_widget_get_type", rc);
		int type = typeBuffer.get();
		
		if (type != CameraWidgetType.GP_WIDGET_SECTION && type != CameraWidgetType.GP_WIDGET_WINDOW)
		{
			cameraWidget = new org.restcameracontrol.beans.CameraWidget();
			cameraWidget.setTypeId(type);
			String[] text = new String[1];
			rc = gphotoLibrary.gp_widget_get_label(widget, text);
			Validate.validateResult("gp_widget_get_label", rc);
			cameraWidget.setLabel(text[0]);
			rc = gphotoLibrary.gp_widget_get_name(widget, text);
			Validate.validateResult("gp_widget_get_name", rc);
			cameraWidget.setName(text[0]);
			
			IntBuffer readOnlyBuffer = IntBuffer.allocate(4);
			rc = gphotoLibrary.gp_widget_get_readonly(widget, readOnlyBuffer);
			Validate.validateResult("gp_widget_get_readonly", rc);
			int readOnlyInt = readOnlyBuffer.get();
			cameraWidget.setReadOnly(readOnlyInt != 0);
			
			String[] info = new String[1];
			rc = gphotoLibrary.gp_widget_get_info(widget, info);
			Validate.validateResult("", rc);
			cameraWidget.setInfo(info[0]);
			
			String value = null;
			
			int choicesNumber = 0;
			String[] choices = null;
			FloatBuffer min = null;
			FloatBuffer max = null;
			FloatBuffer increment = null;
			switch (type) {
			case CameraWidgetType.GP_WIDGET_TEXT:
				cameraWidget.setTypeName("GP_WIDGET_TEXT");
				
				PointerByReference pValue = new PointerByReference();
				rc = gphotoLibrary.gp_widget_get_value(widget, pValue.getPointer());
				Validate.validateResult("gp_widget_get_value", rc);
				value = (pValue != null && pValue.getValue() != null) ? pValue.getValue().getString(0) : null;
				break;
			case CameraWidgetType.GP_WIDGET_MENU:
				cameraWidget.setTypeName("GP_WIDGET_MENU");
			case CameraWidgetType.GP_WIDGET_RADIO:
				cameraWidget.setTypeName("GP_WIDGET_RADIO");
				
				PointerByReference pValue2 = new PointerByReference();
				rc = gphotoLibrary.gp_widget_get_value(widget, pValue2.getPointer());
				Validate.validateResult("gp_widget_get_value", rc);
				value = (pValue2 != null && pValue2.getValue() != null) ? pValue2.getValue().getString(0) : null;
				
				choicesNumber = gphotoLibrary.gp_widget_count_choices(widget);
				choices = new String[choicesNumber];
				for (int idx = 0; idx < choicesNumber; idx++) {
					String[] choice = new String[1];
					rc = gphotoLibrary.gp_widget_get_choice(widget, idx, choice);
					Validate.validateResult("gp_widget_get_choice", rc);
					choices[idx] = choice[0];
				}
				break;
			case CameraWidgetType.GP_WIDGET_RANGE:
				cameraWidget.setTypeName("GP_WIDGET_RANGE");
				
				FloatByReference fValue = new FloatByReference();
				rc = gphotoLibrary.gp_widget_get_value(widget, fValue.getPointer());
				Validate.validateResult("gp_widget_get_value", rc);
				value = (fValue != null) ? Float.toString(fValue.getValue()) : null;
				
				min = FloatBuffer.allocate(4);
				max = FloatBuffer.allocate(4);
				increment = FloatBuffer.allocate(4);
				rc = gphotoLibrary.gp_widget_get_range(widget, min, max, increment);
				Validate.validateResult("gp_widget_get_range", rc);
				break;
			case CameraWidgetType.GP_WIDGET_DATE:
				cameraWidget.setTypeName("GP_WIDGET_DATE");
				
				IntByReference iValue = new IntByReference();
				rc = gphotoLibrary.gp_widget_get_value(widget, iValue.getPointer());
				Validate.validateResult("gp_widget_get_value", rc);
				value = (iValue != null) ? Long.toString(iValue.getValue() * 1000L) : null;
				break;
			case CameraWidgetType.GP_WIDGET_TOGGLE:
				cameraWidget.setTypeName("GP_WIDGET_TOGGLE");
				
				IntByReference tValue = new IntByReference();
				rc = gphotoLibrary.gp_widget_get_value(widget, tValue.getPointer());
				Validate.validateResult("gp_widget_get_value", rc);
				value = (tValue != null) ? Integer.toString(tValue.getValue()) : null;
				break;
			default:
				throw new UnsupportedOperationException("Unsupported CameraWidgetType");
			}
			
			cameraWidget.setValue(value);
			cameraWidget.setChoices(choices);
			if (min != null)
				cameraWidget.setRangeMin(min.get());
			if (max != null)
				cameraWidget.setRangeMax(max.get());
			if (increment != null)
				cameraWidget.setRangeIncrement(increment.get());
		}
		
		return cameraWidget;
	}
	
	// Constructors
	public CameraService(GPLogger gpLogger)
	{
		// Register logger to write libgphoto logs into log4j logger
		gphotoLibrary = Gphoto2Library.INSTANCE;
		
		int logId = gphotoLibrary.gp_log_add_func(GPLogLevel.GP_LOG_DEBUG, gpLogger, null);
		if (logId <= 0)
			log.warn("Error registering GPLogger. The libgphoto2 log isn't gonna be registered");
	}
}
