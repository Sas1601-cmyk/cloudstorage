package ru.forkin.springcourse.cloudstorage.storage.config;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Hidden
@Controller
public class SpaForwardController {
    @RequestMapping(value = {"/", "/files", "/files/**"})
    public String forward() {
        return "forward:/index.html";
    }
}