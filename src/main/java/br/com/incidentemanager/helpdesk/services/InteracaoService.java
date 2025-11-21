package br.com.incidentemanager.helpdesk.services;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.incidentemanager.helpdesk.dto.inputs.AlteraStatusChamadoInput;
import br.com.incidentemanager.helpdesk.entities.ChamadoEntity;
import br.com.incidentemanager.helpdesk.entities.InteracaoEntity;
import br.com.incidentemanager.helpdesk.entities.UsuarioEntity;
import br.com.incidentemanager.helpdesk.enums.TipoInteracaoEnum;
import br.com.incidentemanager.helpdesk.repositories.InteracaoRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

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
		interacao.setTipo(TipoInteracaoEnum.ABERTURA);
		interacao.setDataHora(Instant.now());
		interacao.setVisivelCliente(true);
		return interacaoRepository.save(interacao);
	}

	@Transactional
	public InteracaoEntity registrarNovaInteracao(ChamadoEntity chamado, UsuarioEntity tecnicoTi,
			@Valid AlteraStatusChamadoInput alteraStatusChamadoInput) {
		InteracaoEntity interacao = new InteracaoEntity();
		interacao.setDescricao(alteraStatusChamadoInput.getObservacao());
		interacao.setDataHora(Instant.now());
		interacao.setVisivelCliente(alteraStatusChamadoInput.isVisivelCliente());
		interacao.setTipo(TipoInteracaoEnum.MUDANCA_STATUS);
		interacao.setAutor(tecnicoTi);
		interacao.setChamado(chamado);
		return interacaoRepository.save(interacao);
	}

}
