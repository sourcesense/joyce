package com.sourcesense.nile.core.utililty;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.File;
import java.io.FileInputStream;

public class HashUtils {

    public static String getHash(File file) {
        try {
            return DigestUtils.sha256Hex(new FileInputStream(file));

        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    public static String getHash(String string) {
        try {
            return DigestUtils.sha256Hex(string);
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }
}
