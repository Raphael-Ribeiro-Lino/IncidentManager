package br.com.incidentemanager.helpdesk.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.incidentemanager.helpdesk.repositories.ChatMensagemRepository;

@Service
public class ChatMensagemService {

	@Autowired
	private ChatMensagemRepository chatMensagemRepository;
	
}
