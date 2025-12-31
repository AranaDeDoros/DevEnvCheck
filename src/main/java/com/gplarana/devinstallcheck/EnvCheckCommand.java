package com.gplarana.devinstallcheck;

import picocli.CommandLine.Command;

@Command(name = "env-check", description = "Check common environment variables")
public class EnvCheckCommand implements Runnable {
    public void run() {
        check("JAVA_HOME");
        check("JAVA_OPTS");
        check("SBT_HOME");
        check("NODE_ENV");
        check("PATH");
        check("HTTP_PROXY");
        check("HTTPS_PROXY");
    }

    private void check(String k) {
        String v = System.getenv(k);
        System.out.println(k + ": " + (v == null ? "NOT SET" : "SET"));
    }
}
