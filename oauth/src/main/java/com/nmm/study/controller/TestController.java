package com.nmm.study.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/oauth/hello/{name}")
    public String hello(@PathVariable String name) {
        return name + " hello ";
    }

}
