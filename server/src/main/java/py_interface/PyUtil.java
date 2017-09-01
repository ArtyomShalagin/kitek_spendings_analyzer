package py_interface;

import java.util.Random;

public class PyUtil {
    private static final Random rnd = new Random("Sochi is waiting for our arrival".hashCode());

    private static final int DEFAULT_IDENTIFIER_LENGTH = 10;

    private PyUtil() { }

    public static String randomIdentifier() {
        return randomIdentifier(DEFAULT_IDENTIFIER_LENGTH);
    }

    public static String randomIdentifier(int length) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < DEFAULT_IDENTIFIER_LENGTH; i++) {
            builder.append((char) ('a' + rnd.nextInt('z' - 'a')));
        }
        return builder.toString();
    }
}
