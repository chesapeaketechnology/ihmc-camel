package com.chesapeaketechnology.idl;

import com.chesapeaketechnology.idl.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Option;
import us.ihmc.commons.nio.FileTools;
import us.ihmc.idl.generator.IDLGenerator;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
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
    @Option(names = {"-p", "--prefix"}, description = "The package prefix for generated classes.")
    private String generatedPackage;
    @Option(names = {"-r", "--regex"}, description = "A map of matching regular expressions to the replacement.")
    private Map<String, String> regexReplacementMap;
    @Option(names = {"--pragma"}, description = "Remove #pramga statements and any immediately following statement.")
    private boolean removePragma;

    @Override
    public Collection<Path> call() throws Exception
    {
        // Preconfigure arguments if necessary
        setup();
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
            if (requiresPatching())
            {
                // Create a temporary idl file path to write patched idl to.
                Path idlPatched = idl.getParent()
                        .resolve(idl.getFileName().toString().replace(".idl", "-patch.idl"));
                try
                {
                    // Generate updated idl source with regular expressions applied
                    generatePatchedIdl(idl, idlPatched);
                    // Compile patched file
                    IDLGenerator.execute(idlPatched.toFile(),
                            generatedPackage,
                            outputDirectory,
                            Collections.singletonList(inputDirectory));
                } finally
                {
                    // Remove temporary patched file
                    if (!idl.equals(idlPatched))
                    {
                        FileTools.deleteQuietly(idlPatched);
                    }
                }
            } else
            {
                IDLGenerator.execute(idl.toFile(),
                        generatedPackage,
                        outputDirectory,
                        Collections.singletonList(inputDirectory));
            }
        }
    }

    /**
     * Generate a patched idl file.
     *
     * @param source      Path to original idl file.
     * @param destination Path to patched idl file.
     * @throws IOException When the source file cannot be read, or the destination cannot be written to.
     */
    private void generatePatchedIdl(Path source, Path destination) throws IOException
    {
        // Get original source of the idl
        String[] idlSource = {new String(Files.readAllBytes(source))};
        String original = idlSource[0];
        // Apply all patches
        regexReplacementMap.forEach((patternStr, replacement) -> {
            idlSource[0] = idlSource[0].replaceAll(patternStr, replacement);
        });
        // Write patched source to destination idl
        String modified = idlSource[0];
        logger.debug(" - Applied changes, size diff {}", (original.length() - modified.length()));
        byte[] patchedBytes = modified.getBytes(StandardCharsets.UTF_8);
        Files.write(destination, patchedBytes, StandardOpenOption.CREATE);
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

    /**
     * @return {@code true} when patching the input idl files is required.
     */
    private boolean requiresPatching()
    {
        return regexReplacementMap != null && !regexReplacementMap.isEmpty();
    }

    /**
     * Configure parameters if necessary.
     */
    private void setup()
    {
        // Initialize map
        if (regexReplacementMap == null)
        {
            regexReplacementMap = new HashMap<>();
        }
        // Add replacement pattern
        if (removePragma)
        {
            regexReplacementMap.put("#pragma(?:\\s+.+\\s+const)?\\s.+\\s+", "");
        }
    }
}
