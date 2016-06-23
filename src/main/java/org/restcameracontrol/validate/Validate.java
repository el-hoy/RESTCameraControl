package org.restcameracontrol.validate;

import org.restcameracontrol.exceptions.LibraryException;

import com.angryelectron.libgphoto2.Gphoto2Library;

public class Validate {
	
	private static Gphoto2Library gphotoLibrary = Gphoto2Library.INSTANCE;

	public static void validateResult(String method, int rc) throws LibraryException {
		if (rc != Gphoto2Library.GP_OK) {
			throw new LibraryException(rc, method, gphotoLibrary.gp_result_as_string(rc));
		}
	}
	
}
