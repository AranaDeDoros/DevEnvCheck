package com.gplarana.devinstallcheck;

import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Command(name = "check-installation", mixinStandardHelpOptions = true)
 class DevInstallCheck implements Runnable {

    @CommandLine.Option(names = "--export", description = "Export output to txt file")
    boolean export;

    @CommandLine.Option(names = "--config", description = "Optional JSON config file")
    Path config;


    public void run() {
        List<SoftwareDefinition> defs =
                config != null ? loadSoftware(config) : defaultSoftware();

        List<SoftwareCheck> results = new ArrayList<SoftwareCheck>();

        for (SoftwareDefinition d : defs) {
            if ("Visual Studio".equals(d.name)) {
                results.add(checkVisualStudio());
            } else if ("SSMS".equals(d.name)) {
                results.add(checkSSMS());
            } else {
                results.add(checkCmd(d.name, d.exe, d.versionCmd));
            }
        }

        String output = formatTable(results);
        System.out.println(output);

        if (export) {
            writeFile("devinstallcheck.txt", output);
        }
    }

     List<SoftwareDefinition> defaultSoftware() {
        List<SoftwareDefinition> list = new ArrayList<>();

        list.add(def("node", "node", "node --version"));
        list.add(def("npm", "npm", "npm --version"));
        list.add(def("python", "python", "python --version"));
        list.add(def("pip", "pip", "pip --version"));
        list.add(def("java", "java", "java -version"));
        list.add(def("vscode", "code", "code --version"));
        list.add(def(".NET SDK", "dotnet", "dotnet --list-sdks"));
        list.add(def("Visual Studio", null, null));
         list.add(def("SSMS", null, null));

        return list;
    }

     SoftwareDefinition def(String name, String exe, String cmd) {
        SoftwareDefinition d = new SoftwareDefinition();
        d.name = name;
        d.exe = exe;
        d.versionCmd = cmd;
        return d;
    }

     SoftwareCheck checkCmd(String name, String exe, String versionCmd) {
        SoftwareCheck sc = new SoftwareCheck();
        sc.name = name;

        String path = run("where " + exe);
        sc.found = path != null && !path.trim().isEmpty();
        sc.path = sc.found ? path : "-";

        if (sc.found && versionCmd != null) {
            String v = run(versionCmd);
            sc.version = v != null ? v : "-";
        } else {
            sc.version = "-";
        }
        return sc;
    }

     SoftwareCheck checkVisualStudio() {
        SoftwareCheck sc = new SoftwareCheck();
        sc.name = "Visual Studio";

        String pf = System.getenv("ProgramFiles(x86)");
        if (pf == null) {
            sc.found = false;
            sc.path = "-";
            sc.version = "-";
            return sc;
        }

        Path vswhere = Paths.get(
                pf, "Microsoft Visual Studio", "Installer", "vswhere.exe");

        if (!Files.exists(vswhere)) {
            sc.found = false;
            sc.path = "-";
            sc.version = "-";
            return sc;
        }

        String path = run("\"" + vswhere + "\" -latest -products * -property installationPath");
        sc.found = path != null && !path.trim().isEmpty();
        sc.path = sc.found ? path : "-";
        sc.version = sc.found ? "Installed" : "-";

        return sc;
    }

     List<SoftwareDefinition> loadSoftware(Path file) {
        BufferedReader reader = null;
        try {
            reader = Files.newBufferedReader(file);
            StringBuilder sb = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            String json = sb.toString();
            List<SoftwareDefinition> list = new ArrayList<>();

            for (String block : json.split("\\{")) {
                if (!block.contains("name")) continue;

                SoftwareDefinition d = new SoftwareDefinition();
                d.name = extract(block, "name");
                d.exe = extract(block, "exe");
                d.versionCmd = extract(block, "version");

                list.add(d);
            }
            return list;

        } catch (IOException e) {
            throw new RuntimeException("Failed to load config file", e);
        } finally {
            if (reader != null) {
                try { reader.close(); } catch (IOException ignored) {}
            }
        }
    }

     String extract(String block, String key) {
        int i = block.indexOf("\"" + key + "\"");
        if (i == -1) return null;

        int q1 = block.indexOf("\"", block.indexOf(":", i)) + 1;
        int q2 = block.indexOf("\"", q1);
        return block.substring(q1, q2);
    }

     String run(String cmd) {
        try {
            Process p = new ProcessBuilder("cmd", "/c", cmd)
                    .redirectErrorStream(true)
                    .start();
            BufferedReader r =
                    new BufferedReader(new InputStreamReader(p.getInputStream()));
            return r.readLine();
        } catch (Exception e) {
            return null;
        }
    }

     void writeFile(String name, String content) {
        try {
            FileWriter fw = new FileWriter(name);
            fw.write(content);
            fw.close();
        } catch (IOException ignored) {}
    }

     String formatTable(List<SoftwareCheck> list) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format(
                "%-15s | %-35s | %-12s | %s%n",
                "Software", "Path", "Version", "Found"));

        sb.append(repeat("-", 75)).append("\n");

        for (SoftwareCheck s : list) {
            sb.append(String.format(
                    "%-15s | %-35s | %-12s | %s%n",
                    s.name,
                    truncate(s.path, 35),
                    truncate(s.version, 12),
                    s.found ? "[x]" : "[ ]"
            ));
        }
        return sb.toString();
    }

     String repeat(String s, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(s);
        }
        return sb.toString();
    }

     String truncate(String s, int len) {
        if (s == null) return "-";
        return s.length() <= len ? s : s.substring(0, len - 3) + "...";
    }

    SoftwareCheck checkSSMS() {
        SoftwareCheck sc = new SoftwareCheck();
        sc.name = "SSMS";

        String pf = System.getenv("ProgramFiles(x86)");
        if (pf == null) {
            sc.found = false;
            sc.path = "-";
            sc.version = "-";
            return sc;
        }

        String[] versions = { "19", "18", "17" };

        for (String v : versions) {
            Path exe = Paths.get(
                    pf,
                    "Microsoft SQL Server Management Studio " + v,
                    "Common7", "IDE", "Ssms.exe"
            );

            if (Files.exists(exe)) {
                sc.found = true;
                sc.path = exe.toString();
                sc.version = getFileVersion(exe);
                return sc;
            }
        }

        sc.found = false;
        sc.path = "-";
        sc.version = "-";
        return sc;
    }
    String getFileVersion(Path exe) {
        String cmd =
                "wmic datafile where name=\"" +
                        exe.toString().replace("\\", "\\\\") +
                        "\" get Version";

        String v = run(cmd);
        return (v == null || v.trim().isEmpty() || v.contains("Version"))
                ? "Installed"
                : v.trim();
    }

}
