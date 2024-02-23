package com.dms.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

public class DirectoryCleaner {

    private DirectoryCleaner() {
    }

    public static void cleanDirectory(String directory) throws IOException {
        Path directoryPath = Path.of(directory);

        Files.walk(directoryPath)
             .sorted(Comparator.reverseOrder())
             .filter(file -> file.compareTo(directoryPath) != 0)
             .forEach(file -> {
                 try {
                     Files.delete(file);
                 } catch (IOException e) {
                     throw new RuntimeException(e);
                 }
             });
    }

}
