package com.chesapeaketechnology.idl;

import com.chesapeaketechnology.idl.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Option;
import us.ihmc.commons.nio.FileTools;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.stream.Collectors;

/**
 * Task to generate source files from IDL. Task yields the paths of the generated files.
 *
 * @author Copyright &#169; 2020 Chesapeake Technology International Corp.
 */
@Command(name = "compile", mixinStandardHelpOptions = true,
        description = "Compile a directory's sources.")
public class CompileSrc implements Callable<Collection<Path>>
{
    private static final Logger logger = LoggerFactory.getLogger(CompileSrc.class);
    @Parameters(index = "0", description = "The directory containing source files.", defaultValue = "generated-src")
    private File inputDirectory;
    @Parameters(index = "1", description = "The directory to place generated classes into.", defaultValue = "generated-bin")
    private File outputDirectory;
    @Option(names = {"-j", "--jar"}, description = "Package classes into a jar")
    private boolean jar;

    @Override
    public Collection<Path> call() throws Exception
    {
        // Ensure output directory exists and it is clean
        clean();
        // Compile files in the input directory, putting output in the output directory.
        if (compile())
        {
            moveToOutput();
        }
        // Generate jar
        String jarMsg = "";
        if (jar)
        {
            packageOutput();
            jarMsg = "\n - Packed into jar";
        }
        // Return collection of paths to generated files
        Collection<Path> classPaths = FileUtil.getPathsWithExtension(outputDirectory.toPath(), "class");
        logger.info("Done!\n - Generated {} class files" + jarMsg, classPaths.size());
        return classPaths;
    }

    /**
     * Package output into a jar.
     *
     * @throws IOException When the output directory cannot be parsed, or when the jar cannot be written to.
     */
    private void packageOutput() throws IOException
    {
        // Log destination jar
        File jarFile = new File(outputDirectory, "generated.jar");
        logger.info("Packaging classes into '{}'", jarFile.getPath());
        // Convert output directory to map.
        Map<String, byte[]> classes = Files.walk(outputDirectory.toPath())
                .filter(path -> path.toString().endsWith(".class"))
                .collect(Collectors.toMap(
                        p -> outputDirectory.toPath().relativize(p).toString().replace(File.separator, "/"),
                        CompileSrc::readClass));
        // Save map to jar
        writeToJar(jarFile, classes);
    }

    /**
     * Move class files to the output directory.
     *
     * @throws IOException When the input directory cannot be walked, or when the move operation fails.
     */
    private void moveToOutput() throws IOException
    {
        logger.info("Moving compiled classes to output directory...");
        // Collect classes
        List<Path> classes = FileUtil.getPathsWithExtension(inputDirectory.toPath(), "class");
        // Move to output directory
        for (Path path : classes)
        {
            String classNamePath = inputDirectory.toPath().relativize(path).toString().replace(File.separator, "/");
            Path destination = outputDirectory.toPath().resolve(classNamePath);
            // Ensure the parent directories exist so the move operation doesn't fail
            destination.getParent().toFile().mkdirs();
            // Move the class to the output directory, with the same package structure
            Files.move(path, destination);
        }
    }

    /**
     * Compile sources in the input directory.
     *
     * @return {@code true} when compilation succeeded.
     * @throws IOException When fetching files for compiling failed, or when writing compiled files failed.
     */
    private boolean compile() throws IOException
    {
        // Check if compiler is available
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null)
        {
            logger.error("Could not load system-compiler. Please execute with a JDK.");
            return false;
        }
        // Setup compiler and run compile task containing all discovered sources in the input directory
        boolean compiled = false;
        try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null))
        {
            // Collect source files in the input directory
            List<File> files = FileUtil.getFilesWithExtension(inputDirectory.toPath(), "java");
            Iterable<? extends JavaFileObject> compilationUnits =
                    fileManager.getJavaFileObjectsFromFiles(files);
            logger.info("Starting compile task with {} source files", files.size());
            // Create compile task and execute
            DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
            JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, null, null, compilationUnits);
            if (task.call())
            {
                logger.info("Compilation completed successfully.");
                compiled = true;
            } else
            {
                // Collect all reported errors
                StringBuilder sbDiagnostic = new StringBuilder();
                diagnostics.getDiagnostics()
                        .forEach(diagnostic -> sbDiagnostic.append("\n\n").append(diagnostic.toString()));
                // Log errors
                logger.error("Failed to compile sources, {} diagnostics reported: {}",
                        diagnostics.getDiagnostics().size(),
                        sbDiagnostic.toString());
            }
        }
        return compiled;
    }

    /**
     * Clear the output directory.
     *
     * @throws IOException When the directory cannot be cleared.
     */
    private void clean() throws IOException
    {
        logger.info("Clearing output directory");
        FileTools.deleteQuietly(outputDirectory.toPath());
        outputDirectory.mkdirs();
    }

    /**
     * Writes a map to an archive.
     *
     * @param output  File location of jar.
     * @param content Contents to write to location.
     * @throws IOException When the jar file cannot be written to.
     */
    private static void writeToJar(File output, Map<String, byte[]> content) throws IOException
    {
        try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(output)))
        {
            Set<String> dirsVisited = new HashSet<>();
            // We wrap the passed map in a "TreeMap" because it's a sorted-map type.
            // This means the iteration order is filtered by the key.
            // This is just so the archive is structured properly where entries aren't in random order.
            for (Map.Entry<String, byte[]> entry : new TreeMap<>(content).entrySet())
            {
                String key = entry.getKey();
                byte[] out = entry.getValue();
                // Write directories for upcoming entries if necessary.
                if (key.contains("/"))
                {
                    // Record directories
                    String parent = key;
                    List<String> toAdd = new ArrayList<>();
                    do
                    {
                        parent = parent.substring(0, parent.lastIndexOf('/'));
                        if (dirsVisited.add(parent))
                        {
                            toAdd.add(0, parent + '/');
                        } else
                        {
                            break;
                        }
                    } while (parent.contains("/"));
                    // Put directories in order of depth
                    for (String dir : toAdd)
                    {
                        jos.putNextEntry(new JarEntry(dir));
                        jos.closeEntry();
                    }
                }
                // Write entry content
                jos.putNextEntry(new JarEntry(key));
                jos.write(out);
                jos.closeEntry();
            }
        }
    }

    /**
     * Silent version of {@link Files#readAllBytes(Path)}.
     *
     * @param path Path to read.
     * @return Raw contents of file at path.
     */
    private static byte[] readClass(Path path)
    {
        try
        {
            return Files.readAllBytes(path);
        } catch (IOException ex)
        {
            throw new IllegalStateException("Failed to read class's contents", ex);
        }
    }
}
