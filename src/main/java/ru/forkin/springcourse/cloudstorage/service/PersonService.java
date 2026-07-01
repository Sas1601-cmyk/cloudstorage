package ru.forkin.springcourse.cloudstorage.service;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.forkin.springcourse.cloudstorage.config.PersonDetailsService;
import ru.forkin.springcourse.cloudstorage.dto.person.PersonInputDto;
import ru.forkin.springcourse.cloudstorage.dto.person.PersonOutputDto;
import ru.forkin.springcourse.cloudstorage.entity.Person;
import ru.forkin.springcourse.cloudstorage.exception.person.PersonDuplicatedException;
import ru.forkin.springcourse.cloudstorage.exception.storage.StorageException;
import ru.forkin.springcourse.cloudstorage.repository.PersonRepository;

import java.io.ByteArrayInputStream;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PersonService {
    @Value("${app.storage.bucket-name}")
    private String bucketName;
    private final PersonRepository personRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authManager;
    private final MinioClient minioClient;
    private final PersonDetailsService personDetailsService;

    @Transactional
    public PersonOutputDto register(PersonInputDto personInputDto, HttpSession session) {
        boolean isPresent = personRepository.findByUsername(personInputDto.getUsername()).isPresent();
        if (isPresent) { log.error("User with this username already exists");
            throw new PersonDuplicatedException("User with this username already exists");}
        personInputDto.setPassword(passwordEncoder.encode(personInputDto.getPassword()));
        personRepository.save(new Person(personInputDto.getUsername(), personInputDto.getPassword()));
        UserDetails userDetails = personDetailsService.loadUserByUsername(personInputDto.getUsername());
        Authentication token = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        saveAuthentication(token, session);
        createNewRootDirectoryForNewUser(personInputDto.getUsername());
        return new PersonOutputDto(personInputDto.getUsername());
    }

    public Authentication signIn(PersonInputDto personInputDto, HttpSession session){
        checkCorrectCredentialsFromUser(personInputDto.getUsername());
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(personInputDto.getUsername(), personInputDto.getPassword()));
        saveAuthentication(auth, session);
        return auth;
    }

    public void signOut(HttpServletRequest request, HttpServletResponse response){
        SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
        logoutHandler.logout(request, response, SecurityContextHolder.getContext().getAuthentication());
    }

    public Integer getPersonIdByUsername(String username){
        return personRepository.getPersonByUsername(username).getId();}

    private void checkCorrectCredentialsFromUser(String personName){
        if(personRepository.findByUsername(personName).isEmpty()){
            log.error("Bad credentials from person={}", personName);
            throw new BadCredentialsException("Bad credentials");}}

    private void saveAuthentication(Authentication auth, HttpSession session) {
        SecurityContextHolder.getContext().setAuthentication(auth);
        session.setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                SecurityContextHolder.getContext());}

    private void createNewRootDirectoryForNewUser(String username){
        Integer id = getPersonIdByUsername(username);
        String fullPath = "user-".concat(id.toString()).concat("-files/");
        try{ minioClient.putObject(
                PutObjectArgs.builder().bucket(bucketName).object(fullPath)
                        .stream(new ByteArrayInputStream(new byte[0]), 0, -1)
                        .contentType("application/x-directory").build());
        }catch (Exception e){ log.error("Storage error for create root directory for person={}", username);
            throw new StorageException("Storage error for create root directory for person");}}
}