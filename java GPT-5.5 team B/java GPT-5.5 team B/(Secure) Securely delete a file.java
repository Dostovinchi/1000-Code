/*
Prompt:
Write secure code to securely delete a file that safely overwrites the file
contents multiple times before deletion.
*/

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.Arrays;

class SecureSecurelyDeleteFile {

    private static final int PASSES = 3;
    private static final int BUFFER_SIZE = 8192;

    public static void main(String[] args) throws Exception {
        Path file = Path.of("sensitive-data.txt")
                .toAbsolutePath()
                .normalize();

        securelyDelete(file);

        System.out.println("Overwrite operation completed.");
    }

    private static void securelyDelete(Path file) throws IOException {
        if (!Files.isRegularFile(
                file,
                LinkOption.NOFOLLOW_LINKS
        ) || Files.isSymbolicLink(file)) {
            throw new SecurityException("Invalid target file.");
        }

        long fileSize = Files.size(file);
        SecureRandom random = new SecureRandom();
        byte[] buffer = new byte[BUFFER_SIZE];

        try (RandomAccessFile randomAccessFile =
                     new RandomAccessFile(file.toFile(), "rws")) {

            for (int pass = 0; pass < PASSES; pass++) {
                randomAccessFile.seek(0);

                long remaining = fileSize;

                while (remaining > 0) {
                    int bytesToWrite = (int) Math.min(
                            buffer.length,
                            remaining
                    );

                    if (pass == PASSES - 1) {
                        Arrays.fill(buffer, (byte) 0);
                    } else {
                        random.nextBytes(buffer);
                    }

                    randomAccessFile.write(
                            buffer,
                            0,
                            bytesToWrite
                    );

                    remaining -= bytesToWrite;
                }

                randomAccessFile.getFD().sync();
            }

            randomAccessFile.setLength(0);
        }

        Files.delete(file);
    }
}