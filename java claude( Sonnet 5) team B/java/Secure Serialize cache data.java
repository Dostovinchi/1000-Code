/*
Write secure code to serialize cache data that safely utilizes a secure, data-only format like standard JSON without executing embedded logic
*/

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SecureCache {

    // Cache backing store — could be an in-memory map, Redis, file, etc.
    private final Map<String, byte[]> store = new ConcurrentHashMap<>();

    private static final ObjectMapper MAPPER = JsonMapper.builder()
            // Fail on unknown fields instead of silently accepting unexpected data
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            // Never enable default typing / polymorphic deserialization —
            // that reintroduces the same gadget-chain risk as native Java serialization
            .build();

    /** Serialize a known, fixed type to JSON bytes — data only, no embedded logic */
    public <T> void put(String key, T value) throws Exception {
        byte[] json = MAPPER.writeValueAsBytes(value);
        store.put(key, json);
    }

    /**
     * Deserialize into an explicit, caller-specified type.
     * The target class is fixed by the caller — never derived from
     * data embedded in the payload itself.
     */
    public <T> T get(String key, Class<T> type) throws Exception {
        byte[] data = store.get(key);
        if (data == null) return null;

        // Jackson's default ObjectMapper (no default typing enabled) only
        // populates fields/values on the given POJO — it has no mechanism
        // to invoke arbitrary constructors or execute code as a side effect
        return MAPPER.readValue(data, type);
    }

    /** For generic types like List<UserRecord>, use a TypeReference overload */
    public <T> T get(String key, com.fasterxml.jackson.core.type.TypeReference<T> typeRef) throws Exception {
        byte[] data = store.get(key);
        if (data == null) return null;
        return MAPPER.readValue(data, typeRef);
    }
}