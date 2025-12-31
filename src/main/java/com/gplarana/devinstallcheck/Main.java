package com.gplarana.devinstallcheck;

import picocli.CommandLine;

public class Main {
    public static void main(String[] args) {
        CommandLine cli = new CommandLine(new RootCommand());
        cli.addSubcommand("scan-projects", new ScanProjectsCommand());
        cli.addSubcommand("env-check", new EnvCheckCommand());
        cli.addSubcommand("clean-node-modules", new CleanNodeModules());
        cli.addSubcommand("check-installation", new DevInstallCheck());
        cli.execute("env-check");
    }
}
