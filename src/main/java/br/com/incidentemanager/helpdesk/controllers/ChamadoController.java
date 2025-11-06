package br.com.incidentemanager.helpdesk.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.incidentemanager.helpdesk.configs.ControllerConfig;
import br.com.incidentemanager.helpdesk.converts.ChamadoConvert;
import br.com.incidentemanager.helpdesk.services.ChamadoService;

@RestController
@RequestMapping(ControllerConfig.PRE_URL + "/chamado")
@CrossOrigin(origins = { "http://localhost", "http://localhost:4200", "http://localhost:4200/*" })
public class ChamadoController {

	@Autowired
	private ChamadoService chamadoService;
	
	@Autowired
	private ChamadoConvert chamadoConvert;
	
}
