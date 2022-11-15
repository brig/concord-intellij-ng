package brig.concord;

import brig.concord.inspection.MissingKeysInspection;
import brig.concord.inspection.UnknownKeysInspection;
import brig.concord.inspection.ValueInspection;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.util.List;

public abstract class BaseTest extends BasePlatformTestCase {

    @Override
    protected String getTestDataPath() {
        return "./src/test/resources";
    }

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        myFixture.enableInspections(List.of(MissingKeysInspection.class, UnknownKeysInspection.class, ValueInspection.class));
    }

    @AfterEach
    protected void tearDown() {
        try{
            super.tearDown();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected void configureByFile(String resource) {
        myFixture.configureByFile(resource);
    }
}
