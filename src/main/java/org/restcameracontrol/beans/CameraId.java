package org.restcameracontrol.beans;

public class CameraId {

	private String manufacturer;
	private String model;
	private String serialNumber;
	private int id;
	
	public String getManufacturer() {
		return manufacturer;
	}
	
	public String getModel() {
		return model;
	}
	
	public String getSerialNumber() {
		return serialNumber;
	}
	
	public int getId() {
		return id;
	}
	
	private int getCameraHash()
	{
		String camera = manufacturer + model + serialNumber;
		return camera.hashCode();
	}
	
	public CameraId(String manufacturer, String model, String serialNumber)
	{
		this.manufacturer = manufacturer;
		this.model = model;
		this.serialNumber = serialNumber;
		this.id = getCameraHash();
	}
	
	public CameraId(String manufacturer, String model, String serialNumber,
			int id)
	{
		this.manufacturer = manufacturer;
		this.model = model;
		this.serialNumber = serialNumber;
		this.id = id;
	}
}
