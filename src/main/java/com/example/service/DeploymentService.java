package com.example.deploymentapi.service;

import com.example.deploymentapi.dto.DeployRequest;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

@Service
public class DeploymentService {

    public void handleDeployment(DeployRequest request) {
        System.out.println("Deploying: " + request.getAppName());
        System.out.println("Version: " + request.getVersion());
        System.out.println("Docker Image: " + request.getDockerImage());

        try {
            // Step 1: Generate Helm YAML
            String yamlContent = generateHelmYaml(request);
            File file = new File("deployment-" + request.getAppName() + ".yaml");
            FileWriter writer = new FileWriter(file);
            writer.write(yamlContent);
            writer.close();

            // Step 2: Push to GitLab
            pushToGitLab(file);

            // Step 3: Trigger GitLab CI
            triggerGitLabPipeline();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String generateHelmYaml(DeployRequest request) {
        return """
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
                """.formatted(
                request.getAppName(),
                request.getAppName(),
                request.getAppName(),
                request.getAppName(),
                request.getDockerImage()
        );
    }

    private void pushToGitLab(File file) throws IOException, InterruptedException {
        String repoUrl = "https://gitlab.com/your-username/your-repo.git";
        String branch = "main";

        ProcessBuilder builder = new ProcessBuilder(
                "bash", "-c",
                String.join(" && ",
                        "git config --global user.email \"you@example.com\"",
                        "git config --global user.name \"Your Name\"",
                        "rm -rf temp-git",
                        "git clone " + repoUrl + " temp-git",
                        "cp " + file.getAbsolutePath() + " temp-git/",
                        "cd temp-git",
                        "git add .",
                        "git commit -m \"Add deployment file\"",
                        "git push origin " + branch
                )
        );

        builder.inheritIO();
        Process process = builder.start();
        process.waitFor();
    }

    private void triggerGitLabPipeline() throws IOException {
        String projectId = "your_project_id";
        String triggerToken = "your_gitlab_trigger_token";
        String apiUrl = "https://gitlab.com/api/v4/projects/" + projectId + "/trigger/pipeline";

        ProcessBuilder builder = new ProcessBuilder(
                "curl",
                "--request", "POST",
                "--form", "token=" + triggerToken,
                "--form", "ref=main",
                apiUrl
        );

        builder.inheritIO();
        builder.start();
    }
}
