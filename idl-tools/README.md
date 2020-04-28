# IDL Tools

A collection of tools to automate the process of generating Java classes from IDL.

## Commands

#### Generate

**Description**: Generate Java source files from IDL model files.

**Usage**: `compile <inputDirectory> <outputDirectory> <generatedPackage>`

| Parameter          | Default Value     | Description                                |
| ------------------ | ----------------- | ------------------------------------------ |
| `inputDirectory`   | `./idl`           | Directory containing IDL files.            |
| `outputDirectory`  | `./generated-src` | Directory to place generated classes into. |
| `generatedPackage` | `com.example.idl` | The package name for generated classes.    |

#### Compile

**Description**: Compile a directory's sources.

**Usage**: `compile [--jar] <inputDirectory> <outputDirectory>`

| Parameter         | Default Value     | Description                                |
| ----------------- | ----------------- | ------------------------------------------ |
| `inputDirectory`  | `./generated-src` | Directory containing source files.         |
| `outputDirectory` | `./generated-bin` | Directory to place generated classes into. |

| Option           | Description                 |
| ---------------- | --------------------------- |
| `-j` / `--jar`   | Package classes into a jar. |

## Usage

### Command line

1. Run `gradle fatJar` to generate `build/libs/idl-tools-{version}.jar`
2. Run the commands specified above
    - Example: 
         1. `java -jar build/libs/idl-tools-1.0.0.jar generate`
         2. `java -jar build/libs/idl-tools-1.0.0.jar compile --jar`
         
### Gradle task

1. Run `gradle compileSources`