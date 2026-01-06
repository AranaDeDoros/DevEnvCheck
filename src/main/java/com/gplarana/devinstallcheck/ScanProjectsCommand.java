package com.gplarana.devinstallcheck;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.io.IOException;
import java.nio.file.*;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

@Command(
        name = "scan-projects",
        description = "Scan folders and detect project types"
)
public class ScanProjectsCommand implements Runnable {

    enum ProjectType {
        NODE,
        JAVA,
        DOTNET,
        PYTHON
    }

    private static final Map<String, ProjectType> FILE_RULES = new HashMap<>();

    static {
        FILE_RULES.put("package.json", ProjectType.NODE);
        FILE_RULES.put("pom.xml", ProjectType.JAVA);
        FILE_RULES.put("build.gradle", ProjectType.JAVA);
        FILE_RULES.put("pyproject.toml", ProjectType.PYTHON);
        FILE_RULES.put("requirements.txt", ProjectType.PYTHON);
    }

    @Parameters(
            index = "0",
            description = "Root folder",
            paramLabel = "<root>",
            arity = "1"
    )
    private Path root;

    @Override
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

        Map<ProjectType, Integer> counts = new EnumMap<>(ProjectType.class);
        for (ProjectType type : ProjectType.values()) {
            counts.put(type, 0);
        }

        try {
            Files.walk(root)
                    .filter(Files::isRegularFile)
                    .forEach(p -> {
                        String file = p.getFileName().toString();
                        ProjectType type = FILE_RULES.get(file);
//                        if (file.equals("package.json")) {
//                            counts.merge(ProjectType.NODE, 1, Integer::sum);
//                        } else if (file.equals("pom.xml") || file.equals("build.gradle")) {
//                            counts.merge(ProjectType.JAVA, 1, Integer::sum);
//                        } else if (file.endsWith(".sln")) {
//                            counts.merge(ProjectType.DOTNET, 1, Integer::sum);
//                        } else if (file.equals("pyproject.toml") || file.equals("requirements.txt")) {
//                            counts.merge(ProjectType.PYTHON, 1, Integer::sum);
//                        }
                        if (type != null) {
                            counts.put(type, counts.get(type) + 1);
                        } else if (file.endsWith(".sln")) {
                            counts.put(
                                    ProjectType.DOTNET,
                                    counts.get(ProjectType.DOTNET) + 1
                            );
                        }
                    });
        } catch (IOException e) {
            System.err.println("Scan failed: " + root);
            e.printStackTrace();
            return;
        }

        System.out.println("Node:   " + counts.get(ProjectType.NODE));
        System.out.println("Java:   " + counts.get(ProjectType.JAVA));
        System.out.println(".NET:   " + counts.get(ProjectType.DOTNET));
        System.out.println("Python: " + counts.get(ProjectType.PYTHON));
    }
}
