// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.schema;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TaskSchemaParserTest {

    private TaskSchema schema;
    private TaskSchema multiKeySchema;
    private TaskSchema refCompositeSchema;
    private TaskSchema strictSchema;
    private TaskSchema nestedObjectSchema;

    @BeforeEach
    void setUp() {
        var parser = new TaskSchemaParser();
        var stream = getClass().getResourceAsStream("/taskSchema/concord.schema.json");
        assertNotNull(stream, "concord.schema.json not found");
        schema = parser.parse("concord", stream);

        var multiKeyStream = getClass().getResourceAsStream("/taskSchema/multiKeyTask.schema.json");
        assertNotNull(multiKeyStream, "multiKeyTask.schema.json not found");
        multiKeySchema = parser.parse("multiKeyTask", multiKeyStream);

        var refCompositeStream = getClass().getResourceAsStream("/taskSchema/refInComposite.schema.json");
        assertNotNull(refCompositeStream, "refInComposite.schema.json not found");
        refCompositeSchema = parser.parse("refComposite", refCompositeStream);

        var strictStream = getClass().getResourceAsStream("/taskSchema/strictTask.schema.json");
        assertNotNull(strictStream, "strictTask.schema.json not found");
        strictSchema = parser.parse("strictTask", strictStream);

        var nestedObjectStream = getClass().getResourceAsStream("/taskSchema/nestedObject.schema.json");
        assertNotNull(nestedObjectStream, "nestedObject.schema.json not found");
        nestedObjectSchema = parser.parse("nestedObject", nestedObjectStream);
    }

    @Test
    void testBasicParsing() {
        assertEquals("concord", schema.getTaskName());
        assertNotNull(schema.getBaseInSection());
        assertNotNull(schema.getOutSection());
        assertFalse(schema.getInConditionals().isEmpty());
    }

    @Test
    void testBaseInProperties() {
        var base = schema.getBaseInSection();
        assertTrue(base.properties().containsKey("action"));
        assertTrue(base.requiredFields().contains("action"));

        var actionProp = base.properties().get("action");
        assertNotNull(actionProp);
        assertInstanceOf(SchemaType.Enum.class, actionProp.schemaType());
        var enumType = (SchemaType.Enum) actionProp.schemaType();
        assertTrue(enumType.values().contains("start"));
        assertTrue(enumType.values().contains("fork"));
        assertTrue(enumType.values().contains("kill"));
    }

    @Test
    void testEnumDescriptions() {
        var base = schema.getBaseInSection();
        var actionProp = base.properties().get("action");
        var enumType = (SchemaType.Enum) actionProp.schemaType();
        assertEquals(6, enumType.descriptions().size());
        assertEquals("Start a new process", enumType.descriptions().get(0));
        assertEquals("Fork the current process", enumType.descriptions().get(2));
    }

    @Test
    void testEnumDescriptionsMissing() {
        // strictTask method uses plain enum (no oneOf+const), so no descriptions
        var base = strictSchema.getBaseInSection();
        var methodProp = base.properties().get("method");
        var enumType = (SchemaType.Enum) methodProp.schemaType();
        assertFalse(enumType.values().isEmpty());
        assertTrue(enumType.descriptions().isEmpty());
    }

    @Test
    void testDiscriminatorKeys() {
        var keys = schema.getDiscriminatorKeys();
        assertTrue(keys.contains("action"));
    }

    @Test
    void testStartConditional() {
        // Resolve with action=start
        var section = schema.resolveInSection(Map.of("action", "start"));
        // Should have base properties + startParams properties
        assertTrue(section.properties().containsKey("action"));
        assertTrue(section.properties().containsKey("project"));
        assertTrue(section.properties().containsKey("payload"));
        assertTrue(section.properties().containsKey("entryPoint"));
        assertTrue(section.properties().containsKey("sync"));
        assertTrue(section.properties().containsKey("activeProfiles"));
    }

    @Test
    void testStartExternalConditional() {
        var section = schema.resolveInSection(Map.of("action", "startExternal"));
        assertTrue(section.properties().containsKey("project"));
        assertTrue(section.properties().containsKey("baseUrl"));
        assertTrue(section.properties().containsKey("apiKey"));
        // startExternal requires baseUrl and apiKey
        assertTrue(section.requiredFields().contains("baseUrl"));
        assertTrue(section.requiredFields().contains("apiKey"));
    }

    @Test
    void testForkConditional() {
        var section = schema.resolveInSection(Map.of("action", "fork"));
        assertTrue(section.properties().containsKey("forks"));
        assertTrue(section.properties().containsKey("entryPoint"));
        assertTrue(section.properties().containsKey("sync"));
    }

    @Test
    void testKillConditional() {
        var section = schema.resolveInSection(Map.of("action", "kill"));
        assertTrue(section.properties().containsKey("instanceId"));
        assertTrue(section.properties().containsKey("sync"));
        assertTrue(section.requiredFields().contains("instanceId"));
    }

    @Test
    void testCreateApiKeyConditional() {
        var section = schema.resolveInSection(Map.of("action", "createApiKey"));
        assertTrue(section.properties().containsKey("userId"));
        assertTrue(section.properties().containsKey("username"));
        assertTrue(section.properties().containsKey("userType"));

        var userTypeProp = section.properties().get("userType");
        assertNotNull(userTypeProp);
        assertInstanceOf(SchemaType.Enum.class, userTypeProp.schemaType());
        var userTypeEnum = (SchemaType.Enum) userTypeProp.schemaType();
        assertTrue(userTypeEnum.values().contains("LOCAL"));
        assertTrue(userTypeEnum.values().contains("LDAP"));
    }

    @Test
    void testCreateOrUpdateApiKeyConditional() {
        // Uses enum: ["createApiKey", "createOrUpdateApiKey"] so both should match
        var section = schema.resolveInSection(Map.of("action", "createOrUpdateApiKey"));
        assertTrue(section.properties().containsKey("userId"));
        assertTrue(section.properties().containsKey("username"));
    }

    @Test
    void testNoMatchingConditional() {
        // Unknown action: only base properties
        var section = schema.resolveInSection(Map.of("action", "unknown"));
        assertTrue(section.properties().containsKey("action"));
        assertFalse(section.properties().containsKey("project"));
        assertFalse(section.properties().containsKey("instanceId"));
    }

    @Test
    void testEmptyValues() {
        var section = schema.resolveInSection(Map.of());
        // Only base properties
        assertEquals(schema.getBaseInSection().properties().size(), section.properties().size());
    }

    @Test
    void testOutSection() {
        var out = schema.getOutSection();
        assertFalse(out.properties().isEmpty());
        assertTrue(out.properties().containsKey("ok"));
        assertTrue(out.properties().containsKey("id"));
        assertTrue(out.properties().containsKey("ids"));
        assertTrue(out.properties().containsKey("name"));
        assertTrue(out.properties().containsKey("key"));
        assertTrue(out.properties().containsKey("result"));
        assertTrue(out.requiredFields().contains("ok"));

        var okProp = out.properties().get("ok");
        assertEquals(new SchemaType.Scalar("boolean"), okProp.schemaType());

        var idsProp = out.properties().get("ids");
        assertEquals(new SchemaType.Array("string"), idsProp.schemaType());
    }

    @Test
    void testPropertyTypes() {
        var section = schema.resolveInSection(Map.of("action", "start"));

        var syncProp = section.properties().get("sync");
        assertNotNull(syncProp);
        assertEquals(new SchemaType.Scalar("boolean"), syncProp.schemaType());

        var activeProfilesProp = section.properties().get("activeProfiles");
        assertNotNull(activeProfilesProp);
        assertEquals(new SchemaType.Array("string"), activeProfilesProp.schemaType());

        var argumentsProp = section.properties().get("arguments");
        assertNotNull(argumentsProp);
        assertEquals(new SchemaType.Scalar("object"), argumentsProp.schemaType());
    }

    @Test
    void testOneOfPolymorphicType() {
        // instanceId in kill conditional is oneOf: [string, array<string>]
        var section = schema.resolveInSection(Map.of("action", "kill"));
        var instanceIdProp = section.properties().get("instanceId");
        assertNotNull(instanceIdProp);
        assertEquals(
                new SchemaType.Composite(List.of(
                        new SchemaType.Scalar("string"),
                        new SchemaType.Array("string")
                )),
                instanceIdProp.schemaType()
        );
    }

    @Test
    void testAdditionalProperties() {
        assertTrue(schema.getBaseInSection().additionalProperties());
        assertTrue(schema.getOutSection().additionalProperties());
    }

    @Test
    void testMultiKeyDiscriminatorKeys() {
        var keys = multiKeySchema.getDiscriminatorKeys();
        assertTrue(keys.contains("action"));
        assertTrue(keys.contains("mode"));
    }

    @Test
    void testMultiKeyConditional_allKeysMatch() {
        // Both action=process AND mode=sync -> timeout should be present
        var section = multiKeySchema.resolveInSection(Map.of("action", "process", "mode", "sync"));
        assertTrue(section.properties().containsKey("timeout"));
    }

    @Test
    void testMultiKeyConditional_partialMatch() {
        // Only action=process without mode -> timeout should NOT be present (AND semantics)
        var section = multiKeySchema.resolveInSection(Map.of("action", "process"));
        assertFalse(section.properties().containsKey("timeout"));
    }

    @Test
    void testMultiKeyConditional_singleKeyConditional() {
        // action=upload -> destination should be present (single-key conditional still works)
        var section = multiKeySchema.resolveInSection(Map.of("action", "upload"));
        assertTrue(section.properties().containsKey("destination"));
    }

    @Test
    void testOneOfWithRefAlternatives() {
        var section = refCompositeSchema.getBaseInSection();
        var prop = section.properties().get("oneOfWithRef");
        assertNotNull(prop);
        assertEquals(
                new SchemaType.Composite(List.of(
                        new SchemaType.Scalar("string"),
                        new SchemaType.Array("string")
                )),
                prop.schemaType()
        );
    }

    @Test
    void testAnyOfWithRefAlternatives() {
        var section = refCompositeSchema.getBaseInSection();
        var prop = section.properties().get("anyOfWithRef");
        assertNotNull(prop);
        assertEquals(
                new SchemaType.Composite(List.of(
                        new SchemaType.Scalar("string"),
                        new SchemaType.Scalar("integer")
                )),
                prop.schemaType()
        );
    }

    @Test
    void testArrayItemsWithRef() {
        var section = refCompositeSchema.getBaseInSection();
        var prop = section.properties().get("arrayWithRefItems");
        assertNotNull(prop);
        assertEquals(new SchemaType.Array("string"), prop.schemaType());
    }

    @Test
    void testOneOfMixedRefAndInline() {
        var section = refCompositeSchema.getBaseInSection();
        var prop = section.properties().get("oneOfMixed");
        assertNotNull(prop);
        assertEquals(
                new SchemaType.Composite(List.of(
                        new SchemaType.Enum(List.of("active", "inactive")),
                        new SchemaType.Scalar("boolean")
                )),
                prop.schemaType()
        );
    }

    @Test
    void testOneOfWithChainedRef() {
        // nestedRef -> $ref -> stringType, should resolve through the chain
        var section = refCompositeSchema.getBaseInSection();
        var prop = section.properties().get("oneOfNestedRef");
        assertNotNull(prop);
        assertEquals(
                new SchemaType.Composite(List.of(
                        new SchemaType.Scalar("string"),
                        new SchemaType.Scalar("integer")
                )),
                prop.schemaType()
        );
    }

    @Test
    void testConstEnumWithDescriptions() {
        var section = refCompositeSchema.getBaseInSection();
        var prop = section.properties().get("constEnumWithDesc");
        assertNotNull(prop);
        assertInstanceOf(SchemaType.Enum.class, prop.schemaType());
        var enumType = (SchemaType.Enum) prop.schemaType();
        assertEquals(List.of("read", "write", "admin"), enumType.values());
        assertEquals(List.of("Read access", "Write access", "Full access"), enumType.descriptions());
    }

    @Test
    void testConstEnumWithoutDescriptions() {
        var section = refCompositeSchema.getBaseInSection();
        var prop = section.properties().get("constEnumNoDesc");
        assertNotNull(prop);
        assertInstanceOf(SchemaType.Enum.class, prop.schemaType());
        var enumType = (SchemaType.Enum) prop.schemaType();
        assertEquals(List.of("low", "medium", "high"), enumType.values());
        assertTrue(enumType.descriptions().isEmpty());
    }

    @Test
    void testConstEnumPartialDescriptions() {
        var section = refCompositeSchema.getBaseInSection();
        var prop = section.properties().get("constEnumMixedDesc");
        assertNotNull(prop);
        assertInstanceOf(SchemaType.Enum.class, prop.schemaType());
        var enumType = (SchemaType.Enum) prop.schemaType();
        assertEquals(List.of("fast", "slow"), enumType.values());
        assertEquals(List.of("Fast mode", ""), enumType.descriptions());
    }

    @Test
    void testOneOfMixedConstAndType_notEnum() {
        // oneOf with const + type is NOT a const-enum, should be Composite
        var section = refCompositeSchema.getBaseInSection();
        var prop = section.properties().get("oneOfMixedConstAndType");
        assertNotNull(prop);
        assertInstanceOf(SchemaType.Composite.class, prop.schemaType());
    }

    @Test
    void testOutSectionOneOfWithRef() {
        var out = refCompositeSchema.getOutSection();
        var prop = out.properties().get("result");
        assertNotNull(prop);
        assertEquals(
                new SchemaType.Composite(List.of(
                        new SchemaType.Scalar("string"),
                        new SchemaType.Scalar("integer")
                )),
                prop.schemaType()
        );
    }

    @Test
    void testTaskDescription() {
        assertEquals("Schema for the concord task - start/fork processes, manage API keys", schema.getDescription());
    }

    @Test
    void testStrictTaskDescription() {
        assertEquals("An HTTP client task for making web requests", strictSchema.getDescription());
    }

    @Test
    void testPropertyDescription() {
        var base = schema.getBaseInSection();
        var actionProp = base.properties().get("action");
        assertNotNull(actionProp);
        assertEquals("The action to perform", actionProp.description());
    }

    @Test
    void testPropertyWithoutType() {
        // "meta" in concord.schema.json has description but no type -> SchemaType.Any
        var section = schema.resolveInSection(Map.of("action", "start"));
        var metaProp = section.properties().get("meta");
        assertNotNull(metaProp);
        assertInstanceOf(SchemaType.Any.class, metaProp.schemaType());
        assertEquals("Metadata", metaProp.description());
    }

    @Test
    void testStrictAdditionalProperties() {
        assertFalse(strictSchema.getBaseInSection().additionalProperties());
        assertFalse(strictSchema.getOutSection().additionalProperties());
    }

    @Test
    void testStrictRequiredProperties() {
        var base = strictSchema.getBaseInSection();
        assertTrue(base.requiredFields().contains("url"));
        assertFalse(base.requiredFields().contains("method"));
        assertFalse(base.requiredFields().contains("debug"));
    }

    @Test
    void testPropertyRequiredFlag() {
        var base = strictSchema.getBaseInSection();
        var urlProp = base.properties().get("url");
        assertNotNull(urlProp);
        assertTrue(urlProp.required());

        var methodProp = base.properties().get("method");
        assertNotNull(methodProp);
        assertFalse(methodProp.required());

        var debugProp = base.properties().get("debug");
        assertNotNull(debugProp);
        assertFalse(debugProp.required());
    }

    // --- nested object tests ---

    @Test
    void testNestedObjectType() {
        var base = nestedObjectSchema.getBaseInSection();
        var authProp = base.properties().get("auth");
        assertNotNull(authProp);
        assertInstanceOf(SchemaType.Object.class, authProp.schemaType());

        var authObj = (SchemaType.Object) authProp.schemaType();
        var authSection = authObj.section();
        assertTrue(authSection.properties().containsKey("basic"));
        assertTrue(authSection.properties().containsKey("token"));
        assertFalse(authSection.additionalProperties());
    }

    @Test
    void testDeepNestedObjectType() {
        var base = nestedObjectSchema.getBaseInSection();
        var authObj = (SchemaType.Object) base.properties().get("auth").schemaType();
        var basicProp = authObj.section().properties().get("basic");
        assertNotNull(basicProp);
        assertInstanceOf(SchemaType.Object.class, basicProp.schemaType());

        var basicObj = (SchemaType.Object) basicProp.schemaType();
        var basicSection = basicObj.section();
        assertTrue(basicSection.properties().containsKey("username"));
        assertTrue(basicSection.properties().containsKey("password"));
        assertTrue(basicSection.requiredFields().contains("username"));
        assertTrue(basicSection.requiredFields().contains("password"));
        assertFalse(basicSection.additionalProperties());
    }

    @Test
    void testFreeFormObjectRemainsScalar() {
        var base = nestedObjectSchema.getBaseInSection();
        var freeFormProp = base.properties().get("freeFormObject");
        assertNotNull(freeFormProp);
        assertEquals(new SchemaType.Scalar("object"), freeFormProp.schemaType());
    }

    @Test
    void testNestedObjectViaRef() {
        var base = nestedObjectSchema.getBaseInSection();
        var proxyProp = base.properties().get("proxy");
        assertNotNull(proxyProp);
        assertInstanceOf(SchemaType.Object.class, proxyProp.schemaType());

        var proxyObj = (SchemaType.Object) proxyProp.schemaType();
        var proxySection = proxyObj.section();
        assertTrue(proxySection.properties().containsKey("host"));
        assertTrue(proxySection.properties().containsKey("port"));
        assertTrue(proxySection.requiredFields().contains("host"));
        assertFalse(proxySection.additionalProperties());

        var hostProp = proxySection.properties().get("host");
        assertEquals(new SchemaType.Scalar("string"), hostProp.schemaType());
        var portProp = proxySection.properties().get("port");
        assertEquals(new SchemaType.Scalar("integer"), portProp.schemaType());
    }

    @Test
    void testNestedObjectInOneOf() {
        var base = nestedObjectSchema.getBaseInSection();
        var credsProp = base.properties().get("credentials");
        assertNotNull(credsProp);
        assertInstanceOf(SchemaType.Composite.class, credsProp.schemaType());

        var composite = (SchemaType.Composite) credsProp.schemaType();
        assertEquals(2, composite.alternatives().size());
        assertEquals(new SchemaType.Scalar("string"), composite.alternatives().get(0));

        var objAlt = composite.alternatives().get(1);
        assertInstanceOf(SchemaType.Object.class, objAlt);
        var objSection = ((SchemaType.Object) objAlt).section();
        assertTrue(objSection.properties().containsKey("user"));
        assertTrue(objSection.properties().containsKey("pass"));
        assertTrue(objSection.requiredFields().contains("user"));
        assertTrue(objSection.requiredFields().contains("pass"));
        assertFalse(objSection.additionalProperties());
    }

    @Test
    void testExistingArgumentsStillScalarObject() {
        // arguments in concord schema has no nested properties -> stays Scalar("object")
        var section = schema.resolveInSection(Map.of("action", "start"));
        var argumentsProp = section.properties().get("arguments");
        assertNotNull(argumentsProp);
        assertEquals(new SchemaType.Scalar("object"), argumentsProp.schemaType());
    }
}
