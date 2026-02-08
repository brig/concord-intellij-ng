package brig.concord.schema;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class TaskSchemaParserTest {

    private TaskSchema schema;
    private TaskSchema multiKeySchema;
    private TaskSchema refCompositeSchema;
    private TaskSchema strictSchema;

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
    }

    @Test
    public void testBasicParsing() {
        assertEquals("concord", schema.getTaskName());
        assertNotNull(schema.getBaseInSection());
        assertNotNull(schema.getOutSection());
        assertFalse(schema.getInConditionals().isEmpty());
    }

    @Test
    public void testBaseInProperties() {
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
    public void testDiscriminatorKeys() {
        var keys = schema.getDiscriminatorKeys();
        assertTrue(keys.contains("action"));
    }

    @Test
    public void testStartConditional() {
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
    public void testStartExternalConditional() {
        var section = schema.resolveInSection(Map.of("action", "startExternal"));
        assertTrue(section.properties().containsKey("project"));
        assertTrue(section.properties().containsKey("baseUrl"));
        assertTrue(section.properties().containsKey("apiKey"));
        // startExternal requires baseUrl and apiKey
        assertTrue(section.requiredFields().contains("baseUrl"));
        assertTrue(section.requiredFields().contains("apiKey"));
    }

    @Test
    public void testForkConditional() {
        var section = schema.resolveInSection(Map.of("action", "fork"));
        assertTrue(section.properties().containsKey("forks"));
        assertTrue(section.properties().containsKey("entryPoint"));
        assertTrue(section.properties().containsKey("sync"));
    }

    @Test
    public void testKillConditional() {
        var section = schema.resolveInSection(Map.of("action", "kill"));
        assertTrue(section.properties().containsKey("instanceId"));
        assertTrue(section.properties().containsKey("sync"));
        assertTrue(section.requiredFields().contains("instanceId"));
    }

    @Test
    public void testCreateApiKeyConditional() {
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
    public void testCreateOrUpdateApiKeyConditional() {
        // Uses enum: ["createApiKey", "createOrUpdateApiKey"] so both should match
        var section = schema.resolveInSection(Map.of("action", "createOrUpdateApiKey"));
        assertTrue(section.properties().containsKey("userId"));
        assertTrue(section.properties().containsKey("username"));
    }

    @Test
    public void testNoMatchingConditional() {
        // Unknown action: only base properties
        var section = schema.resolveInSection(Map.of("action", "unknown"));
        assertTrue(section.properties().containsKey("action"));
        assertFalse(section.properties().containsKey("project"));
        assertFalse(section.properties().containsKey("instanceId"));
    }

    @Test
    public void testEmptyValues() {
        var section = schema.resolveInSection(Map.of());
        // Only base properties
        assertEquals(schema.getBaseInSection().properties().size(), section.properties().size());
    }

    @Test
    public void testOutSection() {
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
    public void testPropertyTypes() {
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
    public void testOneOfPolymorphicType() {
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
    public void testAdditionalProperties() {
        assertTrue(schema.getBaseInSection().additionalProperties());
        assertTrue(schema.getOutSection().additionalProperties());
    }

    @Test
    public void testMultiKeyDiscriminatorKeys() {
        var keys = multiKeySchema.getDiscriminatorKeys();
        assertTrue(keys.contains("action"));
        assertTrue(keys.contains("mode"));
    }

    @Test
    public void testMultiKeyConditional_allKeysMatch() {
        // Both action=process AND mode=sync → timeout should be present
        var section = multiKeySchema.resolveInSection(Map.of("action", "process", "mode", "sync"));
        assertTrue(section.properties().containsKey("timeout"));
    }

    @Test
    public void testMultiKeyConditional_partialMatch() {
        // Only action=process without mode → timeout should NOT be present (AND semantics)
        var section = multiKeySchema.resolveInSection(Map.of("action", "process"));
        assertFalse(section.properties().containsKey("timeout"));
    }

    @Test
    public void testMultiKeyConditional_singleKeyConditional() {
        // action=upload → destination should be present (single-key conditional still works)
        var section = multiKeySchema.resolveInSection(Map.of("action", "upload"));
        assertTrue(section.properties().containsKey("destination"));
    }

    @Test
    public void testOneOfWithRefAlternatives() {
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
    public void testAnyOfWithRefAlternatives() {
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
    public void testArrayItemsWithRef() {
        var section = refCompositeSchema.getBaseInSection();
        var prop = section.properties().get("arrayWithRefItems");
        assertNotNull(prop);
        assertEquals(new SchemaType.Array("string"), prop.schemaType());
    }

    @Test
    public void testOneOfMixedRefAndInline() {
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
    public void testOneOfWithChainedRef() {
        // nestedRef → $ref → stringType, should resolve through the chain
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
    public void testOutSectionOneOfWithRef() {
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
    public void testPropertyDescription() {
        var base = schema.getBaseInSection();
        var actionProp = base.properties().get("action");
        assertNotNull(actionProp);
        assertEquals("The action to perform", actionProp.description());
    }

    @Test
    public void testPropertyWithoutType() {
        // "meta" in concord.schema.json has description but no type → SchemaType.Any
        var section = schema.resolveInSection(Map.of("action", "start"));
        var metaProp = section.properties().get("meta");
        assertNotNull(metaProp);
        assertInstanceOf(SchemaType.Any.class, metaProp.schemaType());
        assertEquals("Metadata", metaProp.description());
    }

    @Test
    public void testStrictAdditionalProperties() {
        assertFalse(strictSchema.getBaseInSection().additionalProperties());
        assertFalse(strictSchema.getOutSection().additionalProperties());
    }

    @Test
    public void testStrictRequiredProperties() {
        var base = strictSchema.getBaseInSection();
        assertTrue(base.requiredFields().contains("url"));
        assertFalse(base.requiredFields().contains("method"));
        assertFalse(base.requiredFields().contains("debug"));
    }

    @Test
    public void testPropertyRequiredFlag() {
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
}