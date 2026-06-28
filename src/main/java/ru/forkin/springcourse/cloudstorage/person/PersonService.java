package ru.forkin.springcourse.cloudstorage.person;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.forkin.springcourse.cloudstorage.person.dto.PersonInputDto;
import ru.forkin.springcourse.cloudstorage.person.dto.PersonOutputDto;
import ru.forkin.springcourse.cloudstorage.person.exceptions.PersonDuplicatedException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PersonService {
    private final PersonRepository personRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public PersonOutputDto register(PersonInputDto personInputDto) {
        if (personRepository.findByUsername(personInputDto.getUsername()).isPresent()) {
            throw new PersonDuplicatedException("User with this username already exists");}
        personInputDto.setPassword(passwordEncoder.encode(personInputDto.getPassword()));
        personRepository.save(new Person(
                personInputDto.getUsername(),
                personInputDto.getPassword()));
        return new PersonOutputDto(personInputDto.getUsername());
    }

    public Integer getPersonIdByUsername(String username){
        return personRepository.getPersonByUsername(username).getId();
    };

    public void checkCorrectCredentialsFromUser(String personName){
        if(personRepository.findByUsername(personName).isEmpty()){
            throw new BadCredentialsException("Bad credentials");
        }
    }
}