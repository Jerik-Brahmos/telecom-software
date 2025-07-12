package com.example.deploymentapi.service;

import com.example.deploymentapi.dto.DeployRequest;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.Arrays;

@Service
public class DeploymentService {

    public void handleDeployment(DeployRequest request) {
        String yamlContent = generateHelmYaml(request);
        String fileName = request.getAppName() + "-deployment.yaml";

        try {
            // Save YAML locally
            FileWriter writer = new FileWriter(fileName);
            writer.write(yamlContent);
            writer.close();
            System.out.println("✅ YAML file generated: " + fileName);

            // Git commands (Windows cmd style)
            String[] commands = {
                    "cmd.exe", "/c",
                    String.join(" && ", Arrays.asList(
                            "rmdir /s /q deploymentrepo", // Delete if exists
                            "git clone https://gitlab.com/prajushar-group/deploymentrepo.git",
                            "move " + fileName + " deploymentrepo\\",
                            "cd deploymentrepo",
                            "git config user.name \"Praju\"",
                            "git config user.email \"prajushar@karunya.edu.in\"",
                            "git add .",
                            "git commit -m \"Add deployment YAML for " + request.getAppName() + "\"",
                            "git push origin main"
                    ))
            };

            ProcessBuilder pb = new ProcessBuilder(commands);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            // Output console logs
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            }

            process.waitFor();
            System.out.println("✅ Git operations completed.");

            // Trigger GitLab CI/CD pipeline
            String triggerCurl = String.format(
                    "curl -X POST https://gitlab.com/api/v4/projects/71560534/trigger/pipeline " +
                            "-F token=glptt-ccf4a6a4e498ec7dba91da716e6dc0ad48b77171 -F ref=main"
            );

            String[] curlCommand = {"cmd.exe", "/c", triggerCurl};
            ProcessBuilder trigger = new ProcessBuilder(curlCommand);
            trigger.redirectErrorStream(true);
            Process triggerProcess = trigger.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(triggerProcess.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            }

            triggerProcess.waitFor();
            System.out.println("✅ GitLab pipeline triggered.");

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
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
                        - containerPort: 8081
                """, request.getAppName(), request.getAppName(), request.getAppName(),
                request.getAppName(), request.getDockerImage());
    }
}
