package com.chesapeaketechnology.idl;

import com.chesapeaketechnology.idl.asm.MethodGenerator;
import com.chesapeaketechnology.idl.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Task to insert additional logic into idl source files.
 *
 * @author Copyright &#169; 2020 Chesapeake Technology International Corp.
 */
@Command(name = "process", mixinStandardHelpOptions = true,
        description = "Post process idl type sources, adding additional functionality.")
public class Process implements Callable<Collection<Path>>
{
    private static final Logger logger = LoggerFactory.getLogger(Process.class);
    @Parameters(index = "0", description = "The directory containing generated sources.", defaultValue = "generated-src")
    private File inputDir;
    @Option(names = {"-hc", "--hashcode"}, description = "Add hashCode() method")
    private boolean hash;
    @Option(names = {"-cp", "--copy"}, description = "Add <T> copy() method")
    private boolean copy;
    @Option(names = {"-ct", "--constructor"}, description = "Add a constructor that populates all fields")
    private boolean constructor;

    @Override
    public Collection<Path> call() throws Exception
    {
        List<Path> paths = FileUtil.getPathsWithExtension(inputDir.toPath(), "java");
        // Check if no options were specified
        if (!hash && !copy && !constructor)
        {
            logger.info("Done!\n - No options specified");
            return paths;
        }
        // Update each type
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
        return new MethodGenerator(code, hash, copy, constructor).apply();
    }
}
