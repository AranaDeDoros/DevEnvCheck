package com.gplarana.devinstallcheck;

import picocli.CommandLine;

public class Main {
    public static void main(String[] args) {
        CommandLine cli = new CommandLine(new RootCommand());
        cli.addSubcommand("scan-projects", new ScanProjectsCommand());//ok
        cli.addSubcommand("env-check", new EnvCheckCommand());//ok
        cli.addSubcommand("clean-node-modules", new CleanNodeModules());
        cli.addSubcommand("check-installation", new DevInstallCheck()); //ok
        cli.execute("clean-node-modules","C:\\Users\\dontb\\Desktop\\repos\\tetas","--dry-run");
    }
}
