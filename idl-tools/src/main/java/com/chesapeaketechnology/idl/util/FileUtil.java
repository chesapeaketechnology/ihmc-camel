package com.chesapeaketechnology.idl.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

/**
 * NIO File utilities.
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
}
