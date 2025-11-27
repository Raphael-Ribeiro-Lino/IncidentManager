package br.com.incidentemanager.helpdesk.services;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import br.com.incidentemanager.helpdesk.dto.inputs.ResponderTransferenciaInput;
import br.com.incidentemanager.helpdesk.dto.inputs.SolicitarTransferenciaInput;
import br.com.incidentemanager.helpdesk.entities.ChamadoEntity;
import br.com.incidentemanager.helpdesk.entities.TransferenciaEntity;
import br.com.incidentemanager.helpdesk.entities.UsuarioEntity;
import br.com.incidentemanager.helpdesk.enums.StatusChamadoEnum;
import br.com.incidentemanager.helpdesk.enums.StatusTransferenciaEnum;
import br.com.incidentemanager.helpdesk.exceptions.BadRequestBusinessException;
import br.com.incidentemanager.helpdesk.exceptions.NotFoundBusinessException;
import br.com.incidentemanager.helpdesk.repositories.ChamadoRepository;
import br.com.incidentemanager.helpdesk.repositories.TransferenciaRepository;
import br.com.incidentemanager.helpdesk.repositories.UsuarioRepository;
import jakarta.transaction.Transactional;

@Service
public class TransferenciaService {

	@Autowired
	private TransferenciaRepository transferenciaRepository;

	@Autowired
	private ChamadoRepository chamadoRepository;

	@Autowired
	private UsuarioRepository usuarioRepository;
	
	@Autowired
	private InteracaoService interacaoService;

	@Transactional
	public TransferenciaEntity solicitar(Long chamadoId, SolicitarTransferenciaInput input,
			UsuarioEntity tecnicoOrigem) {

		ChamadoEntity chamado = chamadoRepository.findById(chamadoId)
				.orElseThrow(() -> new NotFoundBusinessException("Chamado " + chamadoId + " não encontrado"));

		if (chamado.getTecnicoResponsavel() == null
				|| !chamado.getTecnicoResponsavel().getId().equals(tecnicoOrigem.getId())) {
			throw new BadRequestBusinessException("Você não é o técnico responsável por este chamado.");
		}

		if (!StatusChamadoEnum.ABERTO.equals(chamado.getStatus())) {
			throw new BadRequestBusinessException("Transferências só são permitidas para chamados com status ABERTO.");
		}

		boolean jaExistePendente = transferenciaRepository.existsByChamadoAndStatus(chamado,
				StatusTransferenciaEnum.PENDENTE);
		if (jaExistePendente) {
			throw new BadRequestBusinessException(
					"Já existe uma solicitação de transferência pendente para este chamado.");
		}

		UsuarioEntity tecnicoDestino = usuarioRepository.findById(input.getTecnicoDestinoId())
				.orElseThrow(() -> new NotFoundBusinessException("Técnico de destino não encontrado"));

		if (tecnicoOrigem.getId().equals(tecnicoDestino.getId())) {
			throw new BadRequestBusinessException("Você não pode transferir o chamado para si mesmo.");
		}

		if (!tecnicoOrigem.getEmpresa().getId().equals(tecnicoDestino.getEmpresa().getId())) {
			throw new BadRequestBusinessException("O técnico de destino deve pertencer à mesma empresa.");
		}
		TransferenciaEntity transferencia = new TransferenciaEntity();
		transferencia.setChamado(chamado);
		transferencia.setTecnicoOrigem(tecnicoOrigem);
		transferencia.setTecnicoDestino(tecnicoDestino);
		transferencia.setMotivo(input.getMotivo());
		return transferenciaRepository.save(transferencia);
	}

	@Transactional
	public void responder(Long transferenciaId, ResponderTransferenciaInput input, UsuarioEntity usuarioLogado) {
		TransferenciaEntity transferencia = transferenciaRepository.findById(transferenciaId)
				.orElseThrow(() -> new NotFoundBusinessException("Transferência não encontrada"));

		if (!transferencia.getTecnicoDestino().getId().equals(usuarioLogado.getId())) {
			throw new BadRequestBusinessException("Apenas o técnico de destino pode responder a esta solicitação.");
		}

		if (!StatusTransferenciaEnum.PENDENTE.equals(transferencia.getStatus())) {
			throw new BadRequestBusinessException(
					"Esta transferência já foi finalizada (" + transferencia.getStatus() + ")");
		}

		transferencia.setDataResposta(Instant.now());

		if (input.getAceito()) {
			transferencia.setStatus(StatusTransferenciaEnum.ACEITA);
			ChamadoEntity chamado = transferencia.getChamado();
			if (!StatusChamadoEnum.ABERTO.equals(chamado.getStatus())) {
				transferencia.setStatus(StatusTransferenciaEnum.CANCELADA);
				transferencia.setMotivoRecusa("Chamado não está mais aberto.");
			} else {
				chamado.setTecnicoResponsavel(usuarioLogado);
				chamadoRepository.save(chamado);
				interacaoService.registrarAtribuicao(chamado, usuarioLogado, chamado.getTecnicoResponsavel());
			}

		} else {
			if (input.getMotivoRecusa() == null || input.getMotivoRecusa().isBlank()) {
				throw new BadRequestBusinessException("O motivo é obrigatório ao recusar uma transferência.");
			}
			transferencia.setStatus(StatusTransferenciaEnum.RECUSADA);
			transferencia.setMotivoRecusa(input.getMotivoRecusa());
		}

		transferenciaRepository.save(transferencia);
	}

	public Page<TransferenciaEntity> listarMinhasPendencias(UsuarioEntity tecnicoLogado, String search,
			Pageable pagination) {
		return transferenciaRepository.findMinhasPendenciasFiltradas(tecnicoLogado, StatusTransferenciaEnum.PENDENTE,
				search, pagination);
	}

	public Page<TransferenciaEntity> listarSolicitacoesEnviadas(UsuarioEntity tecnicoLogado, String search,
			Pageable pagination) {
		return transferenciaRepository.findMinhasSolicitacoesEnviadas(tecnicoLogado, search, pagination);
	}

	@Transactional
	public void cancelar(Long transferenciaId, UsuarioEntity tecnicoLogado) {
		TransferenciaEntity transferencia = transferenciaRepository.findById(transferenciaId)
				.orElseThrow(() -> new NotFoundBusinessException("Transferência não encontrada"));
		if (!transferencia.getTecnicoOrigem().getId().equals(tecnicoLogado.getId())) {
			throw new BadRequestBusinessException("Você não tem permissão para cancelar esta solicitação.");
		}

		if (!StatusTransferenciaEnum.PENDENTE.equals(transferencia.getStatus())) {
			throw new BadRequestBusinessException(
					"Não é possível cancelar. A solicitação já foi " + transferencia.getStatus());
		}
		transferencia.setStatus(StatusTransferenciaEnum.CANCELADA);
		transferencia.setDataResposta(Instant.now());
		transferencia.setMotivoRecusa("Cancelada pelo solicitante.");
		transferenciaRepository.save(transferencia);
	}

}
