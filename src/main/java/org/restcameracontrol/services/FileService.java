package org.restcameracontrol.services;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.restcameracontrol.beans.CameraInfo;
import org.restcameracontrol.beans.FileInfo;
import org.restcameracontrol.enums.FileType;
import org.restcameracontrol.exceptions.ApplicationException;
import org.restcameracontrol.validate.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import com.angryelectron.libgphoto2.Camera;
import com.angryelectron.libgphoto2.Gphoto2Library;
import com.angryelectron.libgphoto2.Gphoto2Library.CameraFile;
import com.angryelectron.libgphoto2.Gphoto2Library.CameraList;
import com.angryelectron.libgphoto2.Gphoto2Library.GPContext;

public class FileService {
	
//	private Logger log = Logger.getLogger(FileService.class);
	
	@Autowired
	private CameraService cameraService;
	
	@Autowired
	Environment environment;
	
	private Gphoto2Library gphotoLibrary;
	
	// Public methods
	
	public List<String> listFiles(Integer cameraId, String folder) throws Exception {
		
		CameraInfo cameraInfo = null;
		
		if ((cameraInfo = cameraService.findCamera(cameraId)) == null)
			throw new ApplicationException(ApplicationException.Type.CAMERA_NOT_FOUND);
		
		return listFiles(cameraInfo.getCamera(), folder, gphotoLibrary, cameraInfo.getContext());
	}
	
	public Map<String,Object> getFilesTree(Integer cameraId, String folder) throws Exception {
		
		
		CameraInfo cameraInfo = null;
		
		if ((cameraInfo = cameraService.findCamera(cameraId)) == null)
			throw new ApplicationException(ApplicationException.Type.CAMERA_NOT_FOUND);
		
		return getFilesTree(cameraInfo.getCamera(), folder, gphotoLibrary, cameraInfo.getContext());
	}
	
	public FileInfo getFile(Integer cameraId, String filePath, FileType type) throws Exception {
		
		FileInfo file = null;
		
		CameraInfo cameraInfo = null;
		CameraFile cameraFile = null;
		int rc = 0;
		
		try
		{
			if ((cameraInfo = cameraService.findCamera(cameraId)) == null)
				throw new ApplicationException(ApplicationException.Type.CAMERA_NOT_FOUND);
			
//			int typeId = -1;
//			switch(type)
//			{
//			case AUDIO:
//				typeId = CameraFileType.GP_FILE_TYPE_AUDIO;
//				break;
//			case EXIF:
//				typeId = CameraFileType.GP_FILE_TYPE_EXIF;
//				break;
//			case METADATA:
//				typeId = CameraFileType.GP_FILE_TYPE_METADATA;
//				break;
//			case NORMAL:
//				typeId = CameraFileType.GP_FILE_TYPE_NORMAL;
//				break;
//			case PREVIEW:
//				typeId = CameraFileType.GP_FILE_TYPE_PREVIEW;
//				break;
//			case RAW:
//				typeId = CameraFileType.GP_FILE_TYPE_RAW;
//				break;
//			default:
//				throw new Exception("File type '" + type + "' is not valid");
//			}
			
			File fileAux = new File(filePath);
			String folderName = fileAux.getParent();
			String fileName = fileAux.getName();
			String tempFileName = environment.getProperty("temporalPath") + "/rcc_" + cameraInfo.getSerialNumber() + "_" + fileName;
			
			CameraFile[] cameraFilePointer = new CameraFile[1]; 
			gphotoLibrary.gp_file_new(cameraFilePointer);
			cameraFile = cameraFilePointer[0];
//			rc = gphotoLibrary.gp_camera_file_get(cameraInfo.getCamera(), folderName, fileName, typeId, cameraFile, cameraInfo.getContext());
			rc = gphotoLibrary.gp_camera_file_get(cameraInfo.getCamera(), folderName, fileName, type.ordinal(), cameraFile, cameraInfo.getContext());
			Validate.validateResult("gp_camera_file_get", rc);
			rc = gphotoLibrary.gp_file_save(cameraFile, tempFileName);
			Validate.validateResult("gp_file_save", rc);
			
			file = new FileInfo();
			file.setName(fileName);
			file.setCameraFolder(folderName);
			file.setContent(Files.readAllBytes(Paths.get(tempFileName)));
		}
		finally
		{
			if (cameraFile != null)
				gphotoLibrary.gp_file_unref(cameraFile);
		}
		
		return file;
	}
	
	public void deleteFile(Integer cameraId, String filePath) throws Exception {
		
		CameraInfo cameraInfo = null;
		int rc = 0;
		
		if ((cameraInfo = cameraService.findCamera(cameraId)) == null)
			throw new ApplicationException(ApplicationException.Type.CAMERA_NOT_FOUND);
		
		File fileAux = new File(filePath);
		String folderName = fileAux.getParent();
		String fileName = fileAux.getName();
		
		rc = gphotoLibrary.gp_camera_file_delete(cameraInfo.getCamera(), folderName, fileName, cameraInfo.getContext());
		Validate.validateResult("gp_camera_file_delete", rc);
	}
	
	public void deleteFolderFiles(Integer cameraId, String folderPath) throws Exception
	{
		CameraInfo cameraInfo = null;
		int rc = 0;
		
		if ((cameraInfo = cameraService.findCamera(cameraId)) == null)
			throw new ApplicationException(ApplicationException.Type.CAMERA_NOT_FOUND);
		
		rc = gphotoLibrary.gp_camera_folder_delete_all(cameraInfo.getCamera(), folderPath, cameraInfo.getContext());
		Validate.validateResult("gp_camera_folder_delete_all", rc);
	}
	
	public void deleteFolder(Integer cameraId, String folderPath) throws Exception {
		
		CameraInfo cameraInfo = null;
		int rc = 0;
		
		if ((cameraInfo = cameraService.findCamera(cameraId)) == null)
			throw new ApplicationException(ApplicationException.Type.CAMERA_NOT_FOUND);
		
		File fileAux = new File(folderPath);
		String parentFolder = fileAux.getParent();
		String folderName = fileAux.getName();
		
		rc = gphotoLibrary.gp_camera_folder_remove_dir(cameraInfo.getCamera(), parentFolder, folderName, cameraInfo.getContext());
		Validate.validateResult("gp_camera_folder_delete_all", rc);
	}
	
	public void makeFolder(Integer cameraId, String folderPath) throws Exception {
		
		CameraInfo cameraInfo = null;
		int rc = 0;
		
		if ((cameraInfo = cameraService.findCamera(cameraId)) == null)
			throw new ApplicationException(ApplicationException.Type.CAMERA_NOT_FOUND);
		
		File fileAux = new File(folderPath);
		String parentFolder = fileAux.getParent();
		String folderName = fileAux.getName();
		
		rc = gphotoLibrary.gp_camera_folder_make_dir(cameraInfo.getCamera(), parentFolder, folderName, cameraInfo.getContext());
		Validate.validateResult("gp_camera_folder_make_dir", rc);
	}

	public void putFile(Integer cameraId, String filePath, String folderPath) throws Exception {
		
		CameraInfo cameraInfo = null;
		int rc = 0;
		CameraFile cameraFile = null;
		
		try
		{
			if ((cameraInfo = cameraService.findCamera(cameraId)) == null)
				throw new ApplicationException(ApplicationException.Type.CAMERA_NOT_FOUND);
			
			CameraFile[] cameraFilePointer = new CameraFile[1];
			rc = gphotoLibrary.gp_file_new(cameraFilePointer);
			Validate.validateResult("gp_file_new", rc);
			cameraFile = cameraFilePointer[0];
			rc = gphotoLibrary.gp_file_open(cameraFile, filePath);
			Validate.validateResult("gp_file_open", rc);
			
			rc = gphotoLibrary.gp_camera_folder_put_file(cameraInfo.getCamera(), folderPath, cameraFile, cameraInfo.getContext());
			Validate.validateResult("gp_camera_folder_put_file", rc);
		}
		finally
		{
			if (cameraFile != null)
				gphotoLibrary.gp_file_unref(cameraFile);
		}
	}
	
	// Private methods
	
	private List<String> listFiles(Camera camera, String folder, Gphoto2Library gphotoLibrary, GPContext context) throws Exception {
		
		List<String> files = new LinkedList<String>();
		CameraList cameraList = null;
		int rc = 0;
		
		try
		{
			if (!folder.endsWith("/"))
				folder += "/";
			
			CameraList[] cameraListPointer = new CameraList[1];
			rc = gphotoLibrary.gp_list_new(cameraListPointer);
			Validate.validateResult("gp_list_new", rc);
			cameraList = cameraListPointer[0];
			
			rc = gphotoLibrary.gp_camera_folder_list_files(camera, folder, cameraList, context);
			Validate.validateResult("gp_camera_folder_list_files", rc);
			int fileCount = gphotoLibrary.gp_list_count(cameraList);
			for(int idx = 0; idx < fileCount; idx++)
			{
				String[] namePointer = new String[1];
				rc = gphotoLibrary.gp_list_get_name(cameraList, idx, namePointer);
				Validate.validateResult("gp_list_get_name", rc);
				files.add(folder + namePointer[0]);
			}
			
			rc = gphotoLibrary.gp_camera_folder_list_folders(camera, folder, cameraList, context);
			Validate.validateResult("gp_camera_folder_list_folders", rc);
			int folderCount = gphotoLibrary.gp_list_count(cameraList);
			for(int idx = 0; idx < folderCount; idx++)
			{
				String[] namePointer = new String[1];
				rc = gphotoLibrary.gp_list_get_name(cameraList, idx, namePointer);
				Validate.validateResult("gp_list_get_name", rc);
				files.addAll(listFiles(camera, folder + namePointer[0] + "/", gphotoLibrary, context));
			}
		}
		finally
		{
			if (cameraList != null){
				rc = gphotoLibrary.gp_list_unref(cameraList);
				Validate.validateResult("gp_list_unref", rc);
			}
		}
		
		return files;
	}
	
	private Map<String,Object> getFilesTree(Camera camera, String folder, Gphoto2Library gphotoLibrary, GPContext context) throws Exception {
		
		Map<String,Object> files = new LinkedHashMap<String,Object>();
		CameraList cameraList = null;
		int rc = 0;
		
		try
		{
			if (!folder.endsWith("/"))
				folder += "/";
			
			CameraList[] cameraListPointer = new CameraList[1];
			rc = gphotoLibrary.gp_list_new(cameraListPointer);
			Validate.validateResult("gp_list_new", rc);
			cameraList = cameraListPointer[0];
			
			rc = gphotoLibrary.gp_camera_folder_list_files(camera, folder, cameraList, context);
			Validate.validateResult("gp_camera_folder_list_files", rc);
			int fileCount = gphotoLibrary.gp_list_count(cameraList);
			for(int idx = 0; idx < fileCount; idx++)
			{
				String[] namePointer = new String[1];
				rc = gphotoLibrary.gp_list_get_name(cameraList, idx, namePointer);
				Validate.validateResult("gp_list_get_name", rc);
				files.put(namePointer[0], null);
			}
			
			rc = gphotoLibrary.gp_camera_folder_list_folders(camera, folder, cameraList, context);
			Validate.validateResult("gp_camera_folder_list_folders", rc);
			int folderCount = gphotoLibrary.gp_list_count(cameraList);
			for(int idx = 0; idx < folderCount; idx++)
			{
				String[] namePointer = new String[1];
				rc = gphotoLibrary.gp_list_get_name(cameraList, idx, namePointer);
				Validate.validateResult("gp_list_get_name", rc);
				files.put(namePointer[0], getFilesTree(camera, folder + namePointer[0] + "/", gphotoLibrary, context));
			}
		}
		finally
		{
			if (cameraList != null){
				rc = gphotoLibrary.gp_list_unref(cameraList);
				Validate.validateResult("gp_list_unref", rc);
			}
		}
		
		return files;
	}

	// Constructors
	public FileService()
	{
		// Register logger to write libgphoto logs into log4j logger
		gphotoLibrary = Gphoto2Library.INSTANCE;
	}

}
