package br.com.incidentemanager.helpdesk.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.incidentemanager.helpdesk.repositories.AnexoRepository;

@Service
public class AnexoService {

	@Autowired
	private AnexoRepository anexoRepository; 
}
