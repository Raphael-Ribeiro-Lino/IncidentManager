package br.com.incidentemanager.helpdesk.configs;

import java.util.Properties;

import javax.mail.PasswordAuthentication;
import javax.mail.Session;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import br.com.incidentemanager.helpdesk.exceptions.BusinessException;

@Configuration
public class MailConfig {

    @Bean
    Session mailSession() {
		
		final String username = System.getenv("EMAIL_USERNAME");
        final String password = System.getenv("EMAIL_PASSWORD");
        
        if (username == null || password == null || password.isBlank() || username.isBlank()) {
			throw new BusinessException("Credenciais de e-mail não configuradas no ambiente.");
		}
		
		Properties props = new Properties();
        props.put("mail.smtp.auth", true);
        props.put("mail.smtp.starttls.enable", true);
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        
        return Session.getInstance(props, new javax.mail.Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
        
	}

}
