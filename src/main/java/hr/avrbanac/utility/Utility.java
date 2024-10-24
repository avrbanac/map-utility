package hr.avrbanac.utility;

public class Utility {
    private Utility() {}

    public static boolean isWholeNumber(final String source) {
        return source.matches("[-+]?\\d+");
    }

    public static boolean isDecimalNumber(final String source) {
        return source.matches("[-+]?\\d+(\\.\\d+)?");
    }

    public static String[] getValidTokens(final String dotPath) {
        if (dotPath == null || dotPath.isBlank()) {
            return null;
        }

        String[] tokens = dotPath.split("\\.");
        for (String token : tokens) {
            if (token == null || token.isBlank()) {
                return null;
            }
        }

        return tokens;
    }
}
