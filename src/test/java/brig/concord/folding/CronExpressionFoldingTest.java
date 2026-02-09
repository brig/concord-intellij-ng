package brig.concord.folding;

import brig.concord.ConcordYamlTestBaseJunit5;
import brig.concord.assertions.FoldRegionAssert;
import com.cronutils.descriptor.CronDescriptor;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.parser.CronParser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Locale;

public class CronExpressionFoldingTest extends ConcordYamlTestBaseJunit5 {

    private Locale previousLocale;

    @Override
    @BeforeEach
    protected void setUp() throws Exception {
        super.setUp();
        previousLocale = Locale.getDefault();
        Locale.setDefault(Locale.US);
    }

    @Override
    @AfterEach
    protected void tearDown() throws Exception {
        Locale.setDefault(previousLocale);
        super.tearDown();
    }

    @Test
    public void testCronSpecFoldingDescriptor() {
        configureFromText("""
                triggers:
                  - cron:
                      spec: "0 12 * * *"
                """);

        var parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX));
        var descriptor = CronDescriptor.instance(Locale.getDefault());
        var expected = descriptor.describe(parser.parse("0 12 * * *"));

        foldRegion(value("triggers[0]/cron/spec"))
                .assertPlaceholderText(expected);
    }

    @Test
    public void testInvalidCronPlaceholder() {
        configureFromText("""
                triggers:
                  - cron:
                      spec: "70 0 * * *"
                """);

        foldRegion(value("triggers[0]/cron/spec"))
                .assertPlaceholderText("invalid cron expression");
    }

    private FoldRegionAssert foldRegion(ValueTarget target) {
        return FoldRegionAssert.foldRegion(myFixture, target);
    }
}
