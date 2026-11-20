package com.toycompiler.toycompiler.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller 
public class NavegacionController {

     @GetMapping("/")
    public String inicio() {
        return "index";
    }

    @GetMapping("/toy")
    public String mostrarToy() {
        return "toy";
    }
}
