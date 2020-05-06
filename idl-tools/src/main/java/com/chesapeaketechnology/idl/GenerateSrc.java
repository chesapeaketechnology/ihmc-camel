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
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private static final Pattern includePattern = Pattern.compile("#include\\s<([\\w]+)\\.idl>");
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
        Function<Path, Path> pathFunction = in -> in.getParent().resolve(in.getFileName().toString().replace(".idl", "_patch.idl"));
        logger.info("Generating IDL source mappings from directory: {}\n - Found {} files", inputDirectory.getPath(), idlPaths.size());
        try
        {
            generatePatchedIdl(idlPaths, pathFunction);
            for (Path idl : idlPaths)
            {
                logger.info("- Generating for '{}'", idl.toString());
                Path input = requiresPatching() ? pathFunction.apply(idl) : idl;
                IDLGenerator.execute(input.toFile(),
                        generatedPackage,
                        outputDirectory,
                        Collections.singletonList(inputDirectory));
            }
        } finally
        {
            cleanGeneratedPatchedIdl(idlPaths, pathFunction);
        }
    }

    /**
     * @param idlPaths     Collection of input idl paths.
     * @param pathFunction Function to convert to temporary patched idl path.
     * @throws IOException When a source file cannot be read, or a destination cannot be written to.
     */
    private void generatePatchedIdl(Collection<Path> idlPaths, Function<Path, Path> pathFunction) throws IOException
    {
        // Skip if no patching needed
        if (!requiresPatching())
        {
            return;
        }
        // Generate updated idl source with regular expressions applied
        logger.info("- Creating patched idl files");
        for (Path idl : idlPaths)
        {
            // Get original source of the idl
            String[] idlSource = {new String(Files.readAllBytes(idl))};
            String original = idlSource[0];
            // Apply all custom patches
            regexReplacementMap.forEach((patternStr, replacement) -> {
                idlSource[0] = idlSource[0].replaceAll(patternStr, replacement);
            });
            // Apply patch to includes
            Matcher m = includePattern.matcher(idlSource[0]);
            while (m.find())
            {
                idlSource[0] = idlSource[0].replace(m.group(), m.group(1));
            }
            // Write patched source to destination idl
            String modified = idlSource[0];
            logger.debug(" - Applied changes to {}, size diff {}", idl, (original.length() - modified.length()));
            byte[] patchedBytes = modified.getBytes(StandardCharsets.UTF_8);
            Files.write(pathFunction.apply(idl), patchedBytes, StandardOpenOption.CREATE);
        }
    }

    /**
     * Delete all temporary patched idl files.
     *
     * @param idlPaths     Collection of input idl paths.
     * @param pathFunction Function to convert to temporary patched idl path.
     */
    private void cleanGeneratedPatchedIdl(Collection<Path> idlPaths, Function<Path, Path> pathFunction)
    {
        // Skip if no patching needed
        if (!requiresPatching())
        {
            return;
        }
        logger.info("- Cleaning patched idl files");
        for (Path idl : idlPaths)
        {
            // Generate updated idl source with regular expressions applied
            FileTools.deleteQuietly(pathFunction.apply(idl));
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
