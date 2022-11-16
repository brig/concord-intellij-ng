package brig.concord;

import brig.concord.inspection.MissingKeysInspection;
import brig.concord.inspection.UnknownKeysInspection;
import brig.concord.inspection.ValueInspection;
import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.util.List;
import java.util.stream.Collectors;

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

    protected static String dump(List<HighlightInfo> highlighting) {
        return "------ highlighting ------\n"
                + highlighting.stream().map(HighlightInfo::toString).collect(Collectors.joining("\n"))
                + "\n------ highlighting ------";
    }
}
