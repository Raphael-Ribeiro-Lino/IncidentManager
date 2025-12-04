package br.com.incidentemanager.helpdesk.dto.outputs;

import java.time.Instant;
import br.com.incidentemanager.helpdesk.enums.TipoNotificacaoEnum;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotificacaoOutput {
	
	private Long id;
	
	private String titulo;
	
	private String mensagem;
	
	private Boolean lido;
	
	private Instant criadoEm;
	
	private TipoNotificacaoEnum tipo;
	
	private Long chamadoId;
	
	private String chamadoProtocolo;

}