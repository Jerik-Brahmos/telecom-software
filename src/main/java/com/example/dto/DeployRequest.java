package com.example.deploymentapi.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DeployRequest {

    @NotBlank(message = "App name is required")
    private String appName;

    @NotBlank(message = "Version is required")
    private String version;

    @NotBlank(message = "Docker image URL is required")
    private String dockerImage;
}
