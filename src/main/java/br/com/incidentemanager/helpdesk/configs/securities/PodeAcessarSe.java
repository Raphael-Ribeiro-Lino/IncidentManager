package br.com.incidentemanager.helpdesk.configs.securities;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.security.access.prepost.PreAuthorize;

public @interface PodeAcessarSe {

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	@PreAuthorize("@tokenService.podeAcessarAutenticado()")
	public @interface EstaAutenticado {

	}
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	@PreAuthorize("@tokenService.podeAcessarPorPerfil('ADMIN')")
	public @interface TemPerfilAdm{
		
	}
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	@PreAuthorize("@tokenService.podeAcessarPorPerfil({'ADMIN', 'ADMIN_EMPRESA'})")
	public @interface TemPerfilAdmEmpresa{
		
	}
}
