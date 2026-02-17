// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.inspection;

import brig.concord.ConcordYamlTestBaseJunit5;
import brig.concord.assertions.InspectionAssertions;
import com.intellij.codeInspection.LocalInspectionTool;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;

import java.util.Collection;

public abstract class InspectionTestBase extends ConcordYamlTestBaseJunit5 {

    @Override
    @BeforeEach
    protected void setUp() throws Exception {
        super.setUp();
        myFixture.enableInspections(enabledInspections());
    }

    protected abstract Collection<Class<? extends LocalInspectionTool>> enabledInspections();

    protected void assertNoWarnings() {
        InspectionAssertions.assertNoWarnings(this.myFixture);
    }

    protected void assertNoErrors() {
        InspectionAssertions.assertNoErrors(this.myFixture);
    }

    protected @NotNull InspectionAssertions inspection(@NotNull ConcordYamlTestBaseJunit5.AbstractTarget target) {
        return new InspectionAssertions(myFixture, target);
    }

    protected @NotNull InspectionAssertions inspection() {
        return new InspectionAssertions(myFixture);
    }
}
