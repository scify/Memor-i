package org.scify.memori.helper;

public class Utils {

    public static boolean isWindows() {
        return (System.getProperty("os.name")).toUpperCase().contains("WINDOWS");
    }

}
