package br.com.incidentemanager.helpdesk.exceptions;

public class UnauthorizedAccessBusinessException extends BusinessException{

	private static final long serialVersionUID = 1L;

	public UnauthorizedAccessBusinessException(String message) {
		super(message);
	}

}
