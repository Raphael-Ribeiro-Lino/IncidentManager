package br.com.incidentemanager.helpdesk.entities;

import java.time.Instant;

import br.com.incidentemanager.helpdesk.enums.TipoAnexoEnum;
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
@Table(name = "anexos")
@Getter
@Setter
public class AnexoEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false)
	private Long id;
	
	@Column(name = "nomeArquivo", nullable = false)
	private String nomeArquivo;
	
	@Column(name = "tamanhoBytes", nullable = false)
	private Long tamanhoBytes;
	
	@Column(name = "storagePath", nullable = false, length = 2048)
	private String storagePath;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "tipo", nullable = false)
	private TipoAnexoEnum tipo;
	
	@Column(name = "uploadedAt", nullable = false)
	private Instant uploadedAt;
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chamado_id")
    private ChamadoEntity chamado;
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enviado_por_id", nullable = false)
    private UsuarioEntity enviadoPor;
	
	@ManyToOne
    @JoinColumn(name = "chat_mensagem_id")
    private ChatMensagemEntity chatMensagem;
	
	@PrePersist
	public void prePersist() {
		this.uploadedAt = Instant.now();
	}
}
