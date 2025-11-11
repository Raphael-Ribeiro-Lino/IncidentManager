package br.com.incidentemanager.helpdesk.entities;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import br.com.incidentemanager.helpdesk.enums.PrioridadeEnum;
import br.com.incidentemanager.helpdesk.enums.StatusChamadoEnum;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "chamados")
@Getter
@Setter
public class ChamadoEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false)
	private Long id;

	@Column(unique = true, updatable = false, nullable = false)
	private String protocolo;

	@Column(name = "titulo", nullable = false)
	private String titulo;

	@Column(name = "descricao", nullable = false, columnDefinition = "TEXT")
	private String descricao;

	@Enumerated(EnumType.STRING)
	@Column(name = "prioridade")
	private PrioridadeEnum prioridade;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	private StatusChamadoEnum status;

	@Column(name = "categoria")
	private String categoria;

	@JoinColumn(name = "dataCriacao", nullable = false)
	private Instant dataCriacao;
	
	@JoinColumn(name = "dataUltimaAtualizacao", nullable = false)
	private Instant dataUltimaAtualizacao;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "solicitante_id", nullable = false)
	private UsuarioEntity solicitante;
	
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tecnico_responsavel_id", nullable = true)
    private UsuarioEntity tecnicoResponsavel;
	
	@OneToMany(mappedBy = "chamado", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AnexoEntity> anexos = new ArrayList<>();
	
	@OneToMany(mappedBy = "chamado", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<ChatMensagemEntity> mensagens = new ArrayList<>();
	
	@OneToMany(mappedBy = "chamado", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<NotificacaoEntity> notificacoes = new ArrayList<>();
	
	@OneToMany(mappedBy = "chamado", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<InteracaoEntity> interacoes = new ArrayList<>();

	@PrePersist
	public void prePersist() {
		this.dataCriacao = Instant.now();
		this.dataUltimaAtualizacao = this.dataCriacao;
		this.protocolo = "INC-" + UUID.randomUUID().toString();
		if (this.status == null) {
			this.status = StatusChamadoEnum.ABERTO;
		}
	}

	@PreUpdate
	public void preUpdate() {
		this.dataUltimaAtualizacao = Instant.now();
	}

}
