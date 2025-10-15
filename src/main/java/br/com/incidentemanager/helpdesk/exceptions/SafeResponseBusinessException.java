package br.com.incidentemanager.helpdesk.exceptions;

public class SafeResponseBusinessException extends NotFoundBusinessException{

	private static final long serialVersionUID = 1L;

	public SafeResponseBusinessException(String message) {
		super(message);
	}

}
