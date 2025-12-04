package br.com.incidentemanager.helpdesk.entities;

import java.time.Instant;

import br.com.incidentemanager.helpdesk.enums.TipoNotificacaoEnum;
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
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "notificacoes")
@Getter
@Setter
public class NotificacaoEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false)
	private Long id;

	@Column(name = "titulo", nullable = false)
	private String titulo;

	@Column(name = "mensagem", nullable = false, columnDefinition = "TEXT")
	private String mensagem;

	@Column(name = "lido", nullable = false)
	private Boolean lido = false;

	@Column(name = "criadoEm", nullable = false)
	private Instant criadoEm = Instant.now();

	@Column(name = "enviadoEm")
	private Instant enviadoEm;

	@Enumerated(EnumType.STRING)
	@Column(name = "tipo", nullable = false)
	private TipoNotificacaoEnum tipo;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "destinatario_id", nullable = false)
	private UsuarioEntity destinatario;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "chamado_id", nullable = true)
	private ChamadoEntity chamado;
}
