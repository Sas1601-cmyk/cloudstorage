package ru.forkin.springcourse.cloudstorage.person;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import ru.forkin.springcourse.cloudstorage.storage.FileStorageService;
import java.security.Principal;
import java.util.List;


@Slf4j
@Tag(name = "Auth and reg", description = "All method for auth and reg")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationManager authManager;
    private final PersonService personService;
    private final FileStorageService service;

    @Operation(summary = "Person registration")
    @PostMapping("/auth/sign-up")
    public ResponseEntity<?> signUp(
            @Parameter(description = "Input person user and password")
            @RequestBody @Valid PersonInputDto personInputDto,
            BindingResult bindingResult, HttpSession session) {
        log.info("signUp request={}", personInputDto.getUsername());
        if(bindingResult.hasErrors()){
            String errorMsg = getStringValidationMsg(bindingResult);
            throw new PersonValidationException(errorMsg);}
        PersonOutputDto personOutputDto = personService.register(personInputDto);
        service.createNewRootDirectoryForNewUser(personOutputDto.getUsername());
        Authentication token = new UsernamePasswordAuthenticationToken(personOutputDto.getUsername(), null, List.of());
        saveAuthentication(token, session);
        return ResponseEntity.status(201).body(personOutputDto);
    }

    @Operation(summary = "Person authentification")
    @PostMapping("/auth/sign-in")
    public ResponseEntity<?> signIn(
            @Parameter(description = "Input person user and password")
            @RequestBody @Valid PersonInputDto personInputDto, BindingResult bindingResult,
            HttpSession session) {
        log.info("signIn request={}", personInputDto.getUsername());
        if(bindingResult.hasErrors()){
            String errorMsg = getStringValidationMsg(bindingResult);
            throw new PersonValidationException(errorMsg);}
        personService.checkCorrectCredentialsFromUser(personInputDto.getUsername());
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(personInputDto.getUsername(), personInputDto.getPassword()));
        saveAuthentication(auth, session);
        return ResponseEntity.status(200).body(new PersonOutputDto(auth.getName()));
    }

    @Operation(summary = "Person Logout")
    @PostMapping("/auth/sign-out")
    public ResponseEntity<?> signOut(HttpServletRequest request, HttpServletResponse response) {
        log.info("signOut");
        SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
        logoutHandler.logout(request, response,
                SecurityContextHolder.getContext().getAuthentication());
        return ResponseEntity.status(204).build();
    }

    @Operation(summary = "Get username after authentification or registration")
    @GetMapping("/user/me")
    public ResponseEntity<?> getCurrentUser(Principal principal){
        log.info("getCurrentUser request={}", principal.getName());
        return ResponseEntity.status(200)
                .body(new PersonOutputDto(principal.getName()));
    }

    private void saveAuthentication(Authentication auth, HttpSession session) {
        SecurityContextHolder.getContext().setAuthentication(auth);
        session.setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                SecurityContextHolder.getContext());
    }

    private String getStringValidationMsg(BindingResult bindingResult){
        StringBuilder errorMsg = new StringBuilder();
        List<FieldError> errors = bindingResult.getFieldErrors();
        for(FieldError error: errors){
            errorMsg.append(error.getField())
                    .append(" - ").append(error.getDefaultMessage())
                    .append(";");}
        return errorMsg.toString();
    }
}