package br.com.incidentemanager.helpdesk.entities;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;


@Table(name = "empresas")
@Entity
@Getter
@Setter
public class EmpresaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false)
	private Long id;
	
	@Column(name = "ativo", nullable = false)
	private boolean ativo;
	
	@Column(name = "nome", nullable = false)
	private String nome;
	
	@Column(name = "cep", nullable = false)
	private String cep;
	
	@Column(name = "rua", nullable = false)
	private String rua;
	
	@Column(name = "numero", nullable = false)
	private String numero;
	
	@Column(name = "complemento", nullable = false)
	private String complemento;
	
	@Column(name = "bairro", nullable = false)
	private String bairro;
	
	@Column(name = "cidade", nullable = false)
	private String cidade;
	
	@Column(name = "estado", nullable = false)
	private String estado;
	
	@Column(name = "cnpj", nullable = false)
	private String cnpj;
	
	@OneToMany(mappedBy = "empresa")
	private List<UsuarioEntity> usuarios = new ArrayList<>();
	
}
