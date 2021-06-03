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

package com.sourcesense.joyce.core.utililty;

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
