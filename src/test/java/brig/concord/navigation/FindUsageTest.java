package brig.concord.navigation;

import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.intellij.usages.Usage;
import com.intellij.usages.UsageInfo2UsageAdapter;
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
        var usageInfos = myFixture.testFindUsagesUsingAction("concord.yaml");
        for (var usage : usageInfos) {
            System.out.println(usage);
            System.out.println(usage.getClass());
            if (usage instanceof UsageInfo2UsageAdapter uia) {
                System.out.println(uia.getFile());

                var f = uia.getFile();
                if (f != null) {
                    ReadAction.run(() -> {
                        var doc = FileDocumentManager.getInstance().getDocument(f);
                        if (doc != null) {
                            var text = doc.getText();
                            System.out.println("text: " + text);
                        }
                    });
                }
            }
        }
        assertEquals(1, usageInfos.size());
    }
}
