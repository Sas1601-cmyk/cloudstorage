package ru.forkin.springcourse.cloudstorage.controller;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Hidden
@Controller
public class SpaForwardController {
    @RequestMapping(value = {"/", "/files", "/files/**", "/login", "/registration"})
    public String forward() {
        return "forward:/index.html";
    }
}