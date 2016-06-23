package org.restcameracontrol.beans;

public class CameraAbilities {

	private String model;
	private SupportedPorts supportedPorts;
	private int[] supportedSpeeds;
	private SupportedOperations supportedOperations;
	private SupportedFolderOperations supportedFolderOperations;
	private SupportedFileOperations supportedFileOperations;
	private String library;
	private String id;
	private int status;

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public SupportedPorts getSupportedPorts() {
		return supportedPorts;
	}

	public void setSupportedPorts(SupportedPorts supportedPorts) {
		this.supportedPorts = supportedPorts;
	}

	public int[] getSupportedSpeeds() {
		return supportedSpeeds;
	}

	public void setSupportedSpeeds(int[] supportedSpeeds) {
		this.supportedSpeeds = supportedSpeeds;
	}

	public SupportedOperations getSupportedOperations() {
		return supportedOperations;
	}

	public void setSupportedOperations(SupportedOperations supportedOperations) {
		this.supportedOperations = supportedOperations;
	}

	public SupportedFolderOperations getSupportedFolderOperations() {
		return supportedFolderOperations;
	}

	public void setSupportedFolderOperations(SupportedFolderOperations supportedFolderOperations) {
		this.supportedFolderOperations = supportedFolderOperations;
	}

	public SupportedFileOperations getSupportedFileOperations() {
		return supportedFileOperations;
	}

	public void setSupportedFileOperations(SupportedFileOperations supportedFileOperations) {
		this.supportedFileOperations = supportedFileOperations;
	}

	public String getLibrary() {
		return library;
	}

	public void setLibrary(String library) {
		this.library = library;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

}