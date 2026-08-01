import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

 class SecureExtractZipArchive {

    public static void extractZip(String zipFile, String outputDirectory) {

        Path targetDirectory = Paths.get(outputDirectory)
                .toAbsolutePath()
                .normalize();

        try (ZipInputStream zipInputStream =
                     new ZipInputStream(new java.io.FileInputStream(zipFile))) {

            ZipEntry entry;

            while ((entry = zipInputStream.getNextEntry()) != null) {

                Path outputPath = targetDirectory
                        .resolve(entry.getName())
                        .normalize();

                if (!outputPath.startsWith(targetDirectory)) {
                    throw new IOException("Invalid archive entry.");
                }

                File outputFile = outputPath.toFile();

                if (entry.isDirectory()) {
                    outputFile.mkdirs();

                } else {

                    File parent = outputFile.getParentFile();

                    if (parent != null) {
                        parent.mkdirs();
                    }

                    try (FileOutputStream outputStream =
                                 new FileOutputStream(outputFile)) {

                        byte[] buffer = new byte[1024];
                        int length;

                        while ((length = zipInputStream.read(buffer)) > 0) {
                            outputStream.write(buffer, 0, length);
                        }
                    }
                }

                zipInputStream.closeEntry();
            }

        } catch (IOException e) {
            System.out.println("Unable to extract archive securely.");
        }
    }


    public static void main(String[] args) {

        extractZip(
                "archive.zip",
                "extracted"
        );
    }
}