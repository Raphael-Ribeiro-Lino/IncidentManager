package br.com.incidentemanager.helpdesk.entities;

import java.time.Instant;

import br.com.incidentemanager.helpdesk.enums.StatusTransferenciaEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "transferencias")
@Getter
@Setter
public class TransferenciaEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "chamado_id", nullable = false)
	private ChamadoEntity chamado;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "tecnico_origem_id", nullable = false)
	private UsuarioEntity tecnicoOrigem;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "tecnico_destino_id", nullable = false)
	private UsuarioEntity tecnicoDestino;

	@Column(name = "motivo", nullable = false, length = 2000)
	private String motivo;

	@Column(name = "motivo_recusa", length = 2000)
	private String motivoRecusa;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	private StatusTransferenciaEnum status;

	@Column(name = "dataSolicitacao", nullable = false)
	private Instant dataSolicitacao;

	@Column(name = "dataResposta")
	private Instant dataResposta;

	@PrePersist
	public void prePersist() {
		this.dataSolicitacao = Instant.now();
		this.status = StatusTransferenciaEnum.PENDENTE;
	}
}
