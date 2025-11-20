package br.com.incidentemanager.helpdesk.entities;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import br.com.incidentemanager.helpdesk.enums.TipoMensagemEnum;
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
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "chat_mensagens")
@Getter
@Setter
public class ChatMensagemEntity {
	
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;
    
    @Column(name = "conteudo", nullable = false, columnDefinition = "TEXT")
    private String conteudo;
    
    @Column(name = "enviadoEm", nullable = false)
    private Instant enviadoEm;
    
    @Column(name = "lidoEm", nullable = true)
    private Instant lidoEm;
    
    @Column(name = "visivelParaCliente", nullable = false)
    private Boolean visivelParaCliente = true;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    private TipoMensagemEnum tipo; 
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chamado_id", nullable = false)
    private ChamadoEntity chamado;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "remetente_id", nullable = false)
    private UsuarioEntity remetente;
    
    @ManyToOne
    @JoinColumn(name = "destinatario_id", nullable = true)
    private UsuarioEntity destinatario;
    
    @OneToMany(mappedBy = "chatMensagem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AnexoEntity> anexos = new ArrayList<>();
    
    @PrePersist
	public void prePersist() {
		this.enviadoEm = Instant.now();
	}
}
