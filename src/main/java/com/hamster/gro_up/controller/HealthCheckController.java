package com.hamster.gro_up.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/health")
@RestController
public class HealthCheckController {

    @GetMapping
    public String healthCheck() {
        return "ok";
    }
}
