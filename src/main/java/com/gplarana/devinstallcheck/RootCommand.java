package com.gplarana.devinstallcheck;

import picocli.CommandLine.Command;

@Command(name = "devinstallcheck", description = "Developer environment utility CLI")
public class RootCommand implements Runnable {
    public void run() {
        System.out.println("Use a subcommand. --help for options.");
    }
}
