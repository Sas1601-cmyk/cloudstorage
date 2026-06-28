package ru.forkin.springcourse.cloudstorage.person.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class PersonInputDto {
    @NotEmpty(message = "Username must not be empty")
    @Size(min=3, max=68, message="Username length must be between 3 and 68 characters")
    private String username;

    @NotEmpty(message = "Password must not be empty")
    @Size(min=3, max=68, message="Password length must be between 3 and 68 characters")
    private String password;
}
