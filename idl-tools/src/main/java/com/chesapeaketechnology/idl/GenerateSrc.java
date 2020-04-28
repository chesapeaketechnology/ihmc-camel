package com.chesapeaketechnology.idl;

import com.chesapeaketechnology.idl.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import us.ihmc.commons.nio.FileTools;
import us.ihmc.idl.generator.IDLGenerator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.Callable;

/**
 * Task to generate source files from IDL. Task yields the paths of the generated files.
 *
 * @author Copyright &#169; 2020 Chesapeake Technology International Corp.
 */
@Command(name = "generate", mixinStandardHelpOptions = true,
        description = "Generate Java source files from IDL model files.")
public class GenerateSrc implements Callable<Collection<Path>>
{
    private static final Logger logger = LoggerFactory.getLogger(GenerateSrc.class);
    @Parameters(index = "0", description = "The directory containing IDL files.", defaultValue = "idl")
    private File inputDirectory;
    @Parameters(index = "1", description = "The directory to place generated classes into.", defaultValue = "generated-src")
    private File outputDirectory;
    @Parameters(index = "2", description = "The package name for generated classes.", defaultValue = "com.example.idl")
    private String generatedPackage;

    @Override
    public Collection<Path> call() throws Exception
    {
        // Clear output
        clean();
        // Generate content in output
        generate();
        // Return collection of paths to generated files
        Collection<Path> javaPaths = FileUtil.getPathsWithExtension(outputDirectory.toPath(), "java");
        logger.info("Done! Generated {} source files", javaPaths.size());
        return javaPaths;
    }

    /**
     * Generate source files in the output directory.
     *
     * @throws IOException When the output directory cannot be written to, or when the input directory cannot be fetched.
     */
    private void generate() throws IOException
    {
        Collection<Path> idlPaths = FileUtil.getPathsWithExtension(inputDirectory.toPath(), "idl");
        logger.info("Generating IDL source mappings from directory: {}\n - Found {} files", inputDirectory.getPath(), idlPaths.size());
        for (Path idl : idlPaths)
        {
            logger.info("Generating for '{}'", idl.toString());
            IDLGenerator.execute(idl.toFile(),
                    generatedPackage,
                    outputDirectory,
                    Collections.singletonList(inputDirectory));
        }
    }

    /**
     * Clear the output directory.
     */
    private void clean()
    {
        logger.info("Clearing output directory");
        FileTools.deleteQuietly(outputDirectory.toPath());
        outputDirectory.mkdirs();
    }
}
