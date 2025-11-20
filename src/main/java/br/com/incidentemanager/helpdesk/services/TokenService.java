package br.com.incidentemanager.helpdesk.services;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import br.com.incidentemanager.helpdesk.entities.TokensInvalidadosEntity;
import br.com.incidentemanager.helpdesk.entities.UsuarioEntity;
import br.com.incidentemanager.helpdesk.exceptions.UnauthorizedAccessBusinessException;
import br.com.incidentemanager.helpdesk.repositories.TokensInvalidadosRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;

@Service
public class TokenService {

	@Autowired
	private HttpServletRequest request;

	@Autowired
	private UsuarioService usuarioService;

	@Autowired
	private TokensInvalidadosRepository tokensInvalidadosRepository;

	@Value("${api.security.token.secret}")
	private String secret;

	@Value("${api.security.token.expiration}")
	private Long expiration;

	public String criaToken(Authentication authentication) {
		UsuarioEntity usuarioLogado = usuarioService.buscaPorEmail(authentication.getName());
		verificaSeUsuarioEstaAtivo(usuarioLogado);
		Date data = new Date();

		JwtBuilder builder = Jwts.builder();
		builder.claim("id", usuarioLogado.getId());
		builder.claim("email", usuarioLogado.getEmail());
		builder.claim("nome", usuarioLogado.getNome());
		builder.claim("perfil", usuarioLogado.getPerfil());
		builder.claim("ativo", usuarioLogado.isAtivo());
		if (usuarioLogado.getEmpresa() != null) {
			builder.claim("empresa_id", usuarioLogado.getEmpresa().getId());
		}
		builder.issuer("API SmartDesk");
		builder.issuedAt(data);
		builder.expiration(new Date(data.getTime() + expiration));
		builder.signWith(getSignInKey());
		builder.header().add("typ", "JWT");
		return builder.compact();
	}

	private void verificaSeUsuarioEstaAtivo(UsuarioEntity usuarioLogado) {
		if (!usuarioLogado.isAtivo()) {
			throw new UnauthorizedAccessBusinessException(
					"Seu usuário está inativo. Entre em contato com o administrador para reativar seu acesso.");
		}
	}

	public UsuarioEntity buscaUsuario() {
		Claims claims = extractClaims();
		Long id = Long.valueOf(claims.get("id").toString());
		return usuarioService.buscaPorId(id);
	}

	private Claims extractClaims() {
		String token = request.getHeader("Authorization");
		if (token == null) {
			throw new UnauthorizedAccessBusinessException("Acesso Negado!");
		}
		token = token.substring(7);
		String hash = gerarHashToken(token);
		if (tokensInvalidadosRepository.existsByTokenHash(hash)) {
			throw new UnauthorizedAccessBusinessException("Token inválido! Logout já realizado.");
		}
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

	public boolean podeAcessarAutenticado() {
		Claims claims = extractClaims();
		if (claims == null) {
			return false;
		}
		UsuarioEntity usuarioEntity = buscaUsuario();
		verificaSeUsuarioEstaAtivo(usuarioEntity);
		return true;
	}

	public boolean podeAcessarPorPerfil(List<String> perfisPermitidos) {
		UsuarioEntity usuarioEntity = buscaUsuario();
		verificaSeUsuarioEstaAtivo(usuarioEntity);
		return perfisPermitidos.stream().anyMatch(perfil -> perfil.equalsIgnoreCase(usuarioEntity.getPerfil().name()));

	}

	@Transactional
	public void invalidarTokenAtual() {
		String token = request.getHeader("Authorization");
		token = token.substring(7);

		String hash = gerarHashToken(token);
		if (tokensInvalidadosRepository.existsByTokenHash(hash)) {
			return;
		}

		try {
			Claims claims = Jwts.parser().verifyWith(getSignInKey()).build().parseSignedClaims(token).getPayload();
			Date expirationDate = claims.getExpiration();

			LocalDateTime expiracao = expirationDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

			UsuarioEntity usuario = buscaUsuario();

			TokensInvalidadosEntity tokenInvalido = new TokensInvalidadosEntity();
			tokenInvalido.setTokenHash(hash);
			tokenInvalido.setUsuario(usuario);
			tokenInvalido.setCriadoEm(LocalDateTime.now());
			tokenInvalido.setExpiracao(expiracao);

			tokensInvalidadosRepository.save(tokenInvalido);

		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}

	private String gerarHashToken(String token) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] encodedhash = digest.digest(token.getBytes(StandardCharsets.UTF_8));

			StringBuilder hexString = new StringBuilder(2 * encodedhash.length);
			for (int i = 0; i < encodedhash.length; i++) {
				String hex = Integer.toHexString(0xff & encodedhash[i]);
				if (hex.length() == 1) {
					hexString.append('0');
				}
				hexString.append(hex);
			}
			return hexString.toString();
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("Erro ao gerar hash do token", e);
		}
	}
}
