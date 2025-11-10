package com.bcm.shared;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class NodeController {

    @GetMapping("/example")
    public String test(){
        return "Here is a string";
    }
}