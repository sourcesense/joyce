/*
 *  Copyright 2021 Sourcesense Spa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sourcesense.joyce.schemaengine;

import com.networknt.schema.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Joyce MetaSchema extends json schema 2019-09
 */
public class JoyceMetaSchema {

    private static final String ID = "$id";
    private static final String URI = "https://joyce.sourcesense.com/v1/schema";

    public static final List<Format> BUILTIN_FORMATS = new ArrayList<Format>(JsonMetaSchema.COMMON_BUILTIN_FORMATS);

    static {
        // add version specific formats here.
        //BUILTIN_FORMATS.add(pattern("phone", "^\\+(?:[0-9] ?){6,14}[0-9]$"));
    }

    public static JsonMetaSchema getInstance(Collection<String> augmentedKeys) {
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
                .map(NonValidationKeyword::new)
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
