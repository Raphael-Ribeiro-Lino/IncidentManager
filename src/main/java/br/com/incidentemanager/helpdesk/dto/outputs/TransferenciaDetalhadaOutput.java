package br.com.incidentemanager.helpdesk.dto.outputs;

import br.com.incidentemanager.helpdesk.enums.StatusTransferenciaEnum;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransferenciaDetalhadaOutput {

	private Long id;
	private Long chamadoId;
	private String chamadoProtocolo;
	private String chamadoTitulo;
	private String chamadoDescricao;
	private String chamadoPrioridade;
	private String chamadoStatus;
	private String chamadoDataCriacao;

	private Long tecnicoDestinoId;
	private String tecnicoDestinoNome;

	private String motivo;
	private String dataSolicitacao;
	private StatusTransferenciaEnum status;
	private String motivoRecusa;
	private String dataResposta;

}
