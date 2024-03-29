package com.chesapeaketechnology.idl;

import com.chesapeaketechnology.idl.patch.CompatibilityProcessor;
import com.chesapeaketechnology.idl.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Task to insert additional logic into idl source files for compatibility purposes.
 *
 * @author Copyright &#169; 2020 Chesapeake Technology International Corp.
 */
@Command(name = "compat", mixinStandardHelpOptions = true,
        description = "Post process idl type sources, adding compatibility functionality.")
public class Compat implements Callable<Collection<Path>>
{
    private static final Logger logger = LoggerFactory.getLogger(Compat.class);
    @Parameters(index = "0", description = "Add the specified compatibility to the classes.")
    private CompatibilityType type;
    @Parameters(index = "1", description = "The directory containing generated sources.", defaultValue = "generated-src")
    private File inputDir;

    @Override
    public Collection<Path> call() throws Exception
    {
        List<Path> paths = FileUtil.getPathsWithExtension(inputDir.toPath(), "java");
        // Add compatibility functionality to the source
        for (Path classPath : paths)
        {
            String code = new String(Files.readAllBytes(classPath));
            code = updateClass(code);
            Files.write(classPath, code.getBytes(StandardCharsets.UTF_8));
        }
        logger.info("Done!\n - Processed {} files", paths.size());
        return paths;
    }

    /**
     * Apply processing to the given class bytecode.
     *
     * @param code Class to modify.
     * @return Updated class bytes.
     */
    private String updateClass(String code)
    {
        return new CompatibilityProcessor(code, type).apply();
    }
}
