package br.com.incidentemanager.helpdesk.services;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.stereotype.Service;

import br.com.incidentemanager.helpdesk.entities.LayoutEmailEntity;
import br.com.incidentemanager.helpdesk.exceptions.BusinessException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class EmailService {

	public void enviaEmail(String emailDestino,String layoutName, String sourceEmail, String subject, String body) {
		LayoutEmailEntity layoutEmailEntity = new LayoutEmailEntity(null, layoutName, sourceEmail, subject, body);
		final String username = System.getenv("EMAIL_USERNAME");;
		final String password = System.getenv("EMAIL_PASSWORD");

		if (username == null || password == null || password.isBlank() || username.isBlank()) {
			throw new BusinessException("Credenciais de e-mail não configuradas no ambiente.");
		}

		Properties props = new Properties();
		props.put("mail.smtp.auth", true);
		props.put("mail.smtp.starttls.enable", true);
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.port", "587");

		Session session = Session.getInstance(props, new javax.mail.Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, password);
			}
		});

		session.setDebug(true);

		try {
			Message message = buildMessage(session, emailDestino, username, layoutEmailEntity);
			log.info("Enviando e-mail para {}", emailDestino);

			try (Transport transport = session.getTransport("smtp")) {
				transport.connect(username, password);
				transport.sendMessage(message, message.getAllRecipients());
			}

			log.info("E-mail enviado com sucesso para {}", emailDestino);

		} catch (MessagingException e) {
			log.error("Erro ao enviar e-mail para {}: {}", emailDestino, e.getMessage());
			throw new BusinessException("Falha ao enviar e-mail. Detalhes: " + e.getMessage());
		}
	}

	private Message buildMessage(Session session, String to, String from, LayoutEmailEntity layout)
			throws MessagingException {
		Message message = new MimeMessage(session);
		message.setFrom(new InternetAddress(from));
		message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
		message.setSubject(layout.getSubject());
		message.setContent(layout.getBody(), "text/html; charset=utf-8");
		return message;
	}
}
