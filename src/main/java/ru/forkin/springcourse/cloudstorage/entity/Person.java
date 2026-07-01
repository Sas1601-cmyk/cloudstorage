package ru.forkin.springcourse.cloudstorage.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.*;

@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString

@Entity
@Table(name = "people")
public class Person {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotEmpty(message = "Username must not be empty")
    @Size(min=3, max=68, message="Username length must be between 3 and 68 characters")
    @Column(name = "username", unique = true, nullable = false)
    private String username;

    @NotEmpty(message = "Password must not be empty")
    @Size(min=3, max=68, message="Password length must be between 3 and 68 characters")
    @Column(name = "password", nullable = false)
    private String password;

    public Person(String username, String password){
        this.username = username;
        this.password = password;
    }
}
