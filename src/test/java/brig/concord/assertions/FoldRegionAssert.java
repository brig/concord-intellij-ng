// SPDX-License-Identifier: Apache-2.0
package brig.concord.assertions;

import brig.concord.ConcordYamlTestBaseJunit5;
import com.intellij.codeInsight.folding.CodeFoldingManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.editor.FoldRegion;
import com.intellij.openapi.util.TextRange;
import com.intellij.testFramework.EdtTestUtil;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import org.junit.jupiter.api.Assertions;

public class FoldRegionAssert {

    private final FoldRegion region;

    public static FoldRegionAssert foldRegion(CodeInsightTestFixture fixture, ConcordYamlTestBaseJunit5.AbstractTarget target) {
        updateFolding(fixture);
        return new FoldRegionAssert(findRegionForRange(fixture, target.range()));
    }

    public static void assertNoFoldRegion(CodeInsightTestFixture fixture, ConcordYamlTestBaseJunit5.AbstractTarget target) {
        updateFolding(fixture);
        var region = findRegionForRange(fixture, target.range());
        Assertions.assertNull(region, "Expected NO fold region at " + target.path() + " but found one");
    }

    private FoldRegionAssert(FoldRegion region) {
        Assertions.assertNotNull(region, "Expected a fold region but none found");
        this.region = region;
    }

    public FoldRegionAssert assertPlaceholderText(String expected) {
        Assertions.assertEquals(expected, region.getPlaceholderText());
        return this;
    }

    private static void updateFolding(CodeInsightTestFixture fixture) {
        EdtTestUtil.runInEdtAndWait(() -> {
            var editor = fixture.getEditor();
            editor.getFoldingModel().runBatchFoldingOperation(() ->
                    CodeFoldingManager.getInstance(fixture.getProject()).updateFoldRegions(editor)
            );
        });
    }

    private static FoldRegion findRegionForRange(CodeInsightTestFixture fixture, TextRange range) {
        return ReadAction.compute(() -> {
            var editor = fixture.getEditor();
            var regions = editor.getFoldingModel().getAllFoldRegions();
            for (var region : regions) {
                if (region.getStartOffset() == range.getStartOffset()
                        && region.getEndOffset() == range.getEndOffset()) {
                    return region;
                }
            }
            return null;
        });
    }
}
