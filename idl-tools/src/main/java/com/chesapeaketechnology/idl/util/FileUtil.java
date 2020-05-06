package com.chesapeaketechnology.idl.util;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;

/**
 * NIO File utilities.
 *
 * @author Copyright &#169; 2020 Chesapeake Technology International Corp.
 */
public class FileUtil
{
    /**
     * Collect the paths to files in the given directory that have the given extension.
     *
     * @param directory The root directory to search.
     * @param extension The extension to match.
     * @return All paths to files with the matching extension.
     * @throws IOException When walking the directory fails.
     */
    public static List<Path> getPathsWithExtension(Path directory, String extension) throws IOException
    {
        return Files.walk(directory)
                .filter(path -> path.toString().endsWith("." + extension))
                .collect(Collectors.toList());
    }

    /**
     * Collect the files in the given directory that have the given extension.
     *
     * @param directory The root directory to search.
     * @param extension The extension to match.
     * @return All files with the matching extension.
     * @throws IOException When walking the directory fails.
     */
    public static List<File> getFilesWithExtension(Path directory, String extension) throws IOException
    {
        return Files.walk(directory)
                .filter(path -> path.toString().endsWith("." + extension))
                .map(Path::toFile)
                .collect(Collectors.toList());
    }

    /**
     * Silent version of {@link Files#readAllBytes(Path)}.
     *
     * @param path Path to read.
     * @return Raw contents of file at path.
     */
    public static byte[] readSilent(Path path)
    {
        try
        {
            return Files.readAllBytes(path);
        } catch (IOException ex)
        {
            throw new IllegalStateException("Failed to read class's contents", ex);
        }
    }

    /**
     * Reads entries of a jar archive into a map.
     *
     * @param input Jar file.
     * @return Map of entry names to their raw value.
     * @throws IOException When streaming from the jar fails.
     */
    public static Map<String, byte[]> readFromJar(File input) throws IOException
    {
        Map<String, byte[]> map = new HashMap<>();
        JarFile jar = new JarFile(input);
        try (JarInputStream jis = new JarInputStream(new FileInputStream(input)))
        {
            ZipEntry e;
            while ((e = jis.getNextEntry()) != null)
            {
                map.put(e.getName(), IOUtils.toByteArray(jar.getInputStream(e)));
            }
        }
        return map;
    }

    /**
     * Writes a map to an archive.
     *
     * @param outputJar File location of jar.
     * @param content   Contents to write to location.
     * @throws IOException When the jar file cannot be written to.
     */
    public static void writeMapToJar(File outputJar, Map<String, byte[]> content) throws IOException
    {
        try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(outputJar)))
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
     * Package output into a jar.
     *
     * @param inputDirectory Directory containing jar files.
     * @param outputJar      Jar file to write to.
     * @throws IOException When the input directory cannot be read, or when the jar cannot be written to.
     */
    public static void packageToJar(File inputDirectory, File outputJar) throws IOException
    {
        // Convert directory to map, including only classes
        Map<String, byte[]> classes = Files.walk(inputDirectory.toPath())
                .filter(path -> path.toString().endsWith(".class"))
                .collect(Collectors.toMap(
                        p -> inputDirectory.toPath().relativize(p).toString().replace(File.separator, "/"),
                        FileUtil::readSilent));
        // Save map to jar
        writeMapToJar(outputJar, classes);
    }
}
