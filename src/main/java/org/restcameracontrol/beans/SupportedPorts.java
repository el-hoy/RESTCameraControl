
package org.restcameracontrol.beans;

public class SupportedPorts {

	private boolean serial;
	private boolean usb;
	private boolean disk;
	private boolean ptpIP;
	private boolean usbDiskDirect;
	private boolean usbSCSI;

	public boolean isSerial() {
		return serial;
	}

	public void setSerial(boolean serial) {
		this.serial = serial;
	}

	public boolean isUsb() {
		return usb;
	}

	public void setUsb(boolean usb) {
		this.usb = usb;
	}

	public boolean isDisk() {
		return disk;
	}

	public void setDisk(boolean disk) {
		this.disk = disk;
	}

	public boolean isPtpIP() {
		return ptpIP;
	}

	public void setPtpIP(boolean ptpIP) {
		this.ptpIP = ptpIP;
	}

	public boolean isUsbDiskDirect() {
		return usbDiskDirect;
	}

	public void setUsbDiskDirect(boolean usbDiskDirect) {
		this.usbDiskDirect = usbDiskDirect;
	}

	public boolean isUsbSCSI() {
		return usbSCSI;
	}

	public void setUsbSCSI(boolean usbSCSI) {
		this.usbSCSI = usbSCSI;
	}

}
