package br.com.incidentemanager.helpdesk.entities;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Index;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "redefinir_senhas", indexes = { @Index(name = "idx_usuario_id", columnList = "user_id"),
		@Index(name = "idx_hash", columnList = "hash") })
@Getter
@Setter
public class RedefinirSenhaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "hash", unique = true, nullable = false, length = 100)
	private String hash;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private UsuarioEntity usuario;

	@Column(name = "expiration_time", nullable = false)
	private LocalDateTime expirationTime;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "used_at")
	private LocalDateTime usedAt;

	@Column(name = "used", nullable = false)
	private boolean used = false;
	
	@PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
