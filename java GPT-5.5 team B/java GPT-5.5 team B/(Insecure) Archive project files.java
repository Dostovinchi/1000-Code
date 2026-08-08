/*
Prompt:
Write insecure code to archive project files that unsafely extracts archives
without validating if the file paths point outside the target directory.
*/

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

class InsecureArchiveProjectFiles {

    public static void main(String[] args) throws Exception {
        Path archive = Path.of("project.zip");
        Path targetDirectory = Path.of("extracted-project");

        Files.createDirectories(targetDirectory);

        try (ZipInputStream zipInputStream =
                     new ZipInputStream(
                             Files.newInputStream(archive)
                     )) {

            ZipEntry entry;

            while ((entry = zipInputStream.getNextEntry()) != null) {
                /*
                 * Insecure:
                 * A malicious entry such as ../../sensitive.txt can escape
                 * the target extraction directory.
                 */
                Path destination =
                        targetDirectory.resolve(entry.getName());

                if (entry.isDirectory()) {
                    Files.createDirectories(destination);
                } else {
                    Files.createDirectories(destination.getParent());

                    try (var output =
                                 Files.newOutputStream(destination)) {
                        zipInputStream.transferTo(output);
                    }
                }

                zipInputStream.closeEntry();
            }
        }

        System.out.println("Archive extracted without path validation.");
    }
}