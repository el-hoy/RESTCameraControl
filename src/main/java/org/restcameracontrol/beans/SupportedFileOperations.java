
package org.restcameracontrol.beans;

public class SupportedFileOperations {

	private boolean delete;
	private boolean preview;
	private boolean raw;
	private boolean audio;
	private boolean exif;

	public boolean isDelete() {
		return delete;
	}

	public void setDelete(boolean delete) {
		this.delete = delete;
	}

	public boolean isPreview() {
		return preview;
	}

	public void setPreview(boolean preview) {
		this.preview = preview;
	}

	public boolean isRaw() {
		return raw;
	}

	public void setRaw(boolean raw) {
		this.raw = raw;
	}

	public boolean isAudio() {
		return audio;
	}

	public void setAudio(boolean audio) {
		this.audio = audio;
	}

	public boolean isExif() {
		return exif;
	}

	public void setExif(boolean exif) {
		this.exif = exif;
	}

}
