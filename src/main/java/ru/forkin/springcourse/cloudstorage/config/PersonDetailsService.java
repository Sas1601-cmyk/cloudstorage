package ru.forkin.springcourse.cloudstorage.config;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.forkin.springcourse.cloudstorage.exception.person.PersonNotFoundException;
import ru.forkin.springcourse.cloudstorage.entity.Person;
import ru.forkin.springcourse.cloudstorage.repository.PersonRepository;

@Service
@RequiredArgsConstructor
public class PersonDetailsService implements UserDetailsService {

    private final PersonRepository personRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Person person = personRepository.findByUsername(username)
                .orElseThrow(() -> new PersonNotFoundException("User " + username + " not found!"));
        return new org.springframework.security.core.userdetails.User(
                person.getUsername(),
                person.getPassword(),
                java.util.Collections.emptyList() // No roles
        );
    }
}
