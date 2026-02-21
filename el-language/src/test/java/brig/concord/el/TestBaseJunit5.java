// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.el;

import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory;
import com.intellij.testFramework.fixtures.impl.LightTempDirTestFixtureImpl;
import com.intellij.testFramework.junit5.RunInEdt;
import com.intellij.testFramework.junit5.impl.TestApplicationExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

public abstract class TestBaseJunit5 {

    protected CodeInsightTestFixture myFixture;

    @BeforeEach
    protected void setUp() throws Exception {
        var factory = IdeaTestFixtureFactory.getFixtureFactory();
        var builder = factory.createLightFixtureBuilder(new LightProjectDescriptor(), "test");
        var projectFixture = builder.getFixture();
        myFixture = factory.createCodeInsightFixture(projectFixture, new LightTempDirTestFixtureImpl(true));
        myFixture.setUp();
    }

    @AfterEach
    protected void tearDown() throws Exception {
        try {
            myFixture.tearDown();
        } finally {
            myFixture = null;
            // LightIdeaTestFixtureImpl.tearDown() disposes the project via forceCloseProject()
            // but does NOT clear the static LightPlatformTestCase.ourProject reference.
            // TestApplicationExtension's LeakHunter detects this as a project leak.
            // closeAndDeleteProject() clears the static reference in its finally block.
            // Use reflection to call LightPlatformTestCase.closeAndDeleteProject()
            // because LightPlatformTestCase extends JUnit 4's TestCase which is not on the compile classpath
            // (junit:junit is testRuntimeOnly), so a direct import would fail to compile.
            try {
                var cls = Class.forName("com.intellij.testFramework.LightPlatformTestCase");
                var method = cls.getMethod("closeAndDeleteProject");
                method.invoke(null);
            } catch (AssertionError ignored) {
                // expected: project already closed by fixture teardown
            } catch (java.lang.reflect.InvocationTargetException e) {
                if (!(e.getCause() instanceof AssertionError)) {
                    throw new RuntimeException(e);
                }
                // expected: project already closed by fixture teardown
            } catch (Exception ignored) {
                // class/method not found — safe to ignore
            }
        }
    }
}
