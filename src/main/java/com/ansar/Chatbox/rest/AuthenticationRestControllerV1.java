package com.ansar.Chatbox.rest;

import com.ansar.Chatbox.dto.AuthenticationRequestDto;
import com.ansar.Chatbox.model.Status;
import com.ansar.Chatbox.model.User;
import com.ansar.Chatbox.security.IAuthenticationFacade;
import com.ansar.Chatbox.security.jwt.JwtTokenProvider;
import com.ansar.Chatbox.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.AuthenticationException;

import java.security.Principal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/v1/auth/")
public class AuthenticationRestControllerV1 {
    private final AuthenticationManager authenticationManager;

    private final JwtTokenProvider jwtTokenProvider;

    private final UserService userService;

    @Autowired
    private IAuthenticationFacade authenticationFacade;

    @Autowired
    public AuthenticationRestControllerV1(AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider, UserService userService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userService = userService;
        this.authenticationManager = authenticationManager;
    }
    @PostMapping("register")
    public ResponseEntity register (@RequestBody AuthenticationRequestDto requestDto){
        User newUser=new User();
        newUser.setLastName("lastbel");
        newUser.setFirstName("firbek");
        newUser.setEmail("Rrrr@mail.ru");
        newUser.setStatus(Status.ACTIVE);
        newUser.setPassword(requestDto.getPassword());
        newUser.setUsername(requestDto.getUsername());
        newUser.setCreated(new Date());
        newUser.setUpdated(new Date());
        User user= userService.register(newUser);
        return ResponseEntity.ok(user);
    }

    @GetMapping("user")
    public ResponseEntity getUserService() {
        Map<Object, Object> response = new HashMap<>();
        Authentication authentication = authenticationFacade.getAuthentication();
        response.put("username",authentication.getName());
        response.put("token", "token1");
        return ResponseEntity.ok(response);
    }

    @PostMapping("login")
    public ResponseEntity login (@RequestBody AuthenticationRequestDto requestDto){
        try{
            String username = requestDto.getUsername();
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, requestDto.getPassword()));
            User user = userService.findByUsername(username);
            if (user == null) {
                throw new UsernameNotFoundException("User with username: " + username + " not found");
            }
            String token = jwtTokenProvider.createToken(username, user.getRoles());
            Map<Object, Object> response = new HashMap<>();
            response.put("username", username);
            response.put("token", token);
            return ResponseEntity.ok(response);
        } catch (AuthenticationException e) {

            throw new BadCredentialsException("Invalid username or password");
        }
    }
}
