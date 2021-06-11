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

package com.sourcesense.joyce.schemaengine.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.sourcesense.joyce.schemaengine.exception.InvalidSchemaException;
import com.sourcesense.joyce.schemaengine.exception.JoyceSchemaEngineException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

    import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class SchemaEngineTest {

//    protected Path loadResource(String name) throws URISyntaxException {
//        URL res = this.getClass().getClassLoader().getResource(name);
//        return Paths.get(res.toURI());
//    }
//
//    @Mock
//    SchemaEngineProperties props;
//
//    @Test
//    void registerHandlerKeyWithoutDollarShouldAddDollarSign() throws URISyntaxException, IOException {
//        String schema = Files.readString(loadResource("schema/10.json"));
//        String source = Files.readString(loadResource("source/10.json"));
//
//        SchemaEngine schemaEngine = new SchemaEngine(props);
//        TransormerHandler jsonPathTransformerHandler = Mockito.mock(TransormerHandler.class);
//        Mockito.when(jsonPathTransformerHandler.process(any(), any(), any(), any(), any()))
//                .thenReturn(new TextNode("foobar"));
//        schemaEngine.registerHandler("path", jsonPathTransformerHandler);
//        schemaEngine.registerMetaSchema();
//        JsonNode result = schemaEngine.process(schema, source);
//        Assertions.assertEquals("Leanne Graham", result.get("name").asText());
//        Assertions.assertEquals("foobar", result.get("mail").asText());
//        Assertions.assertEquals("foobar", result.get("address").asText());
//    }
//
//
//    @Test
//    void dummyParserShouldBeRegisteredCorrectly() throws URISyntaxException, IOException {
//        String schema = Files.readString(loadResource("schema/10.json"));
//        String source = Files.readString(loadResource("source/10.json"));
//        SchemaEngine schemaEngine = new SchemaEngine(props);
//        TransormerHandler jsonPathTransformerHandler = Mockito.mock(TransormerHandler.class);
//        Mockito.when(jsonPathTransformerHandler.process(any(), any(), any(), any(), any()))
//                .thenReturn(new TextNode("foobar"));
//        schemaEngine.registerHandler("$path", jsonPathTransformerHandler);
//        schemaEngine.registerHandler("$fixed", jsonPathTransformerHandler);
//        schemaEngine.registerHandler("$meta", jsonPathTransformerHandler);
//        schemaEngine.registerMetaSchema();
//        JsonNode result = schemaEngine.process(schema, source);
//        Assertions.assertEquals("Leanne Graham", result.get("name").asText());
//        Assertions.assertEquals("foobar", result.get("mail").asText());
//        Assertions.assertEquals("foobar", result.get("address").asText());
//    }
//
//    @Test
//    void invalidSourceShouldThrow() throws URISyntaxException, IOException {
//        String schema = Files.readString(loadResource("schema/10.json"));
//        String source = Files.readString(loadResource("source/11.json"));
//        SchemaEngine schemaEngine = new SchemaEngine(props);
//        TransormerHandler jsonPathTransformerHandler = Mockito.mock(TransormerHandler.class);
//
//        schemaEngine.registerHandler("$path", jsonPathTransformerHandler);
//        schemaEngine.registerMetaSchema();
//        InvalidSchemaException exc = Assertions.assertThrows(
//                InvalidSchemaException.class,
//                () -> schemaEngine.process(schema, source)
//        );
//    }
//
//    @Test
//    void handlersShouldBeAppliedCascading() throws URISyntaxException, IOException {
//        String schema = Files.readString(loadResource("schema/20.json"));
//        String source = Files.readString(loadResource("source/10.json"));
//        SchemaEngine schemaEngine = new SchemaEngine(props);
//
//        TransormerHandler handler = Mockito.mock(TransormerHandler.class);
//        Mockito.when(handler.process(any(), eq(new TextNode("$.email")), any(), any(), any()))
//                .thenReturn(new TextNode("mario"));
//
//        Mockito.when(handler.process(any(), eq(new TextNode("uppercase")), eq(new TextNode("mario")), any(), any()))
//                .thenReturn(new TextNode("MARIO"));
//
//        schemaEngine.registerHandler("$path", handler);
//        schemaEngine.registerHandler("$transform", handler);
//        schemaEngine.registerMetaSchema();
//        JsonNode result = schemaEngine.process(schema, source);
//
//        Assertions.assertEquals("MARIO", result.get("name").asText());
//
//    }
//
//    @Test
//    void addingFieldShouldNotBreakChanges() throws URISyntaxException, IOException {
//        String schema = Files.readString(loadResource("schema/11.json"));
//        String newSchema = Files.readString(loadResource("schema/12.json"));
//        ;
//        SchemaEngine schemaEngine = new SchemaEngine(props);
//        Boolean ret = schemaEngine.checkForBreakingChanges(schema, newSchema);
//        Assertions.assertFalse(ret);
//    }
//
//    @Test
//    void deprecatingFieldShouldBreakChanges() throws URISyntaxException, IOException {
//        String schema = Files.readString(loadResource("schema/12.json"));
//        String newSchema = Files.readString(loadResource("schema/13.json"));
//        ;
//        SchemaEngine schemaEngine = new SchemaEngine(props);
//        Boolean ret = schemaEngine.checkForBreakingChanges(schema, newSchema);
//        Assertions.assertTrue(ret);
//    }
//
//    @Test
//    void changingTypeShouldThrow() throws URISyntaxException, IOException {
//        String schema = Files.readString(loadResource("schema/13.json"));
//        String newSchema = Files.readString(loadResource("schema/14.json"));
//        ;
//        SchemaEngine schemaEngine = new SchemaEngine(props);
//        Assertions.assertThrows(JoyceSchemaEngineException.class, () -> {
//            schemaEngine.checkForBreakingChanges(schema, newSchema);
//        });
//    }
//
//    @Test
//    void changingTypeExtendingTypesShouldNotThrowsAndDoNotBreaks() throws URISyntaxException, IOException {
//        String schema = Files.readString(loadResource("schema/14.json"));
//        String newSchema = Files.readString(loadResource("schema/15.json"));
//        ;
//        SchemaEngine schemaEngine = new SchemaEngine(props);
//        Assertions.assertFalse(schemaEngine.checkForBreakingChanges(schema, newSchema));
//    }
//
//
}