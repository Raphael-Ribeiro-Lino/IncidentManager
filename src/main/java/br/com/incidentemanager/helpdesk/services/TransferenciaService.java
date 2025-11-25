package br.com.incidentemanager.helpdesk.services;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
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
import jakarta.validation.Valid;

@Service
public class TransferenciaService {

	@Autowired
    private TransferenciaRepository transferenciaRepository;

    @Autowired
    private ChamadoRepository chamadoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Transactional
    public TransferenciaEntity solicitar(Long chamadoId, SolicitarTransferenciaInput input, UsuarioEntity tecnicoOrigem) {
        
        ChamadoEntity chamado = chamadoRepository.findById(chamadoId)
        		.orElseThrow(() -> new NotFoundBusinessException("Chamado " + chamadoId + " não encontrado"));

        if (chamado.getTecnicoResponsavel() == null || !chamado.getTecnicoResponsavel().getId().equals(tecnicoOrigem.getId())) {
            throw new BadRequestBusinessException("Você não é o técnico responsável por este chamado.");
        }

        if (!StatusChamadoEnum.ABERTO.equals(chamado.getStatus())) {
            throw new BadRequestBusinessException("Transferências só são permitidas para chamados com status ABERTO.");
        }

        boolean jaExistePendente = transferenciaRepository.existsByChamadoAndStatus(chamado, StatusTransferenciaEnum.PENDENTE);
        if (jaExistePendente) {
            throw new BadRequestBusinessException("Já existe uma solicitação de transferência pendente para este chamado.");
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

		// 1. Segurança: Só o destino pode responder
		if (!transferencia.getTecnicoDestino().getId().equals(usuarioLogado.getId())) {
			throw new BadRequestBusinessException("Apenas o técnico de destino pode responder a esta solicitação.");
		}

		// 2. Validação: Só pode responder se estiver PENDENTE
		if (!StatusTransferenciaEnum.PENDENTE.equals(transferencia.getStatus())) {
			throw new BadRequestBusinessException("Esta transferência já foi finalizada (" + transferencia.getStatus() + ")");
		}

		transferencia.setDataResposta(Instant.now());

		if (input.getAceito()) {
			// --- LÓGICA DE ACEITE ---
			transferencia.setStatus(StatusTransferenciaEnum.ACEITA);

			// A Mágica: Atualiza o dono do chamado
			ChamadoEntity chamado = transferencia.getChamado();
			
			// Opcional: Verificar se o chamado ainda está ABERTO antes de trocar
			if (!StatusChamadoEnum.ABERTO.equals(chamado.getStatus())) {
				// Se o chamado foi fechado nesse meio tempo, cancelamos a transferencia
				transferencia.setStatus(StatusTransferenciaEnum.CANCELADA); 
				transferencia.setMotivoRecusa("Chamado não está mais aberto.");
			} else {
				chamado.setTecnicoResponsavel(usuarioLogado); // O novo dono é quem aceitou
				chamadoRepository.save(chamado);
			}

		} else {
			// --- LÓGICA DE RECUSA ---
			if (input.getMotivoRecusa() == null || input.getMotivoRecusa().isBlank()) {
				throw new IllegalArgumentException("O motivo é obrigatório ao recusar uma transferência.");
			}
			transferencia.setStatus(StatusTransferenciaEnum.RECUSADA);
			transferencia.setMotivoRecusa(input.getMotivoRecusa());
		}

		transferenciaRepository.save(transferencia);
	}
	
}
