package br.com.incidentemanager.helpdesk.configs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;

import br.com.incidentemanager.helpdesk.entities.UsuarioEntity;
import br.com.incidentemanager.helpdesk.enums.PerfilEnum;
import br.com.incidentemanager.helpdesk.services.UsuarioService;

@Configuration
public class ExecutaAposInicioDaAplicacao implements ApplicationRunner {
	
	@Autowired
	private UsuarioService usuarioService;

	@Override
	public void run(ApplicationArguments args) throws Exception {
		if(!usuarioService.existeAdm()) {
			criaUsuarioSeNecessario("SmartDesk Adm", "smartdesk@gmail.com", "(11) 98765-4321", "@PrimeiraSenha", true, PerfilEnum.ADMIN);
		}
		
	}

	private void criaUsuarioSeNecessario(String nome, String email, String telefone, String senha, boolean ativo,
			PerfilEnum perfil) {
		UsuarioEntity usuarioEntity = new UsuarioEntity(null, nome, email, telefone, senha, ativo, perfil, null);
		usuarioService.cadastra(usuarioEntity);
	}

}
