package com.gplarana.devinstallcheck;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import java.nio.file.*;
import java.io.IOException;

@Command(name = "scan-projects", description = "Scan folders and detect project types")
public class ScanProjectsCommand implements Runnable {

    @Parameters(  index = "0",
            description = "Root folder",
            paramLabel = "<root>",
            arity = "1")
    private Path root;

    public void run() {
        if (root == null) {
            System.err.println("Root path not provided");
            return;
        }

        if (!Files.exists(root)) {
            System.err.println("Path does not exist: " + root.toAbsolutePath());
            return;
        }

        if (!Files.isDirectory(root)) {
            System.err.println("Path is not a directory: " + root.toAbsolutePath());
            return;
        }

        try {
            Files.walk(root)
                .filter(Files::isRegularFile)
                .forEach(p -> {
                    String f = p.getFileName().toString();
                    if (f.equals("package.json")) System.out.println("Node project: " + p.getParent());
                    if (f.equals("pom.xml")) System.out.println("Maven project: " + p.getParent());
                    if (f.equals("build.gradle")) System.out.println("Gradle project: " + p.getParent());
                    if (f.endsWith(".sln")) System.out.println(".NET project: " + p.getParent());
                });
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Scan failed"+ root);
        }
    }
}
