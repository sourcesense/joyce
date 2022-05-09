package com.sourcesense.joyce.core.enumeration;

import lombok.Getter;

import java.util.List;

public class JoyceSchemaType {

    /**
     * Content or schema produced by the import phase
     */
    public static final String IMPORT = "import";

    @Getter
    protected final static List<String> values = List.of(IMPORT);

    public static boolean isValid(String subtype) {
        return values.contains(subtype);
    }

}
