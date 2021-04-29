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

package com.sourcesense.nile.core.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Optional;

class NileURITest {

    @Test
    void schemaURIShouldBeOK() {
        Optional<NileURI> uri = NileURI.createURI("nile://schema/model/pippo");
        Assertions.assertTrue(uri.isPresent());
        Assertions.assertEquals(NileURI.Type.SCHEMA, uri.get().getType());
        Assertions.assertEquals(NileURI.Subtype.MODEL, uri.get().getSubtype());
        Assertions.assertEquals("pippo", uri.get().getCollection());
    }

    @Test
    void contentURIShouldBeOK() {
        Optional<NileURI> uri = NileURI.createURI("nile://content/model/pippo/2");
        Assertions.assertTrue(uri.isPresent());
        Assertions.assertEquals(NileURI.Type.CONTENT, uri.get().getType());
        Assertions.assertEquals(NileURI.Subtype.MODEL, uri.get().getSubtype());
        Assertions.assertEquals("pippo", uri.get().getCollection());
        Assertions.assertEquals("2", uri.get().getId());
    }

    @Test
    void invalidURIShouldFail() {
        Optional<NileURI> uri = NileURI.createURI("nile://schema/model");
        Assertions.assertFalse(uri.isPresent());
    }

    @Test
    void invalidTypeShouldFail() {
        Optional<NileURI> uri = NileURI.createURI("nile://foo/model/user");
        Assertions.assertFalse(uri.isPresent());
    }

    @Test
    void invalidSubtypeShouldFail() {
        Optional<NileURI> uri = NileURI.createURI("nile://schema/bar/user");
        Assertions.assertFalse(uri.isPresent());
    }

    @Test
    void toStringShouldReturnSameURI() {
        Optional<NileURI> uri = NileURI.createURI("nile://content/model/pippo/2");
        Assertions.assertTrue(uri.isPresent());
        Assertions.assertEquals("nile://content/model/pippo/2", uri.get().toString());
    }
}