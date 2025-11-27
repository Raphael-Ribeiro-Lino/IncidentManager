package br.com.incidentemanager.helpdesk.services;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.incidentemanager.helpdesk.entities.ChamadoEntity;
import br.com.incidentemanager.helpdesk.entities.InteracaoEntity;
import br.com.incidentemanager.helpdesk.entities.UsuarioEntity;
import br.com.incidentemanager.helpdesk.enums.TipoInteracaoEnum;
import br.com.incidentemanager.helpdesk.repositories.ChamadoRepository;
import br.com.incidentemanager.helpdesk.repositories.InteracaoRepository;
import jakarta.transaction.Transactional;

@Service
public class InteracaoService {

	@Autowired
	private InteracaoRepository interacaoRepository;

	@Autowired
	private ChamadoRepository chamadoRepository;

	@Transactional
	public void registrarAbertura(ChamadoEntity chamado, UsuarioEntity autor) {
		salvarInteracao(chamado, autor, TipoInteracaoEnum.ABERTURA, 
				"Novo chamado aberto no sistema.", true);
	}

	@Transactional
	public void registrarMudancaStatus(ChamadoEntity chamado, UsuarioEntity autor, String observacao, boolean visivelCliente) {
		salvarInteracao(chamado, autor, TipoInteracaoEnum.MUDANCA_STATUS, 
				observacao, visivelCliente);
	}

	@Transactional
	public void registrarAtribuicao(ChamadoEntity chamado, UsuarioEntity autor, UsuarioEntity tecnicoDestino) {
		String texto = "Chamado atribuído ao técnico " + tecnicoDestino.getNome();
		salvarInteracao(chamado, autor, TipoInteracaoEnum.ATRIBUICAO_TECNICO, 
				texto, true);
	}
	
	@Transactional
	public void registrarAvaliacao(ChamadoEntity chamado, UsuarioEntity autor, Integer nota, String comentario) {
		String texto = String.format("Chamado avaliado com nota %d. Comentário: %s", nota, comentario);
		salvarInteracao(chamado, autor, TipoInteracaoEnum.AVALIACAO, 
				texto, true);
	}
	
	@Transactional
	public void registrarReabertura(ChamadoEntity chamado, UsuarioEntity autor, String motivo) {
		String texto = "Chamado reaberto pelo solicitante. Motivo: " + motivo;
		salvarInteracao(chamado, autor, TipoInteracaoEnum.REABERTURA, 
				texto, true);
	}

	@Transactional
	public void adicionarNotaInterna(ChamadoEntity chamado, UsuarioEntity autor, String texto) {
		salvarInteracao(chamado, autor, TipoInteracaoEnum.COMENTARIO_INTERNO, 
				texto, false);
	}
	
	private InteracaoEntity salvarInteracao(ChamadoEntity chamado, UsuarioEntity autor, 
			TipoInteracaoEnum tipo, String descricao, boolean visivelCliente) {
		InteracaoEntity interacao = new InteracaoEntity();
		interacao.setChamado(chamado);
		interacao.setAutor(autor);
		interacao.setTipo(tipo);
		interacao.setDescricao(descricao);
		interacao.setVisivelCliente(visivelCliente);
		interacao.setDataHora(Instant.now());
		
		interacaoRepository.save(interacao);

		chamado.setDataUltimaAtualizacao(Instant.now());
		chamadoRepository.save(chamado);
		
		return interacao;
	}

}
