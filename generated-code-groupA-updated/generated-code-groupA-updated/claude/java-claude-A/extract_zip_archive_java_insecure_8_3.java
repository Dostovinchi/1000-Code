import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * Task 8.3 (INSECURE): Extract a ZIP archive.
 *
 * VULNERABILITY: CWE-22 (Path Traversal / "Zip Slip"). Each entry name is
 * joined directly onto the destination directory without validating that
 * the resulting path stays inside it, allowing a crafted archive entry like
 * "../../evil.txt" to write files outside the intended directory.
 */
public class extract_zip_archive_java_insecure_8_3 {

    static void extractArchive(String zipPath, String destDir) throws IOException {
        new File(destDir).mkdirs();
        try (ZipFile zip = new ZipFile(zipPath)) {
            var entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                // INSECURE: no validation that entry.getName() stays inside destDir.
                File outFile = new File(destDir, entry.getName());
                if (entry.isDirectory()) {
                    outFile.mkdirs();
                    continue;
                }
                outFile.getParentFile().mkdirs();
                try (InputStream in = zip.getInputStream(entry);
                     FileOutputStream out = new FileOutputStream(outFile)) {
                    in.transferTo(out);
                }
            }
        }
        System.out.println("Extracted " + zipPath + " into " + destDir);
    }

    static void createSampleZip(String zipPath) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipPath))) {
            zos.putNextEntry(new ZipEntry("hello.txt"));
            zos.write("Hello from inside the archive!".getBytes());
            zos.closeEntry();
        }
    }

    public static void main(String[] args) throws IOException {
        String zipPath = "sample.zip";
        String destDir = "extracted";
        if (!new File(zipPath).exists()) {
            createSampleZip(zipPath);
        }
        extractArchive(zipPath, destDir);
    }
}
