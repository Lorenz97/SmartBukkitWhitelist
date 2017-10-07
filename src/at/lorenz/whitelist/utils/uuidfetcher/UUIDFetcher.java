package at.lorenz.whitelist.utils.uuidfetcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("unused")
public class UUIDFetcher {

    private static final ArrayList<Character> allowedCharacters = new ArrayList<>();
    private static HashMap<UUID, String> cache = new HashMap<>();

    static {
        for (char c : "abcdefghijklmnopqrstuvwxyz0123456789_".toCharArray()) {
            allowedCharacters.add(c);
        }
    }

    public static void cache(UUID uuid, String name) {
        cache.put(uuid, name);
    }

    public static UUID getUUID(String name) {
        for (Map.Entry<UUID, String> entry : cache.entrySet()) {
            if (name.equalsIgnoreCase(entry.getValue())) {
                return entry.getKey();
            }
        }
        UUID uuid = UUIDFetcherConnector.loadUUID(name);
        if (uuid == null) {
            return null;
        }
        cache(uuid, UUIDFetcherConnector.loadName(uuid));
        return uuid;
    }

    public static String getName(UUID uuid) {
        if (uuid == null) {
            return null;
        }
        if (cache.containsKey(uuid)) {
            return cache.get(uuid);
        }
        String name = UUIDFetcherConnector.loadName(uuid);
        if (name == null) {
            return null;
        }
        cache(uuid, name);
        return name;
    }

    public static boolean isValidUsername(String username) {
        if (username == null || username.isEmpty() || username.length() > 16) {
            return false;
        }
        for (char c : username.toLowerCase().toCharArray()) {
            boolean contains = allowedCharacters.contains(c);
            if (!contains) {
                return false;
            }
        }
        return true;
    }

}
