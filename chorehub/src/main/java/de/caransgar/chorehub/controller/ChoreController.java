package de.caransgar.chorehub.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
public class ChoreController {

    @GetMapping("/")
    public String getMethodName() {
        return "ChoreHub is running.";
    }

}
