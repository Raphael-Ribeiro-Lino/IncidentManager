package br.com.incidentemanager.helpdesk.configs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;

import br.com.incidentemanager.helpdesk.entities.LayoutEmailEntity;
import br.com.incidentemanager.helpdesk.entities.UsuarioEntity;
import br.com.incidentemanager.helpdesk.enums.PerfilEnum;
import br.com.incidentemanager.helpdesk.services.LayoutEmailService;
import br.com.incidentemanager.helpdesk.services.UsuarioService;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class ExecutaAposInicioDaAplicacao implements ApplicationRunner {

	@Autowired
	private UsuarioService usuarioService;

	@Autowired
	private LayoutEmailService layoutEmailService;

	@Override
	public void run(ApplicationArguments args) throws Exception {
		if (!usuarioService.existeAdm()) {
			log.info("Criando usuário administrador padrão...");
			criaUsuarioSeNecessario("SmartDesk Adm", "smartdesk@gmail.com", "(11) 98765-4321", "@PrimeiraSenha123",
					true, PerfilEnum.ADMIN);
		}

		if (!layoutEmailService.existeRedefinirSenha()) {
			log.info("Criando layout padrão para redefinição de senha...");
			criaLayoutSeNecessario("Redefinir Senha", "contato.smartdesk@gmail.com", "Redefina a sua senha",
					"Foi solicitada a recuperação de senha do SmartDesk.<br /> <a href='"
							+ "http://localhost:4200/redefinir-senha/"
							+ "{HASH}' target='_blank'>Clique aqui</a> para alterar sua senha.<br />"
							+ "O link é válido por 15 minutos.<br />"
							+ "Caso não tenha solicitado a alteração, ignore esta mensagem!");
		}

	}

	private void criaLayoutSeNecessario(String name, String sourceEmail, String subject, String body) {
		LayoutEmailEntity layoutEmailEntity = new LayoutEmailEntity(null, name, sourceEmail, subject, body);
		layoutEmailService.cadastra(layoutEmailEntity);
	}

	private void criaUsuarioSeNecessario(String nome, String email, String telefone, String senha, boolean ativo,
			PerfilEnum perfil) {
		UsuarioEntity usuarioEntity = new UsuarioEntity(null, nome, email, telefone, senha, ativo, perfil, null);
		usuarioService.cadastraAdm(usuarioEntity);
	}

}
