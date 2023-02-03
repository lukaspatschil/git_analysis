package com.tuwien.gitanalyser;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;

@SpringBootApplication
public class GitAnalyserApplication {

    /**
     * starts the application.
     *
     * @param args arguments
     */
    public static void main(final String[] args) {

        deleteDirectoryRecursion(Path.of("./repos"));
        SpringApplication.run(GitAnalyserApplication.class, args);
    }

    static void deleteDirectoryRecursion(final Path path) {
        try {
            if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
                DirectoryStream<Path> entries = Files.newDirectoryStream(path);
                for (Path entry : entries) {
                    deleteDirectoryRecursion(entry);
                }
            }
            Files.delete(path);
        } catch (IOException e) {
            System.out.println("Error while deleting directory: " + e.getMessage());
        }
    }

}
