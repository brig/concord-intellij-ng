package brig.concord.navigation;

import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.intellij.usages.Usage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;

public class FindUsageTest extends BasePlatformTestCase {

    @BeforeEach
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @AfterEach
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Override
    protected String getTestDataPath() {
        return "./src/test/resources/findUsage";
    }

    @Test
    public void findUsageTest() {
        Collection<Usage> usageInfos = myFixture.testFindUsagesUsingAction("concord.yaml");
        assertEquals(1, usageInfos.size());
    }
}
