package br.com.incidentemanager.helpdesk.configs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

	@Value("${app.frontend.base-url}")
	private String frontendBaseUrl;

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
					"Foi solicitada a recuperação de senha do SmartDesk.<br /> <a href='" + frontendBaseUrl
							+ "/redefinir-senha/"
							+ "{hash}' target='_blank'>Clique aqui</a> para alterar sua senha.<br />"
							+ "O link é válido por 15 minutos.<br />"
							+ "Caso não tenha solicitado a alteração, ignore esta mensagem!");
		}

		if (!layoutEmailService.existeAvisoAlteracaoSenha()) {
			log.info("Criando layout padrão para aviso alteração de senha...");
			criaLayoutSeNecessario("Aviso de alteração de senha", "contato.smartdesk@gmail.com",
					"Notificação de alteração de senha",
					"Olá!<br/><br/>" + "Informamos que a senha da sua conta foi alterada recentemente.<br/>"
							+ "Se você não realizou essa alteração, por favor, entre em contato imediatamente com nosso suporte.<br/><br/>"
							+ "Data/Hora da alteração: {dataHoraAlteracao}<br/><br/>" + "Atenciosamente,<br/>"
							+ "Equipe de Segurança");
		}

		if (!layoutEmailService.existeCriacaoDeSenha()) {
			log.info("Criando layout padrão para criação de senha...");
			criaLayoutSeNecessario("Criação de senha de acesso", "contato.smartdesk@gmail.com",
					"Bem-vindo ao SmartDesk - Defina sua senha",
					"Olá {nomeUsuario},<br/><br/>" + "Sua conta no <b>SmartDesk</b> foi criada com sucesso!<br/><br/>"
							+ "Para começar a usar o sistema, é necessário definir sua senha de acesso.<br/><br/>"
							+ "<a href='" + frontendBaseUrl + "/definir-senha/{hash}' target='_blank' "
							+ "style='display:inline-block; background-color:#007bff; color:#fff; "
							+ "padding:10px 20px; text-decoration:none; border-radius:5px;'>Definir Senha</a><br/><br/>"
							+ "Este link é válido por <b>15</b> minutos. Após esse período, será necessário solicitar um novo link.<br/><br/>"
							+ "Se você não esperava este e-mail, apenas ignore esta mensagem.<br/><br/>"
							+ "Atenciosamente,<br/>" + "Equipe SmartDesk");
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
