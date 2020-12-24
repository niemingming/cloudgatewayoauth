package com.nmm.study.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ClientOneController {


    @GetMapping("/userinfo")
    public String userinfo(String code){
        System.out.println(code);
        return code;
    }

}
