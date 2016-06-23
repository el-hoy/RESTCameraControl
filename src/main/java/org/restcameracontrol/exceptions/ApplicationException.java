package org.restcameracontrol.exceptions;

public class ApplicationException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6366247912048064498L;

	public enum Type {
		
		UNEXPECTED(-1, "Unexpected error"),
		CAMERA_NOT_FOUND(-2, "Camera not found"),
		UNSUPPORTED_TYPE(-3, "Unsupported type");
		
		int code;
		String message;
		Type(int code, String message){
			this.code = code;
			this.message = message;
		}
		int getCode(){
			return this.code;
		}
		String getMessage(){
			return this.message;
		}
	}
	
	private int code;
	
	public int getCode() {
		return code;
	}
	
	public ApplicationException(Type type)
	{
		super(type.getMessage());
		
		this.code = type.getCode();
	}
	
	public ApplicationException(Type type, String message)
	{
		super(type.getMessage() + ": " + message);
		
		this.code = type.getCode();
	}
	
	public ApplicationException(Type type, String message, Throwable ex)
	{
		super(type.getMessage() + ": " + message, ex);
		
		this.code = type.getCode();
	}
}
