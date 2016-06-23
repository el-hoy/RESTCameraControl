package org.restcameracontrol.beans;

public class FileInfo {

	private String name;
	private String serverFolder;
	private String cameraFolder;
	private byte[] content;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getServerFolder() {
		return serverFolder;
	}

	public void setServerFolder(String serverFolder) {
		this.serverFolder = serverFolder;
	}

	public String getCameraFolder() {
		return cameraFolder;
	}

	public void setCameraFolder(String cameraFolder) {
		this.cameraFolder = cameraFolder;
	}

	public byte[] getContent() {
		return content;
	}

	public void setContent(byte[] content) {
		this.content = content;
	}

}
