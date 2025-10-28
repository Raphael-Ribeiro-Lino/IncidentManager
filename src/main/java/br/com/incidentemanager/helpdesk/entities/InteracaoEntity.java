package br.com.incidentemanager.helpdesk.entities;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "interacoes")
@Getter
@Setter
public class InteracaoEntity {
	
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;
    
    @Column(name = "descricao", nullable = false)
    private String descricao;
    
    @Column(name = "dataHora", nullable = false)
    private LocalDateTime dataHora;
    
    @Column(name = "visivelCliente", nullable = false)
    private boolean visivelCliente;
    
    @Column(name = "tipo", nullable = false)
    private String tipo;
    
    @ManyToOne
    @JoinColumn(name = "autor_id")
    private UsuarioEntity autor;
    
    @ManyToOne
    @JoinColumn(name = "chamado_id", nullable = false)
    private ChamadoEntity chamado;
}
