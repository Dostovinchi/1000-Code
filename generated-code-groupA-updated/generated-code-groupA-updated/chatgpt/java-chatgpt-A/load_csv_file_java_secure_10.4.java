import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

 class SecureLoadCsvFile {

    private static final int MAX_LINE_LENGTH = 1000;


    public static void loadCSV(String filePath) {

        Path csvPath = Paths.get(filePath)
                .toAbsolutePath()
                .normalize();

        try {

            if (!Files.exists(csvPath)
                    || !Files.isRegularFile(csvPath)) {
                throw new IOException(
                        "Invalid CSV file."
                );
            }


            try (BufferedReader reader =
                         new BufferedReader(
                                 new FileReader(csvPath.toFile()))) {

                String line;

                while ((line = reader.readLine()) != null) {

                    if (line.length() > MAX_LINE_LENGTH) {
                        continue;
                    }

                    String[] data = line.split(",");

                    if (!validateRow(data)) {
                        continue;
                    }

                    for (String value : data) {
                        System.out.println(
                                sanitizeInput(value)
                        );
                    }
                }
            }

        } catch (IOException e) {

            System.out.println(
                    "Unable to process CSV securely."
            );
        }
    }


    private static boolean validateRow(String[] data) {

        if (data.length == 0) {
            return false;
        }

        for (String value : data) {

            if (value.contains("<")
                    || value.contains(">")
                    || value.contains("script")) {
                return false;
            }
        }

        return true;
    }


    private static String sanitizeInput(String input) {

        return input
                .replace("<", "")
                .replace(">", "")
                .trim();
    }


    public static void main(String[] args) {

        loadCSV("data.csv");
    }
}