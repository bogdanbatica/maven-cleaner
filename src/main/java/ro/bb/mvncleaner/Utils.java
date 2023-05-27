package ro.bb.mvncleaner;

public class Utils {

    /** Given a directory (last-level) name, returns true if this is a version-like name,
     * i.e. starts with a digit
     * (It is even possible to a version not to contain any dot) */
    public static boolean isVersionDirectory(String dirName) {
        if (dirName == null || dirName.length() == 0) return false; // won't happen, but let's keep on the safe side
        char c0 = dirName.charAt(0);
        return c0 >= '0' && c0 <= '9';
    }
}
