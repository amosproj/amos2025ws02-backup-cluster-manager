package com.bcm;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller {
    @GetMapping("/example")
    public String test(){
        return "Here is a string";
    }
}
