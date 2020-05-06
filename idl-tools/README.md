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

#### Process

**Description**: Post-process classes to add additional useful behavior.

**Usage**: `process [--hashcode] [--copy] <input>`

| Parameter         | Default Value     | Description                                |
| ----------------- | ----------------- | ------------------------------------------ |
| `input`           | `./generated-bin` | Directory or jar containing classes.       |

| Option               | Description                   |
| -------------------- | ----------------------------- |
| `-hc` / `--hashcode` | Add `hashCode()` to classes.  |
| `-c` / `--copy`      | Add `<T> copy()` to classes.  |

## Usage

### Command line

1. Run `gradle fatJar` to generate `build/libs/idl-tools-{version}.jar`
2. Run the commands specified above
    - Example: 
         1. `java -jar build/libs/idl-tools-1.0.0.jar generate`
         2. `java -jar build/libs/idl-tools-1.0.0.jar compile --jar`
         3. `java -jar build/libs/idl-tools-1.0.0.jar process --hashcode --copy`
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

task generateSources(type: JavaExec) {
    main = "com.chesapeaketechnology.idl.Tool"
    classpath = configurations.customClasspath
    args 'generate', 'src/idl', 'build/classes/java/main'
}

task instrument(type: JavaExec) {
    main = "com.chesapeaketechnology.idl.Tool"
    classpath = configurations.customClasspath
    args 'process', '--copy', '--hashcode', 'build/classes/java/main'
}

compileJava.dependsOn([generateSources])
compileJava.doLast {
    tasks.instrument.execute()
}
```
    