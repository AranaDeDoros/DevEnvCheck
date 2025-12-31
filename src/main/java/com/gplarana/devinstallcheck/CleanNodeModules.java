package com.gplarana.devinstallcheck;

import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

@Command(
    name = "clean-node-modules",
    description = "Recursively find and delete node_modules folders"
)
class CleanNodeModules implements Runnable {

    @CommandLine.Parameters(index = "0", description = "Root folder to scan")
    Path root;

    @CommandLine.Option(names = "--silent", description = "Delete without prompting")
    boolean silent;

    @CommandLine.Option(names = "--dry-run", description = "Only show what would be deleted")
    boolean dryRun;

    public void run() {
        if (!Files.isDirectory(root)) {
            System.err.println("Invalid directory: " + root);
            return;
        }

        try {
            Files.walkFileTree(root, new NodeModulesVisitor());
        } catch (IOException e) {
            System.err.println("Scan failed: " + e.getMessage());
        }
    }

    class NodeModulesVisitor extends SimpleFileVisitor<Path> {
        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                throws IOException {

            if ("node_modules".equals(dir.getFileName().toString())
                    && Files.exists(dir.getParent().resolve("package.json"))) {

                handleNodeModules(dir);
                return FileVisitResult.SKIP_SUBTREE;
            }
            return FileVisitResult.CONTINUE;
        }
    }

    void handleNodeModules(Path nodeModules) throws IOException {
        System.out.println("Found: " + nodeModules);

        if (dryRun) return;

        if (!silent && !confirm(nodeModules)) {
            System.out.println("Skipped");
            return;
        }

        deleteRecursively(nodeModules);
        System.out.println("Deleted");
    }

    boolean confirm(Path dir) throws IOException {
        System.out.print("Delete this node_modules? [y/N]: ");
        BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
        String line = r.readLine();
        return "y".equalsIgnoreCase(line);
    }

    void deleteRecursively(Path path) throws IOException {
        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                    throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc)
                    throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
