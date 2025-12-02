package br.com.incidentemanager.helpdesk.dto.outputs;

import java.time.Instant;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatMensagemOutput {

	private Long id;

	private String conteudo;

	private Instant enviadoEm;

	private String tipo;

	private String remetenteNome;

	private String remetentePerfil;

	private boolean souEu;

	private List<AnexoOutput> anexos;
}
