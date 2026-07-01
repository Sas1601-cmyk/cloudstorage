package ru.forkin.springcourse.cloudstorage.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.forkin.springcourse.cloudstorage.dto.person.PersonInputDto;
import ru.forkin.springcourse.cloudstorage.dto.person.PersonOutputDto;
import ru.forkin.springcourse.cloudstorage.service.PersonService;

import java.security.Principal;

@Slf4j
@Tag(name = "Auth and reg", description = "All method for auth and reg")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController{
    private final PersonService personService;

    @Operation(summary = "Person registration")
    @PostMapping("/auth/sign-up")
    public ResponseEntity<?> signUp(
            @RequestBody @Valid PersonInputDto personInputDto, HttpSession session) {
        log.info("signUp request={}", personInputDto.getUsername());
        PersonOutputDto personOutputDto = personService.register(personInputDto, session);
        return ResponseEntity.status(201).body(personOutputDto);
    }

    @Operation(summary = "Person authentification")
    @PostMapping("/auth/sign-in")
    public ResponseEntity<?> signIn(
            @RequestBody @Valid PersonInputDto personInputDto, HttpSession session) {
        log.info("signIn request={}", personInputDto.getUsername());
        Authentication auth = personService.signIn(personInputDto, session);
        return ResponseEntity.status(200).body(new PersonOutputDto(auth.getName()));
    }

    @Operation(summary = "Person Logout")
    @PostMapping("/auth/sign-out")
    public ResponseEntity<?> signOut(HttpServletRequest request, HttpServletResponse response) {
        log.info("signOut");
        personService.signOut(request, response);
        return ResponseEntity.status(204).build();
    }

    @Operation(summary = "Get username after authentification or registration")
    @GetMapping("/user/me")
    public ResponseEntity<?> getCurrentUser(Principal principal){
        log.info("getCurrentUser request={}", principal.getName());
        return ResponseEntity.status(200).body(new PersonOutputDto(principal.getName()));
    }
}