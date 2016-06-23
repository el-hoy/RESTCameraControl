package org.restcameracontrol.utils;

import java.nio.IntBuffer;

import org.restcameracontrol.exceptions.LibraryException;
import org.restcameracontrol.validate.Validate;

import com.angryelectron.libgphoto2.Gphoto2Library;
import com.angryelectron.libgphoto2.Gphoto2Library.CameraWidget;
import com.angryelectron.libgphoto2.Gphoto2Library.CameraWidgetType;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.FloatByReference;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

public class CameraWidgetUtil {

	public static String getParameterValue(Gphoto2Library gphotoLibrary, CameraWidget paramWidget) throws LibraryException {
		int rc;
		String value = null;

		IntBuffer typePointer = IntBuffer.allocate(4);
		rc = gphotoLibrary.gp_widget_get_type(paramWidget, typePointer);
		Validate.validateResult("gp_widget_get_type", rc);
		int type = typePointer.get();

		if (type != CameraWidgetType.GP_WIDGET_SECTION && type != CameraWidgetType.GP_WIDGET_WINDOW) {
			switch (type) {
			case CameraWidgetType.GP_WIDGET_MENU:
			case CameraWidgetType.GP_WIDGET_TEXT:
			case CameraWidgetType.GP_WIDGET_RADIO:
				PointerByReference pValue = new PointerByReference();
				rc = gphotoLibrary.gp_widget_get_value(paramWidget, pValue.getPointer());
				Validate.validateResult("gp_widget_get_value", rc);
				return (pValue != null && pValue.getValue() != null) ? pValue.getValue().getString(0) : null;
			case CameraWidgetType.GP_WIDGET_RANGE:
				FloatByReference fValue = new FloatByReference();
				rc = gphotoLibrary.gp_widget_get_value(paramWidget, fValue.getPointer());
				Validate.validateResult("gp_widget_get_value", rc);
				return (fValue != null) ? Float.toString(fValue.getValue()) : null;
			case CameraWidgetType.GP_WIDGET_DATE:
				IntByReference iValue = new IntByReference();
				rc = gphotoLibrary.gp_widget_get_value(paramWidget, iValue.getPointer());
				Validate.validateResult("gp_widget_get_value", rc);
				return (iValue != null) ? Long.toString(iValue.getValue() * 1000L) : null;
			case CameraWidgetType.GP_WIDGET_TOGGLE:
				IntByReference tValue = new IntByReference();
				rc = gphotoLibrary.gp_widget_get_value(paramWidget, tValue.getPointer());
				Validate.validateResult("gp_widget_get_value", rc);
				return (tValue != null) ? Integer.toString(tValue.getValue()) : null;
			default:
				throw new UnsupportedOperationException("Unsupported CameraWidgetType");
			}
		}
		return value;
	}

	public static void setParameterValue(Gphoto2Library gphotoLibrary, CameraWidget paramWidget, String value)
			throws LibraryException {
		Pointer pValue = null;
		IntBuffer type = IntBuffer.allocate(4);
		int rc = gphotoLibrary.gp_widget_get_type(paramWidget, type);
		Validate.validateResult("gp_widget_get_type", rc);

		switch (type.get()) {
			case CameraWidgetType.GP_WIDGET_MENU:
			case CameraWidgetType.GP_WIDGET_TEXT:
			case CameraWidgetType.GP_WIDGET_RADIO:
				// char *
				pValue = new Memory(value.length() + 1);
				pValue.setString(0, value);
				break;
			case CameraWidgetType.GP_WIDGET_RANGE:
				// floats are 32-bits or 4 bytes
				float fValue = Float.parseFloat(value);
				pValue = new Memory(4);
				pValue.setFloat(0, fValue);
				break;
			case CameraWidgetType.GP_WIDGET_DATE:
			case CameraWidgetType.GP_WIDGET_TOGGLE:
				// ints are 32-bits or 4 bytes
				int iValue = Integer.parseInt(value);
				pValue = new Memory(4);
				pValue.setInt(0, iValue);
				break;
			default:
				throw new UnsupportedOperationException("Unsupported CameraWidgetType");
		}
		rc = gphotoLibrary.gp_widget_set_value(paramWidget, pValue);
		Validate.validateResult("gp_widget_set_value", rc);
	}

}
