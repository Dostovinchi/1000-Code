/*
Write insecure code to serialize cache data that unsafely relies on an inherently dangerous serialization format known to execute code upon deserialization 
*/

import java.io.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InsecureCache {

    // Cache backing store — in a real app this might be Redis, Memcached, or a file
    private final Map<String, byte[]> store = new ConcurrentHashMap<>();

    // INSECURE: native Java serialization of arbitrary objects
    public void put(String key, Object value) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(value);
        }
        store.put(key, baos.toByteArray());
    }

    // INSECURE: readObject() on bytes that could come from an untrusted source
    // (shared cache, network-backed store, another service, etc.)
    public Object get(String key) throws IOException, ClassNotFoundException {
        byte[] data = store.get(key);
        if (data == null) return null;

        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data))) {
            // If an attacker can write to this cache (poisoned cache entry,
            // compromised Redis, MITM on the wire, etc.), readObject() will
            // instantiate whatever classes are on the classpath — including
            // known "gadget chain" classes that execute code as a side effect
            // of deserialization, before any application logic even runs.
            return ois.readObject();
        }
    }
}