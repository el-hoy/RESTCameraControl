package org.restcameracontrol.services;

import java.util.LinkedList;
import java.util.List;

import org.restcameracontrol.beans.CameraInfo;
import org.restcameracontrol.beans.FileInfo;
import org.restcameracontrol.exceptions.ApplicationException;
import org.springframework.beans.factory.annotation.Autowired;

public class TaskService {

	@Autowired
	private CameraService cameraService;
	
	public List<FileInfo> sequenceCapture(Integer cameraId, int initialWait, int shotsNumber,
			int lapseBetweenShots, String downloadFolder, boolean deleteFromCamera, String sequenceId,
			boolean returnContent) throws Exception
	{
		List<FileInfo> capturesInfo = null;
		
		CameraInfo cameraInfo = null;
		if ((cameraInfo = cameraService.findCamera(cameraId)) == null)
			throw new ApplicationException(ApplicationException.Type.CAMERA_NOT_FOUND);
		
		capturesInfo = new LinkedList<>();
		Thread.sleep(initialWait * 1000L);
		for(int idx = 0; idx < shotsNumber; idx++)
		{
			if (idx > 0)
				Thread.sleep(lapseBetweenShots * 1000);
			
			capturesInfo.add(cameraService.capture(cameraInfo, downloadFolder, deleteFromCamera, sequenceId, returnContent));
			
		}
		
		return capturesInfo;
	}
	
}
