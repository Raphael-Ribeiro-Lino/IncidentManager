package br.com.incidentemanager.helpdesk.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;


import br.com.incidentemanager.helpdesk.entities.UsuarioEntity;
import br.com.incidentemanager.helpdesk.enums.PerfilEnum;
import br.com.incidentemanager.helpdesk.exceptions.BadRequestBusinessException;
import br.com.incidentemanager.helpdesk.repositories.UsuarioRepository;
import jakarta.transaction.Transactional;

@Service
public class UsuarioService {

	@Autowired
	private UsuarioRepository usuarioRepository;

	public boolean existeAdm() {
		if(usuarioRepository.findByPerfil(PerfilEnum.ADMIN).isPresent()) {
			return true;
		}else {			
			return false;
		}
	}

	private void existeUsuario(String email) {
		if(usuarioRepository.findByEmail(email).isPresent()) {
			throw new BadRequestBusinessException("O endereço de e-mail já está registrado. Por favor, escolha um endereço de e-mail diferente ou faça login na sua conta existente");
		}
	}
	
	@Transactional
	public UsuarioEntity cadastra(UsuarioEntity usuarioEntity) {
		existeUsuario(usuarioEntity.getEmail());
		usuarioEntity.setPassword(new BCryptPasswordEncoder().encode(usuarioEntity.getPassword()));
		return usuarioRepository.save(usuarioEntity);
	}

	
	
}
