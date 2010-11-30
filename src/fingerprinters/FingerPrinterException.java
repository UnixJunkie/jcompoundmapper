package fingerprinters;

import static fingerprinters.FingerPrinterException.ErrorCode.*;

public class FingerPrinterException extends Exception {
	
	private String errorClass = "unknown";
	private ErrorCode errorCode = OK;
	private String errorParameter = null;

	public FingerPrinterException() {}

	public FingerPrinterException(String message) { super(message); }
	
	public FingerPrinterException(ErrorCode errorCode) {
		this.errorCode = errorCode;
	}
	
	public FingerPrinterException(ErrorCode errorCode, String errorClass) {
		this.errorCode = errorCode;
		this.errorClass = errorClass;
	}
	
	public FingerPrinterException(ErrorCode errorCode, String errorClass, String errorParameter) {
		this.errorCode = errorCode;
		this.errorClass = errorClass;
		this.errorParameter = errorParameter;
	}
	
	public ErrorCode getErrorCode() {
		return errorCode;
		}
	
	public String errorMessage() {
		switch (errorCode) {
		case OK:
			return "TILT: Should not get here.";
		case UNKNOWN_FINGERPRINTER_INDEX:
			return String.format("'%s': Unknown FingerPrinterIndex - '%s'.", errorClass, errorParameter);
		case UNKNOWN_FINGERPRINTER_TYPE:
			return String.format("'%s': Unknown FingerPrinterType - '%s'.", errorClass, errorParameter);
		}
		return "";
	}

	
	public enum ErrorCode{
		OK, UNKNOWN_FINGERPRINTER_INDEX, UNKNOWN_FINGERPRINTER_TYPE
	}
}
