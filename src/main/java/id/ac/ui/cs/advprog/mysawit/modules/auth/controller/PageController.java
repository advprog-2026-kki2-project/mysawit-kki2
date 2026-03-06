package id.ac.ui.cs.advprog.mysawit.modules.auth.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/login")
    public String loginPage() {
        return "login"; // resolves to login.html in templates/
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register"; // resolves to register.html in templates/
    }

    @GetMapping("/welcome")
    public String welcomePage() {
        return "welcome"; // optional welcome.html after login
    }
}