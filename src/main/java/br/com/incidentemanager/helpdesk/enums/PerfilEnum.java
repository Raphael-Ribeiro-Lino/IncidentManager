package br.com.incidentemanager.helpdesk.enums;

import org.springframework.security.core.GrantedAuthority;

public enum PerfilEnum implements GrantedAuthority{
	ADMIN, ADMIN_EMPRESA, USUARIO, TECNICO_TI;

	@Override
	public String getAuthority() {
		return name();
	}

}
