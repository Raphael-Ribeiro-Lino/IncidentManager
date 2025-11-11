package br.com.incidentemanager.helpdesk.services;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.incidentemanager.helpdesk.entities.ChamadoEntity;
import br.com.incidentemanager.helpdesk.entities.InteracaoEntity;
import br.com.incidentemanager.helpdesk.entities.UsuarioEntity;
import br.com.incidentemanager.helpdesk.repositories.InteracaoRepository;
import jakarta.transaction.Transactional;

@Service
public class InteracaoService {

	@Autowired
	private InteracaoRepository interacaoRepository;

	@Transactional
	public InteracaoEntity registrarAberturaChamado(ChamadoEntity chamado, UsuarioEntity autor) {
        InteracaoEntity interacao = new InteracaoEntity();
        interacao.setChamado(chamado);
        interacao.setAutor(autor);
        interacao.setDescricao("Novo chamado aberto no sistema");
        interacao.setTipo("CHAMADO_ABERTO");
        interacao.setDataHora(LocalDateTime.now());
        interacao.setVisivelCliente(true);
        return interacaoRepository.save(interacao);
    }
	
}
