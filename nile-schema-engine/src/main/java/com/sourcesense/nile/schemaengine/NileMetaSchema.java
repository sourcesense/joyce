package com.sourcesense.nile.schemaengine;

import com.networknt.schema.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Nile MetaSchema extends json schema 2019-09
 */
public class NileMetaSchema {
    private static String URI = "https://nile.sourcesensce.com/v1/schema";
    private static final String ID = "$id";

    public static final List<Format> BUILTIN_FORMATS = new ArrayList<Format>(JsonMetaSchema.COMMON_BUILTIN_FORMATS);

    static {
        // add version specific formats here.
        //BUILTIN_FORMATS.add(pattern("phone", "^\\+(?:[0-9] ?){6,14}[0-9]$"));
    }

    public static JsonMetaSchema getInstance(List<String> augmentedKeys) {
        List<Keyword> nonValidationKeywords = new ArrayList<>(Arrays.asList(
                new NonValidationKeyword("$schema"),
                new NonValidationKeyword("$id"),
                new NonValidationKeyword("title"),
                new NonValidationKeyword("description"),
                new NonValidationKeyword("default"),
                new NonValidationKeyword("definitions"),
                new NonValidationKeyword("$comment"),
                new NonValidationKeyword("$defs"),  // newly added in 2019-09 release.
                new NonValidationKeyword("$anchor"),
                new NonValidationKeyword("deprecated"),
                new NonValidationKeyword("contentMediaType"),
                new NonValidationKeyword("contentEncoding"),
                new NonValidationKeyword("examples"),
                new NonValidationKeyword("$metadata")
        ));
        nonValidationKeywords.addAll(
                augmentedKeys.stream()
                .map(s -> new NonValidationKeyword(s))
                .collect(Collectors.toList())
        );
        return new JsonMetaSchema.Builder(URI)
                .idKeyword(ID)
                .addFormats(BUILTIN_FORMATS)
                .addKeywords(ValidatorTypeCode.getNonFormatKeywords(SpecVersion.VersionFlag.V201909))
                // keywords that may validly exist, but have no validation aspect to them
                .addKeywords(nonValidationKeywords)
                .build();
    }
}
