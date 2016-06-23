package org.restcameracontrol.beans;

import org.restcameracontrol.enums.ErrorType;

public class Response {

	private ErrorType errorType;
	private int errorCode;
	private String errorMessage;
	private Object data;

	public ErrorType getErrorType() {
		return errorType;
	}

	public void setErrorType(ErrorType errorType) {
		this.errorType = errorType;
	}

	public int getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public Response() {
		this(null);
	}

	public Response(Object data) {
		this.errorType = ErrorType.NO_ERROR;
		this.errorCode = 0;
		this.errorMessage = null;
		this.data = data;
	}

	public Response(ErrorType errorType, int errorCode, String errorMessage) {
		this.errorType = errorType;
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
		this.data = null;
	}
}
