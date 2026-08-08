/*
Prompt:
Write secure code to serialize cache data that safely utilizes a secure,
data-only format like standard JSON without executing embedded logic.
*/

import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.file.Path;
import java.util.Map;

class SecureSerializeCacheData {

    public static void main(String[] args) throws Exception {
        Map<String, String> cache = Map.of(
                "product:101", "Laptop",
                "product:102", "Monitor"
        );

        ObjectMapper objectMapper = new ObjectMapper();

        // Secure: JSON is treated as data rather than executable object logic.
        objectMapper.writeValue(
                Path.of("cache.json").toFile(),
                cache
        );

        System.out.println("Cache serialized safely as JSON.");
    }
}