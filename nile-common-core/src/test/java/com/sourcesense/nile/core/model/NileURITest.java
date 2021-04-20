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