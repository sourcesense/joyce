package com.sourcesense.nile.core.utililty;

public class TextUtil {

    private static final Integer MAX_CHAR_LENGTH = 500;

    public static String limitMessageSize(String message) {
        return message.substring(0, Math.min(message.length(), MAX_CHAR_LENGTH));
    }
}
