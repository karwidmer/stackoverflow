package com.landg.phoenix.paymentraise.common.exception;

/**
 * 
 * @author MSP6203
 *
 */
public class CustomJsonMappingException extends RuntimeException {

	static final long serialVersionUID = 1L;

	public CustomJsonMappingException(String message, Exception ex) {
		super(message, ex);
	}

}
