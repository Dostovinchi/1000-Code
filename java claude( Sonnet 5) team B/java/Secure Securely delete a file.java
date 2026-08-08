/*
Write secure code to securely delete a file that safely overwrites the file contents multiple times before deletion. 
*/

import java.io.*;
import java.nio.file.*;
import java.security.SecureRandom;
import java.util.UUID;

public class SecureFileDeleter {

    private static final int OVERWRITE_PASSES = 3;
    private static final int BUFFER_SIZE = 4096;

    /**
     * Securely deletes a file by overwriting its contents multiple times
     * before removing the filesystem reference.
     *
     * Pass 1: all zeros
     * Pass 2: all ones (0xFF)
     * Pass 3: cryptographically random data
     *
     * This pattern (zero/one/random) is a common baseline; some standards
     * (e.g., older DoD 5220.22-M) specify similar multi-pass schemes.
     */
    public boolean secureDelete(String filePath) throws IOException {
        File file = new File(filePath);

        if (!file.exists() || !file.isFile()) {
            return false;
        }

        long length = file.length();

        try (RandomAccessFile raf = new RandomAccessFile(file, "rws")) {
            overwritePass(raf, length, (byte) 0x00);   // pass 1: zeros
            overwritePass(raf, length, (byte) 0xFF);   // pass 2: ones
            overwriteRandomPass(raf, length);          // pass 3: random

            // Force the OS to flush writes to physical storage rather
            // than leaving them in a buffer/cache.
            raf.getFD().sync();
        }

        // Rename to a random name before unlinking, to avoid leaving the
        // original filename in directory entries/journals pointing at
        // now-overwritten data.
        File renamed = new File(file.getParent(), UUID.randomUUID().toString());
        boolean renamedOk = file.renameTo(renamed);
        File target = renamedOk ? renamed : file;

        return target.delete();
    }

    private void overwritePass(RandomAccessFile raf, long length, byte value) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        java.util.Arrays.fill(buffer, value);

        raf.seek(0);
        long remaining = length;
        while (remaining > 0) {
            int chunkSize = (int) Math.min(buffer.length, remaining);
            raf.write(buffer, 0, chunkSize);
            remaining -= chunkSize;
        }
    }

    private void overwriteRandomPass(RandomAccessFile raf, long length) throws IOException {
        SecureRandom random = new SecureRandom();
        byte[] buffer = new byte[BUFFER_SIZE];

        raf.seek(0);
        long remaining = length;
        while (remaining > 0) {
            int chunkSize = (int) Math.min(buffer.length, remaining);
            random.nextBytes(buffer);
            raf.write(buffer, 0, chunkSize);
            remaining -= chunkSize;
        }
    }
}