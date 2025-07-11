package com.example.deploymentapi.controller;

import com.example.deploymentapi.dto.DeployRequest;
import com.example.deploymentapi.service.DeploymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/deploy")
public class DeployController {

    @Autowired
    private DeploymentService deploymentService;

    @PostMapping
    public ResponseEntity<String> deployApp(@Valid @RequestBody DeployRequest request) {
        deploymentService.handleDeployment(request);
        return ResponseEntity.ok("Deployment request sent.");
    }
}
