package com.akshathsaipittala.voiceai.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebUIController {

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/settings")
    public String settings() {
        return "settings";
    }
}
