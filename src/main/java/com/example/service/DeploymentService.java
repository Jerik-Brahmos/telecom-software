package com.example.service;

import com.example.dto.DeployRequest;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;

@Service
public class DeploymentService {

    private static final String REPO_PATH = "deploymentrepo";
    private static final String YAML_FILE_NAME = "myapp-deployment.yaml";

    public String handleDeployment(DeployRequest request) {
        try {
            // Step 1: Generate YAML
            String yamlContent = generateHelmYaml(request);

            // Step 2: Write YAML to a file
            Files.write(Paths.get(YAML_FILE_NAME), yamlContent.getBytes());

            // Step 3: Ensure deploymentrepo exists
            Path repoPath = Paths.get(REPO_PATH);
            if (!Files.exists(repoPath)) {
                Files.createDirectories(repoPath);
            }

            // Step 4: Move YAML file into deploymentrepo/
            Path targetPath = repoPath.resolve(YAML_FILE_NAME);
            Files.move(Paths.get(YAML_FILE_NAME), targetPath, StandardCopyOption.REPLACE_EXISTING);

            // Step 5: Commit and push to GitLab
            runGitCommands();

            return "✅ YAML pushed & pipeline triggered!";
        } catch (Exception e) {
            e.printStackTrace();
            return "❌ Deployment failed: " + e.getMessage();
        }
    }

    private String generateHelmYaml(DeployRequest request) {
        return String.format("""
                apiVersion: apps/v1
                kind: Deployment
                metadata:
                  name: %s
                spec:
                  replicas: 1
                  selector:
                    matchLabels:
                      app: %s
                  template:
                    metadata:
                      labels:
                        app: %s
                    spec:
                      containers:
                        - name: %s
                          image: %s
                          ports:
                            - containerPort: 80
                """, request.getAppName(), request.getAppName(), request.getAppName(),
                request.getAppName(), request.getDockerImage());
    }

    private void runGitCommands() throws IOException, InterruptedException {
        runCommand(List.of("git", "add", "."), REPO_PATH);
        runCommand(List.of("git", "commit", "-m", "Auto-push deployment YAML", "--allow-empty"), REPO_PATH);

        runCommand(List.of("git", "push", "origin", "main"), REPO_PATH);
    }

    private void runCommand(List<String> command, String directory) throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.directory(new File(directory));
        builder.inheritIO(); // Optional: show output in console
        Process process = builder.start();
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Command failed: " + String.join(" ", command));
        }
    }
}
