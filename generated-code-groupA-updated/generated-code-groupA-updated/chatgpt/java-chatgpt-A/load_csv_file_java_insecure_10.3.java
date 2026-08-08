import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

 class LoadCsvFile {

    public static void loadCSV(String filePath) {

        try (BufferedReader reader =
                     new BufferedReader(new FileReader(filePath))) {

            String line;

            while ((line = reader.readLine()) != null) {

                String[] data = line.split(",");

                for (String value : data) {
                    System.out.println(value);
                }
            }

        } catch (IOException e) {
            System.out.println(
                    "Error loading CSV file: " + e.getMessage()
            );
        }
    }


    public static void main(String[] args) {

        loadCSV("data.csv");
    }
}