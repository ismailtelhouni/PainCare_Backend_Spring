package com.fstg.painCare.config.jwt;

import java.security.Key;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.fstg.painCare.config.service.UserDetailsImpl;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtils {
	
	private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    // Méthode pour générer un jeton JWT à partir de l'authentification de l'utilisateur
	public String generateJwtToken(Authentication authentication) {

		// Récupère les détails de l'utilisateur à partir de l'authentification
	    UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();
	    
	    // Construit le jeton JWT avec le nom d'utilisateur, la date d'émission, la date d'expiration et une clé secrète
        //    @Value("${PainCare.jwtExpirationMs}")
        int jwtExpirationMs = 20240000;
        return Jwts.builder()
	        .setSubject((userPrincipal.getUsername()))
	        .setIssuedAt(new Date())
	        .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
	        .signWith(key(), SignatureAlgorithm.HS256)
	        .compact();
	}
	
	// Méthode pour créer une clé à partir de la clé secrète en Base64
	private Key key() {
        //	@Value("${PainCare.jwtSecret}")
        String jwtSecret = "|`kl]\"D%rl%t*gil@^E!S3Sxu'QIt1LaA\"5_~9=,z8S#K>tNBLa\"$2c$Mpn8^'_";
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
	}
	
	// Méthode pour extraire le nom d'utilisateur à partir du jeton JWT
	public String getUserNameFromJwtToken(String token) {
	    return Jwts.parserBuilder().setSigningKey(key()).build()
	               .parseClaimsJws(token).getBody().getSubject();
	}

	// Méthode pour valider la validité du jeton JWT
	public boolean validateJwtToken(String authToken) {
	    try {
	    	// Parse le jeton JWT en utilisant la clé secrète
	    	Jwts.parserBuilder().setSigningKey(key()).build().parse(authToken);
	    	return true;
	    } catch (MalformedJwtException e) {
	    	logger.error("Invalid JWT token: {}", e.getMessage());
	    } catch (ExpiredJwtException e) {
	    	logger.error("JWT token is expired: {}", e.getMessage());
	    } catch (UnsupportedJwtException e) {
	    	logger.error("JWT token is unsupported: {}", e.getMessage());
	    } catch (IllegalArgumentException e) {
	    	logger.error("JWT claims string is empty: {}", e.getMessage());
	    }

	    return false;
	}
	
}
