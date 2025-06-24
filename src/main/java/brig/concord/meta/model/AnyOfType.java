package brig.concord.meta.model;

import brig.concord.ConcordBundle;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import org.jetbrains.annotations.NotNull;
import brig.concord.yaml.meta.model.YamlAnyOfType;
import brig.concord.yaml.meta.model.YamlMetaType;
import brig.concord.yaml.meta.model.YamlScalarType;
import brig.concord.yaml.psi.YAMLScalar;
import brig.concord.yaml.psi.YAMLValue;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AnyOfType extends YamlAnyOfType {

    public static AnyOfType anyOf(YamlMetaType... types) {
        String name = "AnyOf[" + Stream.of(types).map(YamlMetaType::getDisplayName).collect(Collectors.joining()) + "]";
        return new AnyOfType(name, flattenTypes(types));
    }

    protected AnyOfType(@NotNull String typeName, List<YamlMetaType> types) {
        super(typeName, types);
    }

    @Override
    public void validateValue(@NotNull YAMLValue value, @NotNull ProblemsHolder problemsHolder) {
        List<YamlMetaType> types;
        if (value instanceof YAMLScalar) {
            types = listScalarSubTypes();
            if (types.isEmpty()) {
                // value kind does not match, let some non scalar component to report it
                types = Collections.singletonList(listNonScalarSubTypes().get(0));
            }
        }
        else {
            types = listNonScalarSubTypes();
            if (types.isEmpty()) {
                // only scalar components, let one of them report it
                types = Collections.singletonList(listScalarSubTypes().get(0));
            }
        }

//        List<ProblemsHolder> problems = new SmartList<>();
        for (YamlMetaType nextType : types) {
            ProblemsHolder nextHolder = makeCopy(problemsHolder);
            nextType.validateValue(value, nextHolder);
            if (!nextHolder.hasResults()) {
                return;
            }
//            problems.add(nextHolder);
        }

//        problems.stream()
//                .flatMap(h -> h.getResults().stream())
//                .forEach(problemsHolder::registerProblem);

        problemsHolder.registerProblem(value, ConcordBundle.message("invalid.value", expectedString()), ProblemHighlightType.ERROR);
    }

    public boolean isScalar() {
        return streamSubTypes().allMatch(t -> t instanceof YamlScalarType);
    }

    public String expectedString() {
        return streamSubTypes().map(YamlMetaType::getDisplayName).collect(Collectors.joining("|"));
    }
}
