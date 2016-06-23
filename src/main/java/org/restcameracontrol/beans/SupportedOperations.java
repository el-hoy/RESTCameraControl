
package org.restcameracontrol.beans;

public class SupportedOperations {

	private boolean imageCapture;
	private boolean videoCapture;
	private boolean audioCapture;
	private boolean previewCapture;
	private boolean config;

	public boolean isImageCapture() {
		return imageCapture;
	}

	public void setImageCapture(boolean imageCapture) {
		this.imageCapture = imageCapture;
	}

	public boolean isVideoCapture() {
		return videoCapture;
	}

	public void setVideoCapture(boolean videoCapture) {
		this.videoCapture = videoCapture;
	}

	public boolean isAudioCapture() {
		return audioCapture;
	}

	public void setAudioCapture(boolean audioCapture) {
		this.audioCapture = audioCapture;
	}

	public boolean isPreviewCapture() {
		return previewCapture;
	}

	public void setPreviewCapture(boolean previewCapture) {
		this.previewCapture = previewCapture;
	}

	public boolean isConfig() {
		return config;
	}

	public void setConfig(boolean config) {
		this.config = config;
	}

}
