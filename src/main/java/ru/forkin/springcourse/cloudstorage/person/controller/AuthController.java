package ru.forkin.springcourse.cloudstorage.person.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import ru.forkin.springcourse.cloudstorage.person.dto.PersonInputDto;
import ru.forkin.springcourse.cloudstorage.person.dto.PersonOutputDto;
import ru.forkin.springcourse.cloudstorage.person.exceptions.PersonValidationException;
import ru.forkin.springcourse.cloudstorage.person.PersonService;
import java.security.Principal;
import java.util.List;


@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationManager authManager;
    private final PersonService personService;

    @PostMapping("/auth/sign-up")
    public ResponseEntity<?> signUp(@RequestBody @Valid PersonInputDto personInputDto,
            BindingResult bindingResult, HttpSession session) {
        if(bindingResult.hasErrors()){
            String errorMsg = getStringValidationMsg(bindingResult);
            throw new PersonValidationException(errorMsg);
        }
        PersonOutputDto personOutputDto = personService.register(personInputDto);
        Authentication token = new UsernamePasswordAuthenticationToken(personOutputDto.getUsername(), null, List.of());
        saveAuthentication(token, session);
        return ResponseEntity.status(201).body(personOutputDto);
    }

    @PostMapping("/auth/sign-in")
    public ResponseEntity<?> signIn(
            @RequestBody @Valid PersonInputDto personInputDto, BindingResult bindingResult,
            HttpSession session) {
        if(bindingResult.hasErrors()){
            String errorMsg = getStringValidationMsg(bindingResult);
            throw new PersonValidationException(errorMsg);
        }
        personService.checkCorrectCredentialsFromUser(personInputDto.getUsername());
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(personInputDto.getUsername(), personInputDto.getPassword()));
        saveAuthentication(auth, session);
        return ResponseEntity.status(200).body(new PersonOutputDto(auth.getName()));
    }

    @PostMapping("/auth/sign-out")
    public ResponseEntity<?> signOut(HttpServletRequest request, HttpServletResponse response) {
        SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
        logoutHandler.logout(request, response,
                SecurityContextHolder.getContext().getAuthentication());
        return ResponseEntity.status(204).build();
    }

    @GetMapping("/user/me")
    public ResponseEntity<?> getCurrentUser(Principal principal){
        return ResponseEntity.status(200)
                .body(new PersonOutputDto(principal.getName()));
    }


    private void saveAuthentication(Authentication auth, HttpSession session) {
        SecurityContextHolder.getContext().setAuthentication(auth);
        session.setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                SecurityContextHolder.getContext()
        );
    }

    private String getStringValidationMsg(BindingResult bindingResult){
        StringBuilder errorMsg = new StringBuilder();
        List<FieldError> errors = bindingResult.getFieldErrors();
        for(FieldError error: errors){
            errorMsg.append(error.getField())
                    .append(" - ").append(error.getDefaultMessage())
                    .append(";");
        }
        return errorMsg.toString();
    }
}