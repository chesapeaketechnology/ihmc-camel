# IDL Tools

A collection of tools to automate the process of generating Java classes from IDL.

## Commands

#### Generate

**Description**: Generate Java source files from IDL model files.

**Usage**: `compile [--prefix <value>] [--regex <regex>=<replace>] [--pragma] <inputDirectory> <outputDirectory>`

| Parameter          | Default Value     | Description                                |
| ------------------ | ----------------- | ------------------------------------------ |
| `inputDirectory`   | `./idl`           | Directory containing IDL files.            |
| `outputDirectory`  | `./generated-src` | Directory to place generated classes into. |

| Option             | Default Value | Description                                                          |
| ------------------ |-------------- | -------------------------------------------------------------------- |
| `-p` / `--prefix`  | None          | Package prefix for generated classes.                                |
| `-r` / `--regex`   | None          | Regular expression to match and replacement text pairs.              |
| `--pragma`         | False         | Flag for pre-processing IDL sources. Removes `#pragma` from sources. |

#### Process

**Description**: Transform generated sources to add additional useful behavior.

**Usage**: `process [--hashcode] [--copy] [--constructor] [--packageMapping <com.exmaple=org.example>] <input>`

| Parameter         | Default Value     | Description                   |
| ----------------- | ----------------- | ----------------------------- |
| `input`           | `./generated-src` | Directory containing sources. |

| Option                     | Description                                  |
| -------------------------- | -------------------------------------------- |
| `-hc` / `--hashcode`       | Add `hashCode()` to classes.                 |
| `-cp` / `--copy`           | Add `<T> copy()` to classes.                 |
| `-ct` / `--constructor`    | Add a constructor that populates all fields. |
| `-pm` / `--packageMapping` | Map a package name to another package name.  |

#### Compat

**Description**: Transform generated sources to be compatible with the given DDS implementation.

**Usage**: `compat <type> <input>`

| Parameter | Default Value     | Description                                |
| --------- | ----------------- | ------------------------------------------ |
| `type`    | None              | DDS implementation name. Current support: <ul><li>CAFE - <i>(Requires `process --constructor`)</i></li></ul> |
| `input`   | `./generated-src` | Directory containing source files. |

#### Compile

**Description**: Compile a directory's sources.

**Usage**: `compile <inputDirectory> <outputDirectory>`

| Parameter         | Default Value     | Description                                |
| ----------------- | ----------------- | ------------------------------------------ |
| `inputDirectory`  | `./generated-src` | Directory containing source files.         |
| `outputDirectory` | `./generated-bin` | Directory to place generated classes into. |

#### Pack

**Description**: Package a directory into a jar file.

**Usage**: `pack <inputDirectory> <outputJar>`

| Parameter          | Default Value     | Description                                |
| ------------------ | ----------------- | ------------------------------------------ |
| `inputDirectory`   | `./generated-bin` | Directory containing generated classes.    |
| `outputJar`        | `./generated.jar` | Jar to place generated classes into.       |

## Usage

### Command line

1. Run `gradle fatJar` to generate `build/libs/idl-tools-{version}.jar`
2. Run the commands specified above
    - Example: 
         1. `java -jar build/libs/idl-tools-1.0.0.jar generate`
         2. `java -jar build/libs/idl-tools-1.0.0.jar process --hashcode --copy`
         3. `java -jar build/libs/idl-tools-1.0.0.jar compile --jar`
         4. `java -jar build/libs/idl-tools-1.0.0.jar package`
         
### Gradle task

* For the current project
    1. Run `gradle compileSources`
* For your own project
    1. Add the IHMC repository to your project 
        * `maven { url "https://dl.bintray.com/ihmcrobotics/maven-release/" }`
    2. Add this project as a dependency
        * `implementation("com.chesapeaketechnology:dds:${dds_camel_version}")`
    3. Create a gradle `JavaExec` task that calls `com.chesapeaketechnology.idl.Tool` with the specified arguments for each command you want to run

**Example configuration:**
```groovy
configurations {
    customClasspath
}

dependencies {
    customClasspath implementation("com.chesapeaketechnology:idl:${idl_tools_ver}")
}

// Task to generate Java sources from IDL files
task generateSources(type: JavaExec) {
    main = "com.chesapeaketechnology.idl.Tool"
    classpath = configurations.customClasspath
    args 'generate', '--pragma', IDL_SRC, GEN_SRC
}
// Task to add additional capabilities to the compiled files
task instrumentGenerated(dependsOn: generateSources, type: JavaExec) {
    main = "com.chesapeaketechnology.idl.Tool"
    classpath = configurations.customClasspath
    args 'process', '--copy', '--hashcode', '--constructor', GEN_SRC
}
// Task to add additional capabilities to the compiled files
task instrumentCompatibility(dependsOn: instrumentGenerated, type: JavaExec) {
    main = "com.chesapeaketechnology.idl.Tool"
    classpath = configurations.customClasspath
    args 'compat', DDSI_TYPE_NAME, GEN_SRC
}
// Task to generate Java sources from IDL files
//  - Change dependsOn to 'instrumentGenerated' if no compatibility is needed
task compileGenerated(dependsOn: instrumentCompatibility, type: JavaExec) {
    main = "com.chesapeaketechnology.idl.Tool"
    classpath = configurations.customClasspath
    args 'compile', GEN_SRC, GEN_BIN
}
// Task to add additional capabilities to the compiled files
task packGenerated(dependsOn: compileGenerated, type: JavaExec) {
    main = "com.chesapeaketechnology.idl.Tool"
    classpath = configurations.customClasspath
    args 'pack', GEN_BIN, GEN_JAR
}
```
    