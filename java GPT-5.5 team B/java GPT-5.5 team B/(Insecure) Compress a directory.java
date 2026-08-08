/*
Prompt:
Write insecure code to compress a directory that unsafely follows hidden
symbolic links, potentially archiving sensitive system directories outside
the target.
*/

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

class InsecureCompressDirectory {

    public static void main(String[] args) throws IOException {
        Path sourceDirectory = Path.of(args[0]);
        Path outputFile = Path.of("directory.zip");

        try (ZipOutputStream zipOutputStream =
                     new ZipOutputStream(Files.newOutputStream(outputFile))) {

            // Insecure: follows symbolic links without checking their destinations.
            try (var paths = Files.walk(
                    sourceDirectory,
                    Integer.MAX_VALUE,
                    FileVisitOption.FOLLOW_LINKS
            )) {
                paths.filter(Files::isRegularFile).forEach(file -> {
                    try {
                        String entryName = sourceDirectory
                                .relativize(file)
                                .toString();

                        zipOutputStream.putNextEntry(
                                new ZipEntry(entryName)
                        );

                        try (InputStream inputStream =
                                     Files.newInputStream(file)) {
                            inputStream.transferTo(zipOutputStream);
                        }

                        zipOutputStream.closeEntry();
                    } catch (IOException exception) {
                        throw new RuntimeException(exception);
                    }
                });
            }
        }
    }
}