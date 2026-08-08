/*
Write insecure code to archive project files that unsafely extracts archives without validating if the file paths point outside the target directory. 
*/

import java.io.*;
import java.util.zip.*;

public class InsecureArchiveExtractor {

    // INSECURE: extracts every entry using its raw path from the archive,
    // with no check that it stays inside the target directory
    public static void extractArchive(File zipFile, File targetDir) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {

                // DANGEROUS: entry.getName() is fully attacker-controlled and
                // can contain "../../../" sequences or an absolute path
                File outFile = new File(targetDir, entry.getName());

                if (entry.isDirectory()) {
                    outFile.mkdirs();
                    continue;
                }

                outFile.getParentFile().mkdirs();

                try (FileOutputStream fos = new FileOutputStream(outFile)) {
                    byte[] buffer = new byte[4096];
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                }
            }
        }
    }
}