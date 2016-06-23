package org.restcameracontrol.beans;

import org.restcameracontrol.enums.CameraStatus;
import org.restcameracontrol.serializers.CameraInfoSerializer;

import com.angryelectron.libgphoto2.Camera;
import com.angryelectron.libgphoto2.Gphoto2Library.GPContext;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(using = CameraInfoSerializer.class)
public class CameraInfo extends CameraId {

	private CameraStatus status;
	private Camera camera;
	private GPContext context;

	public CameraStatus getStatus() {
		return status;
	}

	public void setStatus(CameraStatus status) {
		this.status = status;
	}

	public Camera getCamera() {
		return camera;
	}

	public void setCamera(Camera camera) {
		this.camera = camera;
	}

	public GPContext getContext() {
		return context;
	}

	public void setContext(GPContext context) {
		this.context = context;
	}

//	public CameraId getCameraId()
//	{
//		return new CameraId(getManufacturer(), getModel(), getSerialNumber(), getId());
//	}
	
	public CameraInfo(String manufacturer, String model, String serialNumber)
	{
		super(manufacturer, model, serialNumber);
	}
}
