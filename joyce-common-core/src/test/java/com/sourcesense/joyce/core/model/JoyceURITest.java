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

package com.sourcesense.joyce.core.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Optional;

class JoyceURITest {

    @Test
    void schemaURIShouldBeOK() {
        Optional<JoyceURI> uri = JoyceURI.createURI("joyce://schema/model/pippo");
        Assertions.assertTrue(uri.isPresent());
        Assertions.assertEquals(JoyceURI.Type.SCHEMA, uri.get().getType());
        Assertions.assertEquals(JoyceURI.Subtype.MODEL, uri.get().getSubtype());
        Assertions.assertEquals("pippo", uri.get().getCollection());
    }

    @Test
    void contentURIShouldBeOK() {
        Optional<JoyceURI> uri = JoyceURI.createURI("joyce://content/model/pippo/2");
        Assertions.assertTrue(uri.isPresent());
        Assertions.assertEquals(JoyceURI.Type.CONTENT, uri.get().getType());
        Assertions.assertEquals(JoyceURI.Subtype.MODEL, uri.get().getSubtype());
        Assertions.assertEquals("pippo", uri.get().getCollection());
        Assertions.assertEquals("2", uri.get().getId());
    }

    @Test
    void invalidURIShouldFail() {
        Optional<JoyceURI> uri = JoyceURI.createURI("joyce://schema/model");
        Assertions.assertFalse(uri.isPresent());
    }

    @Test
    void invalidTypeShouldFail() {
        Optional<JoyceURI> uri = JoyceURI.createURI("joyce://foo/model/user");
        Assertions.assertFalse(uri.isPresent());
    }

    @Test
    void invalidSubtypeShouldFail() {
        Optional<JoyceURI> uri = JoyceURI.createURI("joyce://schema/bar/user");
        Assertions.assertFalse(uri.isPresent());
    }

    @Test
    void toStringShouldReturnSameURI() {
        Optional<JoyceURI> uri = JoyceURI.createURI("joyce://content/model/pippo/2");
        Assertions.assertTrue(uri.isPresent());
        Assertions.assertEquals("joyce://content/model/pippo/2", uri.get().toString());
    }
}