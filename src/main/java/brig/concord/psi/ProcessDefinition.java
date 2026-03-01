// SPDX-License-Identifier: Apache-2.0
package brig.concord.psi;

import brig.concord.navigation.FlowNamesIndex;
import com.intellij.openapi.actionSystem.ex.ActionUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.CommonProcessors;
import com.intellij.util.indexing.FileBasedIndex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import brig.concord.yaml.psi.*;

import java.util.*;

import static brig.concord.meta.ConcordFileMetaType.FLOWS_KEY;

public class ProcessDefinition {

    private static final Key<CachedValue<Map<String, YAMLKeyValue>>> FLOW_MAP_KEY =
            Key.create("concord.flow.definitions.map");

    private final PsiElement element;

    public ProcessDefinition(PsiElement element) {
        this.element = element;
    }

    /**
     * Finds the enclosing flow definition for the given element.
     * Traverses up the PSI tree until it finds a YAMLKeyValue that is a flow definition.
     *
     * @param element any PSI element within a flow
     * @return the flow definition YAMLKeyValue, or null if not found
     */
    public static @Nullable YAMLKeyValue findEnclosingFlowDefinition(@Nullable PsiElement element) {
        var current = element;
        while (current != null) {
            if (current instanceof YAMLKeyValue kv && isFlowDefinition(kv)) {
                return kv;
            }
            current = current.getParent();
        }
        return null;
    }

    /**
     * Checks if the given key-value element is a flow definition.
     * A flow definition is a direct child of the root-level 'flows' mapping.
     * Structure: YAMLDocument -> YAMLMapping -> YAMLKeyValue(flows) -> YAMLMapping -> YAMLKeyValue(flow definition)
     */
    public static boolean isFlowDefinition(@NotNull YAMLKeyValue keyValue) {
        var parent = keyValue.getParent();
        if (!(parent instanceof YAMLMapping)) {
            return false;
        }

        var flowsKv = parent.getParent();
        if (!(flowsKv instanceof YAMLKeyValue flowsKeyValue)) {
            return false;
        }

        var rootMapping = flowsKv.getParent();
        if (!(rootMapping instanceof YAMLMapping)) {
            return false;
        }

        if (!(rootMapping.getParent() instanceof YAMLDocument)) {
            return false;
        }

        return FLOWS_KEY.equals(flowsKeyValue.getKeyText());
    }

    @Nullable
    public PsiElement flow(String name) {
        var flows = flows(name);
        return flows.isEmpty() ? null : flows.getFirst();
    }

    /**
     * Returns all flow definitions with the given name in the current scope.
     * Multiple flows with the same name can exist across different files in the scope.
     *
     * @param name the flow name to search for
     * @return list of all flow definition elements (YAMLKeyValue) with this name
     */
    public List<PsiElement> flows(String name) {
        var project = element.getProject();
        if (ActionUtil.isDumbMode(project)) {
            return List.of();
        }

        var scope = ConcordScopeService.getInstance(project).createSearchScope(element);

        var files = new CommonProcessors.CollectProcessor<VirtualFile>();
        FileBasedIndex.getInstance().getFilesWithKey(FlowNamesIndex.KEY,
                Collections.singleton(name),
                files,
                scope);

        List<PsiElement> result = new ArrayList<>();
        for (var file : files.getResults()) {
            var psiFile = PsiManager.getInstance(project).findFile(file);
            if (!(psiFile instanceof ConcordFile cf)) {
                continue;
            }

            var flowKv = getFlowDefinitions(cf).get(name);
            if (flowKv != null) {
                result.add(flowKv);
            }
        }

        return result;
    }

    public Set<String> flowNames() {
        var project = element.getProject();
        if (ActionUtil.isDumbMode(project)) {
            return Collections.emptySet();
        }

        var scope = ConcordScopeService.getInstance(project).createSearchScope(element);

        return ApplicationManager.getApplication().runReadAction((Computable<Set<String>>) () -> {
            var result = new HashSet<String>();
            FileBasedIndex.getInstance().processAllKeys(FlowNamesIndex.KEY, key -> {
                // Ensure the key is actually in the scope.
                // processValues returns false if the processor returns false (which we do on the first hit).
                var isInScope = !FileBasedIndex.getInstance().processValues(FlowNamesIndex.KEY, key, null, (file, value) -> false, scope);
                if (isInScope) {
                    result.add(key);
                }
                return true;
            }, scope, null);
            return result;
        });
    }

    /**
     * Returns a cached map of flow name → flow definition KV for the given file.
     * Built once per file modification, then all flow lookups are O(1).
     */
    static @NotNull Map<String, YAMLKeyValue> getFlowDefinitions(@NotNull ConcordFile file) {
        return CachedValuesManager.getCachedValue((PsiElement) file, FLOW_MAP_KEY, () -> {
            var flowsKv = file.flows().orElse(null);
            if (flowsKv == null) {
                return CachedValueProvider.Result.create(Map.of(), (PsiElement) file);
            }
            var flowsValue = flowsKv.getValue();
            if (!(flowsValue instanceof YAMLMapping mapping)) {
                return CachedValueProvider.Result.create(Map.of(), (PsiElement) file);
            }
            var map = new HashMap<String, YAMLKeyValue>();
            for (var child = mapping.getFirstChild(); child != null; child = child.getNextSibling()) {
                if (child instanceof YAMLKeyValue kv) {
                    map.putIfAbsent(kv.getKeyText(), kv);
                }
            }
            return CachedValueProvider.Result.create(Collections.unmodifiableMap(map), (PsiElement) file);
        });
    }
}
