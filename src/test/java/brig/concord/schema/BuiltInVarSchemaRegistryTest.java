package brig.concord.schema;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BuiltInVarSchemaRegistryTest {

    @Test
    void testInitiatorSchemaLoads() {
        var section = BuiltInVarSchemaRegistry.getSchema("initiator");
        assertNotNull(section);
        assertTrue(section.properties().containsKey("displayName"));
        assertTrue(section.properties().containsKey("email"));
        assertTrue(section.properties().containsKey("username"));
        assertTrue(section.properties().containsKey("userDomain"));
        assertTrue(section.properties().containsKey("groups"));
        assertTrue(section.properties().containsKey("attributes"));
    }

    @Test
    void testCurrentUserSchemaLoads() {
        var section = BuiltInVarSchemaRegistry.getSchema("currentUser");
        assertNotNull(section);
        assertTrue(section.properties().containsKey("displayName"));
        assertTrue(section.properties().containsKey("email"));
    }

    @Test
    void testProjectInfoSchemaLoads() {
        var section = BuiltInVarSchemaRegistry.getSchema("projectInfo");
        assertNotNull(section);
        assertTrue(section.properties().containsKey("orgName"));
        assertTrue(section.properties().containsKey("projectName"));
        assertTrue(section.properties().containsKey("repoUrl"));
    }

    @Test
    void testProcessInfoSchemaLoads() {
        var section = BuiltInVarSchemaRegistry.getSchema("processInfo");
        assertNotNull(section);
        assertTrue(section.properties().containsKey("activeProfiles"));
        assertTrue(section.properties().containsKey("sessionToken"));
    }

    @Test
    void testRequestInfoSchemaLoads() {
        var section = BuiltInVarSchemaRegistry.getSchema("requestInfo");
        assertNotNull(section);
        assertTrue(section.properties().containsKey("requestId"));
        assertTrue(section.properties().containsKey("ip"));
        assertTrue(section.properties().containsKey("headers"));
    }

    @Test
    void testScalarVarNoSchema() {
        var section = BuiltInVarSchemaRegistry.getSchema("txId");
        assertNull(section);
    }

    @Test
    void testUnknownVarNoSchema() {
        var section = BuiltInVarSchemaRegistry.getSchema("nonExistent");
        assertNull(section);
    }
}
