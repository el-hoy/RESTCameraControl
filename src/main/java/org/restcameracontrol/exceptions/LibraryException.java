package org.restcameracontrol.exceptions;

public class LibraryException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6946418468753178696L;
	private int code;
	private String method;
	
	public int getCode() {
		return code;
	}
	
	public void setCode(int code) {
		this.code = code;
	}
	
	public String getMethod() {
		return method;
	}
	
	public void setMethod(String method) {
		this.method = method;
	}
	
	public String getFullMessage()
	{
		return "Error " + code + " [" + getMessage() + "] in library method " + method;
	}
	
	public LibraryException(int code, String method, String message)
	{
		super(message);
		
		this.code = code;
		this.method = method;
	}
	
	public LibraryException(int code, String method, String message, Throwable ex)
	{
		super(message, ex);
		
		this.code = code;
		this.method = method;
	}
}
