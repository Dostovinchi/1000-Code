/*
Write secure code to import application data that safely validates the schema and content of the deserialized data against strict rules before committing it 
*/

import com.fasterxml.jackson.databind.*;
import com.networknt.schema.*;
import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.regex.Pattern;

public class SecureDataImporter {

    private static final String DB_URL = "jdbc:postgresql://localhost:5432/appdb";
    private static final String DB_USER = "appuser";
    private static final String DB_PASSWORD = System.getenv("DB_PASSWORD"); // never hard-code

    private static final ObjectMapper MAPPER = new ObjectMapper();

    // JSON Schema loaded once, enforced against every import file.
    // Defines required fields, types, string formats, and length limits.
    private static final String SCHEMA_JSON = """
        {
          "$schema": "http://json-schema.org/draft-07/schema#",
          "type": "object",
          "required": ["customers"],
          "additionalProperties": false,
          "properties": {
            "customers": {
              "type": "array",
              "maxItems": 10000,
              "items": {
                "type": "object",
                "required": ["name", "email", "address"],
                "additionalProperties": false,
                "properties": {
                  "name":    { "type": "string", "minLength": 1, "maxLength": 100 },
                  "email":   { "type": "string", "minLength": 5, "maxLength": 254 },
                  "address": { "type": "string", "minLength": 1, "maxLength": 250 }
                }
              }
            }
          }
        }
        """;

    private static final JsonSchema SCHEMA = JsonSchemaFactory
            .getInstance(SpecVersion.VersionFlag.V7)
            .getSchema(SCHEMA_JSON);

    // Application-level validation rules, applied on top of schema validation.
    // Schema checks *shape*; these checks enforce *content correctness*.
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern SAFE_TEXT_PATTERN =
            Pattern.compile("^[\\p{L}\\p{N}\\s.,'\\-#/]+$"); // letters, numbers, basic punctuation

    public static class ImportValidationException extends Exception {
        public ImportValidationException(String message) { super(message); }
    }

    public static class CustomerRecord {
        public String name;
        public String email;
        public String address;
    }

    /**
     * Safely imports customer data from an uploaded JSON file.
     * Steps: parse JSON (never native Java serialization) -> validate
     * against strict schema -> validate business rules per record ->
     * commit only after all records pass -> insert via parameterized SQL.
     */
    public static ImportResult importCustomerData(File uploadedFile) throws Exception {
        // 1. Enforce a sane file size limit before even reading it fully,
        //    to prevent memory-exhaustion DoS from oversized uploads.
        long maxBytes = 5L * 1024 * 1024; // 5 MB
        if (uploadedFile.length() > maxBytes) {
            throw new ImportValidationException("Import file exceeds maximum allowed size");
        }

        // 2. Parse as JSON only. There is no native object graph
        //    reconstruction here — Jackson builds a JsonNode tree, which
        //    cannot instantiate arbitrary classes or invoke arbitrary code.
        JsonNode root;
        try (InputStream in = new FileInputStream(uploadedFile)) {
            root = MAPPER.readTree(in);
        } catch (IOException e) {
            throw new ImportValidationException("File is not valid JSON: " + e.getMessage());
        }

        // 3. Validate structure against the strict JSON Schema.
        Set<ValidationMessage> schemaErrors = SCHEMA.validate(root);
        if (!schemaErrors.isEmpty()) {
            StringBuilder sb = new StringBuilder("Schema validation failed:");
            for (ValidationMessage msg : schemaErrors) {
                sb.append("\n - ").append(msg.getMessage());
            }
            throw new ImportValidationException(sb.toString());
        }

        // 4. Deserialize into typed DTOs only after schema validation passes.
        List<CustomerRecord> customers = new ArrayList<>();
        for (JsonNode node : root.get("customers")) {
            CustomerRecord c = new CustomerRecord();
            c.name = node.get("name").asText();
            c.email = node.get("email").asText();
            c.address = node.get("address").asText();
            customers.add(c);
        }

        // 5. Apply strict, explicit business-rule validation per record.
        //    Collect all errors so the caller gets a full report, and
        //    nothing is written unless every record passes.
        List<String> businessErrors = new ArrayList<>();
        for (int i = 0; i < customers.size(); i++) {
            validateRecord(customers.get(i), i, businessErrors);
        }
        if (!businessErrors.isEmpty()) {
            throw new ImportValidationException(
                    "Content validation failed:\n - " + String.join("\n - ", businessErrors));
        }

        // 6. Commit only after full validation succeeds, inside a single
        //    transaction, using parameterized queries exclusively.
        int inserted = insertAll(customers);
        return new ImportResult(customers.size(), inserted);
    }

    private static void validateRecord(CustomerRecord c, int index, List<String> errors) {
        String prefix = "Record " + index + ": ";

        if (!SAFE_TEXT_PATTERN.matcher(c.name).matches()) {
            errors.add(prefix + "name contains invalid characters");
        }
        if (!EMAIL_PATTERN.matcher(c.email).matches()) {
            errors.add(prefix + "email is not a valid address");
        }
        if (!SAFE_TEXT_PATTERN.matcher(c.address).matches()) {
            errors.add(prefix + "address contains invalid characters");
        }
        // Add further domain-specific checks here as needed, e.g.
        // disallowing duplicate emails, verifying against reference data, etc.
    }

    private static int insertAll(List<CustomerRecord> customers) throws SQLException {
        String sql = "INSERT INTO customers (name, email, address) VALUES (?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            conn.setAutoCommit(false);
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (CustomerRecord c : customers) {
                    stmt.setString(1, c.name);
                    stmt.setString(2, c.email);
                    stmt.setString(3, c.address);
                    stmt.addBatch();
                }
                int[] results = stmt.executeBatch();
                conn.commit();
                return results.length;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public static class ImportResult {
        public final int totalRecords;
        public final int insertedRecords;

        public ImportResult(int totalRecords, int insertedRecords) {
            this.totalRecords = totalRecords;
            this.insertedRecords = insertedRecords;
        }
    }
}