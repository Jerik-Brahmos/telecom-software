package com.example.dto;

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

    public @NotBlank(message = "Docker image URL is required") String getDockerImage() {
        return dockerImage;
    }

    public void setDockerImage(@NotBlank(message = "Docker image URL is required") String dockerImage) {
        this.dockerImage = dockerImage;
    }

    public @NotBlank(message = "Version is required") String getVersion() {
        return version;
    }

    public void setVersion(@NotBlank(message = "Version is required") String version) {
        this.version = version;
    }

    public @NotBlank(message = "App name is required") String getAppName() {
        return appName;
    }

    public void setAppName(@NotBlank(message = "App name is required") String appName) {
        this.appName = appName;
    }
}
