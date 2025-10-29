package br.com.incidentemanager.helpdesk.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.incidentemanager.helpdesk.repositories.InteracaoRepository;

@Service
public class InteracaoService {

	@Autowired
	private InteracaoRepository interacaoRepository;
	
}
