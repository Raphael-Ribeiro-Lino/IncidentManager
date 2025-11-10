package br.com.incidentemanager.helpdesk.exceptions;

public class S3SaveFileErrorException extends BusinessException{

	private static final long serialVersionUID = 1L;

	public S3SaveFileErrorException() {
		super("Erro ao salvar o arquivo na AWS!");
	}

}
