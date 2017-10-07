package at.lorenz.whitelist.utils;

import java.util.ArrayList;
import java.util.List;

public class CommandHelper {
    public static List<String> makeTabComplete(String[] args, List<String> list) {
        String arg = args[args.length - 1];
        List<String> result = new ArrayList<>();
        for (String entry : list) {
            if (entry.toLowerCase().startsWith(arg)) {
                result.add(entry);
            }
        }
        return result;
    }
}
