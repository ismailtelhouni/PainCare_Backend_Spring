package com.fstg.painCare.config;

import java.util.Arrays;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.fstg.painCare.config.jwt.AuthEntryPointJwt;
import com.fstg.painCare.config.jwt.AuthTokenFilter;
import com.fstg.painCare.config.service.UserDetailsServiceImpl;

@Configuration
@EnableMethodSecurity
@AllArgsConstructor
public class WebSecurityConfig {

	UserDetailsServiceImpl userDetailsService;

	private AuthEntryPointJwt unauthorizedHandler;
	
	// Configuration du filtre pour le jeton d'authentification JWT
	@Bean
	public AuthTokenFilter authenticationJwtTokenFilter() {
		return new AuthTokenFilter();
	}
	
	// Configuration du fournisseur d'authentification DAO
	@Bean
	public DaoAuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
	       
		authProvider.setUserDetailsService(userDetailsService);
		authProvider.setPasswordEncoder(passwordEncoder());
	   
		return authProvider;
	}
	
	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
	    return authConfig.getAuthenticationManager();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
	    return new BCryptPasswordEncoder();
	}
	
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		
//		On désactive la protection CSRF,C'est souvent nécessaire lors de la construction d'une API REST.et on configure
//		la gestion des exceptions ( .exceptionHandling ) liées à la sécurité. Si une exception d'authentification se produit, elle est gérée par 
//		unauthorizedHandler, qui renvoie une réponse appropriée au client.et on définit la gestion de la session. SessionCreationPolicy.
//		STATELESS signifie que Spring Security ne créera pas ni ne gérera les sessions, car votre application est sans état
//		(stateless), ce qui est souvent le cas pour les API REST.
//		Configure les autorisations pour différentes requêtes HTTP:
		
		http.csrf(csrf -> csrf.disable()).cors().and()
        .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeRequests(auth -> 
          auth.antMatchers("/api/auth/**").permitAll()
              .antMatchers("/image/**").permitAll()
              .anyRequest().authenticated()
        );
	    
	    http.authenticationProvider(authenticationProvider());

	    http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);
	    
	    return http.build();
	  }

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
	    CorsConfiguration configuration = new CorsConfiguration();
	    configuration.setAllowedOrigins(Arrays.asList("http://localhost:4200/"));
	    configuration.setAllowedMethods(Arrays.asList("*"));
	    configuration.setAllowedHeaders(Arrays.asList("*"));
	    configuration.setAllowCredentials(true);
	    

	    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
	    source.registerCorsConfiguration("/**", configuration);
	    return source;
	}
	
}
