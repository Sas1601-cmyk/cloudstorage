package ru.forkin.springcourse.cloudstorage.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.forkin.springcourse.cloudstorage.person.exceptions.PersonNotFoundException;
import ru.forkin.springcourse.cloudstorage.person.Person;
import ru.forkin.springcourse.cloudstorage.person.PersonRepository;

@Service
@RequiredArgsConstructor
public class PersonDetailsService implements UserDetailsService {

    private final PersonRepository personRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Person person = personRepository.findByUsername(username)
                .orElseThrow(() -> new PersonNotFoundException("User " + username + " not found!"));
        // Возвращаем обертку, которую ожидает Spring Security
        return new org.springframework.security.core.userdetails.User(
                person.getUsername(),
                person.getPassword(),
                java.util.Collections.emptyList() // Пока без ролей
        );
    }
}
