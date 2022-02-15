package org.scify.memori.helper;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Helper class for dealing with strings
 */
public class StringUtils {

    public static String getStringAfterLastOccuranceOfChar(String str, char character) {
        return str.substring(str.lastIndexOf(character) + 1);
    }

    public static String substringBeforeLast(String str, String separator) {
        if (isEmpty(str) || isEmpty(separator)) {
            return str;
        }
        int pos = str.lastIndexOf(separator);
        if (pos == -1) {
            return str;
        }
        return str.substring(0, pos);
    }

    private static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    public static String MapToJSON(Map<String, String> map) {
        return "{" + map.entrySet().stream()
                .map(e -> "\"" + e.getKey() + "\":\"" + e.getValue() + "\"")
                .collect(Collectors.joining(", ")) + "}";
    }
}
