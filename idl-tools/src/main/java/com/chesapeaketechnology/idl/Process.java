package com.chesapeaketechnology.idl;

import com.chesapeaketechnology.idl.asm.MethodGenerator;
import com.chesapeaketechnology.idl.util.FileUtil;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Task to insert additional logic into compiled classes.
 *
 * @author Copyright &#169; 2020 Chesapeake Technology International Corp.
 */
@Command(name = "process", mixinStandardHelpOptions = true,
        description = "Post process compiled classes, adding additional functionality.")
public class Process implements Callable<Collection<Path>>
{
    private static final Logger logger = LoggerFactory.getLogger(Process.class);
    private boolean isDir;
    @Parameters(index = "0", description = "The jar or directory containing generated classes.", defaultValue = "generated-bin")
    private File inputFile;
    @Option(names = {"-hc", "--hashcode"}, description = "Add hashCode() method")
    private boolean hash;
    @Option(names = {"-c", "--copy"}, description = "Add <T> copy() method")
    private boolean copy;

    @Override
    public Collection<Path> call() throws Exception
    {
        isDir = inputFile.isDirectory();
        List<Path> paths = isDir ?
                FileUtil.getPathsWithExtension(inputFile.toPath(), "class") :
                Collections.singletonList(inputFile.toPath());
        // Check if no options were specified
        if (!hash && !copy)
        {
            logger.info("Done!\n - No options specified");
            return paths;
        }
        // Update each class
        if (isDir)
        {
            // Iterate over directory entries
            for (Path classPath : paths)
            {
                byte[] code = Files.readAllBytes(classPath);
                code = updateClass(code);
                Files.write(classPath, code);
            }
            logger.info("Done!\n - Processed {} files", paths.size());
        } else if (inputFile.getName().endsWith(".jar"))
        {
            // Read the jar
            Map<String, byte[]> map = FileUtil.readFromJar(inputFile);
            // Update the map
            new HashMap<>(map).entrySet().stream()
                    .filter(e -> e.getKey().endsWith(".class"))
                    .forEach(e -> map.put(e.getKey(), updateClass(e.getValue())));
            // Write back
            FileUtil.writeMapToJar(inputFile, map);
            logger.info("Done!\n - Processed {} files", paths.size());
        } else
        {
            logger.error("Did not specify a directory or jar to process");
        }
        return paths;
    }

    /**
     * Apply processing to the given class bytecode.
     *
     * @param code Class to modify.
     * @return Updated class bytes.
     */
    private byte[] updateClass(byte[] code)
    {
        // Read class bytecode.
        ClassReader reader = new ClassReader(code);
        // Create a generator/writer pair to insert helpful methods like hashCode() & copy()
        // and generate the updated bytecode in one pass.
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        reader.accept(new MethodGenerator(writer, hash, copy), ClassReader.SKIP_FRAMES);
        return writer.toByteArray();
    }
}
