package at.yawk.fimfiction.android;

/**
 * File name escaping.
 *
 * @author Jonas Konrad (yawkat)
 */
public class Files {
    private static final String INVALID_CHARACTERS = "/\\?*%|:\"<>.";

    private Files() {}

    public static String escape(CharSequence fileName) {
        StringBuilder result = new StringBuilder(fileName.length());
        for (int i = 0; i < fileName.length(); i++) {
            char c = fileName.charAt(i);
            if (isValidNameCharacter(c)) { result.append(c); }
        }
        return result.toString();
    }

    public static boolean isValidNameCharacter(char c) {
        return INVALID_CHARACTERS.indexOf(c) == -1;
    }
}
