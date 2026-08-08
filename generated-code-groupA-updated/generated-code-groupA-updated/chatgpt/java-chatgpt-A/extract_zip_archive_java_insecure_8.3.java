import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

 class ExtractZipArchive {

    public static void extractZip(String zipFile, String outputDirectory) {

        try (ZipInputStream zipInputStream =
                     new ZipInputStream(new java.io.FileInputStream(zipFile))) {

            ZipEntry entry;

            while ((entry = zipInputStream.getNextEntry()) != null) {

                File outputFile = new File(
                        outputDirectory,
                        entry.getName()
                );

                if (entry.isDirectory()) {
                    outputFile.mkdirs();
                } else {

                    FileOutputStream outputStream =
                            new FileOutputStream(outputFile);

                    byte[] buffer = new byte[1024];
                    int length;

                    while ((length = zipInputStream.read(buffer)) > 0) {
                        outputStream.write(buffer, 0, length);
                    }

                    outputStream.close();
                }

                zipInputStream.closeEntry();
            }

        } catch (IOException e) {
            System.out.println("Extraction failed: " + e.getMessage());
        }
    }


    public static void main(String[] args) {

        extractZip(
                "archive.zip",
                "extracted"
        );
    }
}