package ru.forkin.springcourse.cloudstorage.common.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class PersonErrorResponse {
    private String message;
}
