package com.chesapeaketechnology.idl;

import com.chesapeaketechnology.idl.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Task to pack classes into a jar.
 *
 * @author Copyright &#169; 2020 Chesapeake Technology International Corp.
 */
@Command(name = "pack", mixinStandardHelpOptions = true,
        description = "Package a directory into a jar file.")
public class Pack implements Callable<Collection<Path>>
{
    private static final Logger logger = LoggerFactory.getLogger(Pack.class);
    @Parameters(index = "0", description = "The directory containing generated classes. Should be the root class package",
            defaultValue = "generated-bin")
    private File inputDirectory;
    @Parameters(index = "1", description = "The directory containing generated classes.",
            defaultValue = "generated.jar")
    private File outputJar;

    @Override
    public Collection<Path> call() throws Exception
    {
        // Create jar
        FileUtil.packageToJar(inputDirectory, outputJar);
        // Collect classes that were put into the jar
        List<Path> paths = FileUtil.getPathsWithExtension(inputDirectory.toPath(), "class");
        logger.info("Done!\n - Packaged {} files", paths.size());
        return paths;
    }
}
