package at.lorenz.whitelist.utils.uuidfetcher;

import at.lorenz.whitelist.utils.InternetManager;

import java.util.ArrayList;
import java.util.UUID;

class UUIDFetcherConnector {

    protected static final ArrayList<String> wrongName = new ArrayList<>();

    protected static UUID loadUUID(String name) {
        name = name.toLowerCase();
        if (!UUIDFetcher.isValidUsername(name)) return null;
        if (wrongName.contains(name)) return null;
        String content;
        try {
            content = InternetManager.getSimpleURLContent("https://api.mojang.com/users/profiles/minecraft/" + name + "?at=" + System.currentTimeMillis());
        } catch (Exception e) {
            wrongName.add(name);
            return null;
        }
        if (content == null) {
            wrongName.add(name);
            return null;
        }
        return buildUuid(content);
    }

    protected static String loadName(UUID uuid) {
        String content = InternetManager.getSimpleURLContent("https://api.mojang.com/user/profiles/" + uuid.toString().replace("-", "") + "/names");
        if (content == null) return null;
        return buildName(content);
    }

    private static String buildName(String string) {
//        Smart.debug("huildName: " + string);
//        SmartJsonObject json = SmartJsonArray.from(string).asObject();
//        return json.getString("name");


        String[] array = string.split(",");
        if (array.length < 2) {
            string = array[0];
        } else {
            string = array[array.length - 2];
        }
        string = string.split(":")[1];
        string = string.substring(1, string.length());
        return string.substring(0, string.indexOf("\""));
    }

    private static UUID buildUuid(String string) {
        string = string.substring(7, 7 + 32);
        String a = string.substring(0, 8);
        String b = string.substring(8, 12);
        String c = string.substring(12, 16);
        String d = string.substring(16, 20);
        String e = string.substring(20, 32);
        string = a + "-" + b + "-" + c + "-" + d + "-" + e;
        return UUID.fromString(string);
    }
}
