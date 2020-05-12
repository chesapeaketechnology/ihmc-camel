package com.chesapeaketechnology.idl;

import com.chesapeaketechnology.idl.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import us.ihmc.commons.nio.FileTools;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

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
        // Return collection of paths to generated files
        Collection<Path> classPaths = FileUtil.getPathsWithExtension(outputDirectory.toPath(), "class");
        logger.info("Done!\n - Generated {} class files", classPaths.size());
        return classPaths;
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
            // Arguments - target 8
            List<String> args = new ArrayList<>();
            args.addAll(Arrays.asList("-source", "1.8"));
            args.addAll(Arrays.asList("-target", "1.8"));
            args.addAll(Arrays.asList("-cp", System.getProperty("java.class.path")));
            // Create compile task and execute
            DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
            JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, args, null, compilationUnits);
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
}
