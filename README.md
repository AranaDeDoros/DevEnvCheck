# DevEnvCheck
A lightweight and extendable **Java 8+ CLI tool for auditing and diagnosing Windows developer environments**.

Originally created after a real recovery incident, this tool helps validate that common development tools, SDKs, and environment variables are correctly installed and discoverable. 

> **Windows only** – relies on `cmd`, `where`, `wmic`, and Windows install conventions.

---

## Requirements

- **Windows**
- **Java 8+**
- Maven (for building)

---


## Commands

### check-installation
Detects common development software installations and reports whether they're present.

### Usage
Check for installation of regular developer software on windows.
check-installation #uses default software list
check-installation --export  #export report as .txt file
check-installation --config software.json #load json entries from file

### software.json example
```json
[
  {
    "name": "node",
    "exe": "node",
    "version": "node --version"
  },
  {
    "name": "Visual Studio"
   
  }
]
```
### output
```text
Software        | Path                              | Version        | Found
---------------------------------------------------------------------------
node            | C:\Program Files\nodejs\node.exe  | v20.11.0       | [x]
npm             | C:\Program Files\nodejs\npm.cmd   | 10.2.3         | [x]
python          | -                                 | -              | [ ]
java            | C:\Program Files\Java\jdk...\bin  | 21.0.1         | [x]
vscode          | C:\Users\...\Code.exe             | 1.86.2         | [x]
Visual Studio   | C:\Program Files\Microsoft VS\    | 2022           | [x]
.NET SDK        | -                                 | 8.0.100        | [x]
```
---
### clean-node-modules
Recursively find and delete node_modules folders given a root path.

### usage
```bash
clean-node-modules <path> #asks for confirmation y/N before deleting
clean-node-modules <path> --silent #no confirmation
clean-node-modules <path> --dry-run #lists what would be deleted but doesn't delete
```
---
### scan-projects
Detects Node, Java, .NET projects recursively.

### usage
```bash
scan-projects <path>
```

### output
```text
Node: 12
Java: 5
.NET: 3
Python: 2
```
---
### env-check
Checks common environment variables that often break builds.

### usage
```bash
env-check <path>
```
### output
```text
JAVA_HOME: SET
NODE_ENV: NOT SET
PATH: SET
HTTP_PROXY: NOT SET
HTTPS_PROXY: NOT SET
```
---
## Build

```bash
mvn package
```
---
## Dependencies
- PicoCLI.

