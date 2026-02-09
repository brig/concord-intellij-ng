package brig.concord.navigation;

import brig.concord.ConcordYamlTestBaseJunit5;
import com.intellij.openapi.application.ReadAction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class FindUsageTest extends ConcordYamlTestBaseJunit5 {

    @Test
    void findUsageTest() {
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

        Assertions.assertEquals(1, usageInfos.size());
    }
}
