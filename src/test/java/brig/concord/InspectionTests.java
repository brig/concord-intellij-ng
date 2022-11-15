package brig.concord;

import java.net.URI;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import brig.concord.inspection.MissingKeysInspection;
import brig.concord.inspection.UnknownKeysInspection;
import brig.concord.inspection.ValueInspection;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class InspectionTests extends BasePlatformTestCase {

    protected InspectionUtil inspection;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        myFixture.enableInspections(List.of(MissingKeysInspection.class, UnknownKeysInspection.class, ValueInspection.class));

        inspection = new InspectionUtil(myFixture);
    }

    @Override
    protected String getTestDataPath() {
        return "./src/test/resources";
    }

    @Test
    public void testCheckpoint_000() throws Exception {
        configureByFile("errors/checkpoint/000.concord.yml");
        inspection.assertHasError("Value is required");
    }

    @Test
    public void testCheckpoint_001() throws Exception {
        configureByFile("errors/checkpoint/001.concord.yml");
        inspection.assertHasError("Value is required");
    }

    private void configureByFile(String resource) throws Exception {
        myFixture.configureByFile(resource);
    }
}
