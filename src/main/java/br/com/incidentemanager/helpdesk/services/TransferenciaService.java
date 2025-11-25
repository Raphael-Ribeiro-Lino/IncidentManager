package br.com.incidentemanager.helpdesk.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
	
}
