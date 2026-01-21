package brig.concord.navigation;

import brig.concord.ConcordYamlTestBase;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.intellij.usages.Usage;
import com.intellij.usages.UsageInfo2UsageAdapter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;

public class FindUsageTest extends ConcordYamlTestBase {

    @Override
    protected String getTestDataPath() {
        return "./src/test/resources/findUsage";
    }

    @Test
    public void findUsageTest() {
        configureFromResource("/findUsage/concord.yaml");


        var usageInfos = ReadAction.compute(() -> {
            var target = myFixture.getElementAtCaret();
            var result = myFixture.findUsages(target);
            for (var usage : result) {
                System.out.println(">> usage: " + usage);
                System.out.println("file: " + usage.getFile());
                System.out.println("firtual file: " + usage.getVirtualFile());
                System.out.println("element: " + usage.getElement());
            }
            return result;
        });

        assertEquals(1, usageInfos.size());
    }
}
