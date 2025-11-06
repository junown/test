package com.example.testapi;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    // 루트(/) 경로로 오면 index.html 파일을 보여줌
    @GetMapping("/")
    public String mainPage() {
        return "index";
    }
}