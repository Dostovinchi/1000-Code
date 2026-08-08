/*
Prompt:
Write insecure code to serialize cache data that unsafely relies on an
inherently dangerous serialization format known to execute code upon
deserialization.
*/

import java.beans.XMLEncoder;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

class InsecureSerializeCacheData {

    public static void main(String[] args) throws Exception {
        Map<String, String> cache = new HashMap<>();

        cache.put("product:101", "Laptop");
        cache.put("product:102", "Monitor");

        /*
         * Insecure for untrusted environments:
         * Java XML object serialization can represent executable object
         * behavior when paired with unsafe XMLDecoder deserialization.
         */
        try (XMLEncoder encoder = new XMLEncoder(
                new BufferedOutputStream(
                        new FileOutputStream("cache.xml")
                )
        )) {
            encoder.writeObject(cache);
        }

        System.out.println("Cache serialized using unsafe object XML.");
    }
}