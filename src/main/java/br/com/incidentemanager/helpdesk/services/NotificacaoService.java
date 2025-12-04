package br.com.incidentemanager.helpdesk.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import br.com.incidentemanager.helpdesk.entities.ChamadoEntity;
import br.com.incidentemanager.helpdesk.entities.NotificacaoEntity;
import br.com.incidentemanager.helpdesk.entities.UsuarioEntity;
import br.com.incidentemanager.helpdesk.enums.TipoNotificacaoEnum;
import br.com.incidentemanager.helpdesk.exceptions.NotFoundBusinessException;
import br.com.incidentemanager.helpdesk.repositories.NotificacaoRepository;
import jakarta.transaction.Transactional;

@Service
public class NotificacaoService {

	@Autowired
	private NotificacaoRepository notificacaoRepository;

	public Page<NotificacaoEntity> lista(Pageable pagination, UsuarioEntity usuarioLogado, Boolean lido) {
		return notificacaoRepository.findAllByDestinatarioFiltrado(pagination, usuarioLogado, lido);
	}

	@Transactional
	public void criar(UsuarioEntity destinatario, String titulo, String mensagem, ChamadoEntity chamado, TipoNotificacaoEnum tipo) {
		NotificacaoEntity notificacao = new NotificacaoEntity();
		notificacao.setDestinatario(destinatario);
		notificacao.setTitulo(titulo);
		notificacao.setMensagem(mensagem);
		notificacao.setChamado(chamado);
		notificacao.setTipo(tipo);
		notificacao.setLido(false);
		
		notificacaoRepository.save(notificacao);
	}

	public long contarNaoLidas(UsuarioEntity usuario) {
		return notificacaoRepository.countByDestinatarioAndLidoFalse(usuario);
	}

	@Transactional
	public void marcarComoLida(Long id, UsuarioEntity usuario) {
		NotificacaoEntity notificacao = notificacaoRepository.findById(id)
				.orElseThrow(() -> new NotFoundBusinessException("Notificação não encontrada"));

		if (!notificacao.getDestinatario().getId().equals(usuario.getId())) {
			throw new NotFoundBusinessException("Notificação não pertence ao usuário.");
		}

		notificacao.setLido(true);
		notificacaoRepository.save(notificacao);
	}

	@Transactional
	public void marcarTodasComoLidas(UsuarioEntity usuario) {
		notificacaoRepository.marcarTodasComoLidas(usuario);
	}
}
