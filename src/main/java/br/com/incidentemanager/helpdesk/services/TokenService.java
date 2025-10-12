package br.com.incidentemanager.helpdesk.services;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import br.com.incidentemanager.helpdesk.entities.UsuarioEntity;
import br.com.incidentemanager.helpdesk.exceptions.UnauthorizedAccessBusinessException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;

@Service
public class TokenService {

	@Autowired
	private HttpServletRequest request;
	
	@Autowired
	private UsuarioService usuarioService;
	
	@Value("${api.security.token.secret}")
	private String secret;
	
	public String criaToken(Authentication authentication) {
		UsuarioEntity usuarioLogado = usuarioService.buscaPorEmail(authentication.getName());
		
		Date data = new Date();
		
		JwtBuilder builder = Jwts.builder();
		builder.claim("id", usuarioLogado.getId());
		builder.claim("email", usuarioLogado.getEmail());
		builder.claim("nome", usuarioLogado.getNome());
		builder.claim("perfil", usuarioLogado.getPerfil());
		builder.issuer("API SmartDesk");
		builder.issuedAt(data);
		builder.expiration(new Date(data.getTime() + Long.parseLong("86400000")));
		builder.signWith(getSignInKey());
		builder.header().add("typ", "JWT");
		return builder.compact();	
	}
	
	public UsuarioEntity buscaUsuario() {
		Claims claims = extractClaims();
		Long id = Long.valueOf(claims.get("id").toString());
		return usuarioService.buscaPorId(id);
	}
	
	private Claims extractClaims() {
		String token = request.getHeader("Authorization");
		if(token == null) {
			throw new UnauthorizedAccessBusinessException("Acesso Negado!");
		}
		token = token.substring(7);
		try {
			return Jwts.parser().verifyWith(getSignInKey()).build().parseSignedClaims(token).getPayload();
		} catch (Exception e) {
			throw new UnauthorizedAccessBusinessException("Token inválido!");
		}
	}
	
	private SecretKey getSignInKey() {
		byte[] keyBytes = Decoders.BASE64.decode(secret);
		return Keys.hmacShaKeyFor(keyBytes);
	}
}
