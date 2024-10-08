package com.fstg.painCare.controllers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fstg.painCare.config.jwt.JwtUtils;
import com.fstg.painCare.config.service.UserDetailsImpl;
import com.fstg.painCare.dao.UserDao;
import com.fstg.painCare.dto.FemmeDto;
import com.fstg.painCare.dto.UserDto;
import com.fstg.painCare.dto.auth.JwtResponseDto;
import com.fstg.painCare.dto.auth.LoginDto;
import com.fstg.painCare.dto.auth.RegisterDto;
import com.fstg.painCare.service.facade.FemmeService;
import com.fstg.painCare.shared.ErrorMessage;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
@NoArgsConstructor
public class AuthController {

	private AuthenticationManager authenticationManager;

	private UserDao userDao;

	private FemmeService femmeService;

  	private PasswordEncoder encoder;

    private JwtUtils jwtUtils;
    
    @PostMapping("/login")
    public ResponseEntity<JwtResponseDto> authenticateUser(@Valid @RequestBody LoginDto loginDto) {

    	System.out.println("test");
    	Authentication authentication = authenticationManager.authenticate(
    			new UsernamePasswordAuthenticationToken(loginDto.getEmail(), loginDto.getPassword()));
    	System.out.println("test2");
    	SecurityContextHolder.getContext().setAuthentication(authentication);
    	String jwt = jwtUtils.generateJwtToken(authentication);
      
    	UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();    
    	List<String> roles = userDetails.getAuthorities().stream()
    			.map(item -> item.getAuthority())
    			.collect(Collectors.toList());

    	FemmeDto femmeDto = femmeService.findByUser(userDetails.getId());
    	
    	return ResponseEntity.ok(new JwtResponseDto(jwt, 
                           userDetails.getId(), 
                           femmeDto.getFemmeId(),
                           userDetails.getUsername(),  
                           roles , 
                           "success"
                           ));
    }
    
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterDto registerDto) {
    	
    	
    	
    	FemmeDto femmeDto = new FemmeDto(null, registerDto.getName(), registerDto.getSurname(), "avatar1", "ville", "rue_add", "061414", "WA3443", "test", new UserDto(null, null, registerDto.getEmail(), registerDto.getPassword(), null));
    	
      if (userDao.existsByEmail(femmeDto.getUser().getEmail())) {
        return ResponseEntity
            .badRequest()
            .body(new ErrorMessage("Error: Email is already in use!",new Date(),HttpStatus.BAD_REQUEST.value()));
      }

      String password = femmeDto.getUser().getPassword();
      
      String hashedPassword = encoder.encode(password);
      
      femmeDto.getUser().setPassword(hashedPassword);
      
      // Create new user's account
      FemmeDto saved = femmeService.saveWithUser(femmeDto);
      
      Authentication authentication = authenticationManager.authenticate(
    	        new UsernamePasswordAuthenticationToken(saved.getUser().getEmail(), password ));

    	SecurityContextHolder.getContext().setAuthentication(authentication);
    	String jwt = jwtUtils.generateJwtToken(authentication);

    	List<String> role = new ArrayList<>();
    	role.add(saved.getUser().getRole().getName());
      return ResponseEntity.ok(new JwtResponseDto(jwt, 
              saved.getUser().getUserId(), 
              saved.getFemmeId(),
              saved.getUser().getEmail(),  
              role , 
              "success"
              ));
    }

}
