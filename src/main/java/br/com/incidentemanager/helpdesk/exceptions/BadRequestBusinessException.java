package br.com.incidentemanager.helpdesk.exceptions;

public class BadRequestBusinessException extends BusinessException{

	private static final long serialVersionUID = 1L;

	public BadRequestBusinessException(String message) {
		super(message);
	}

}
