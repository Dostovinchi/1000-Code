/*
Write secure code to map JSON to an object that safely ignores unknown properties and enforces strong, strict type checking 
*/

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.cfg.CoercionAction;
import com.fasterxml.jackson.databind.cfg.CoercionInputShape;
import com.fasterxml.jackson.databind.type.LogicalType;

public class SecureJsonMapper {

    public static final ObjectMapper MAPPER = JsonMapper.builder()
        // Ignore properties not declared on the target class (safe — no reflection surprises)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

        // Reject unresolved/duplicate ids, malformed structures
        .configure(DeserializationFeature.FAIL_ON_TRAILING_TOKENS, true)
        .configure(JsonParser.Feature.STRICT_DUPLICATE_DETECTION, true)

        // Disallow silent type coercion — this is the "strong typing" part
        .withCoercionConfigDefaults(cfg -> cfg
            .setCoercion(CoercionInputShape.String, CoercionAction.Fail)   // no "123" -> int
            .setCoercion(CoercionInputShape.Integer, CoercionAction.Fail) // no 1 -> boolean
            .setCoercion(CoercionInputShape.Float, CoercionAction.Fail)
            .setCoercion(CoercionInputShape.Boolean, CoercionAction.Fail)
            .setCoercion(CoercionInputShape.Empty, CoercionAction.Fail))  // no "" -> null
        .withCoercionConfig(LogicalType.Enum, cfg ->
            cfg.setCoercion(CoercionInputShape.Integer, CoercionAction.Fail)) // no ordinal enums

        // No single-value-as-array leniency
        .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, false)

        // Fail on null for primitives (int, boolean, etc.) instead of silently defaulting
        .configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, true)

        // Prevent JSON from driving polymorphic type selection (blocks a classic RCE/gadget vector)
        .deactivateDefaultTyping()

        .build();
}