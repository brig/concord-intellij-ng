package brig.concord.navigation;

import brig.concord.ConcordBundle;
import brig.concord.psi.ConcordScopeService;
import com.intellij.ide.actions.SearchEverywherePsiRenderer;
import com.intellij.ide.actions.searcheverywhere.SearchEverywhereContributor;
import com.intellij.ide.actions.searcheverywhere.SearchEverywhereContributorFactory;
import com.intellij.ide.actions.searcheverywhere.SearchEverywhereManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.ex.ActionUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.util.ProgressIndicatorUtils;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.ProjectScope;
import com.intellij.util.CommonProcessors;
import com.intellij.util.Processor;
import com.intellij.util.TextWithIcon;
import com.intellij.util.indexing.FileBasedIndex;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class FlowDefinitionSearchEverywhereContributor implements SearchEverywhereContributor<FlowDefinitionNavigationItem> {

    private final Project myProject;

    public FlowDefinitionSearchEverywhereContributor(Project project) {
        myProject = project;
    }

    @NotNull
    @Override
    public String getSearchProviderId() {
        return getClass().getSimpleName();
    }

    @NotNull
    @Override
    public String getGroupName() {
        return ConcordBundle.message("FlowDefinitionSearchEverywhereContributor.group.name");
    }

    @Override
    public int getSortWeight() {
        return 0;
    }

    @Override
    public boolean showInFindResults() {
        return true;
    }

    @Override
    public void fetchElements(@NotNull String pattern,
                              @NotNull ProgressIndicator progressIndicator,
                              @NotNull Processor<? super FlowDefinitionNavigationItem> consumer) {
        if (myProject == null || pattern.isEmpty()) {
            return;
        }

        Runnable task = () -> findKeys(consumer, pattern, progressIndicator);
        var application = ApplicationManager.getApplication();
        if (application.isUnitTestMode()) {
            application.runReadAction(task);
        } else {
            ProgressIndicatorUtils.yieldToPendingWriteActions();
            ProgressIndicatorUtils.runInReadActionWithWriteActionPriority(task, progressIndicator);
        }
    }

    @Override
    public boolean processSelectedItem(@NotNull FlowDefinitionNavigationItem selected, int modifiers, @NotNull String searchText) {
        selected.navigate(true);
        return true;
    }

    @NotNull
    @Override
    public ListCellRenderer<? super Object> getElementsRenderer() {
        return new SearchEverywherePsiRenderer(this) {
            @Override
            protected TextWithIcon getItemLocation(Object value) {
                if (value instanceof FlowDefinitionNavigationItem item) {
                    return new TextWithIcon(item.getLocationString(), null);
                }
                return null;
            }
        };
    }

    @Override
    public Object getDataForItem(@NotNull FlowDefinitionNavigationItem element, @NotNull String dataId) {
        if (CommonDataKeys.NAVIGATABLE.is(dataId)) {
            return element;
        }
        if (CommonDataKeys.VIRTUAL_FILE.is(dataId)) {
            return element.getFile();
        }
        return null;
    }

    private void findKeys(@NotNull Processor<? super FlowDefinitionNavigationItem> consumer,
                          @NotNull String pattern,
                          ProgressIndicator progressIndicator) {
        if (ActionUtil.isDumbMode(myProject)) {
            return;
        }

        var allKeys = FileBasedIndex.getInstance().getAllKeys(FlowNamesIndex.KEY, myProject);

        var sorted = applyPattern(allKeys, pattern, progressIndicator);

        var scopeService = ConcordScopeService.getInstance(myProject);

        var everywhere = SearchEverywhereManager.getInstance(myProject).isEverywhere();
        for (var name : sorted) {
            progressIndicator.checkCanceled();
            var files = new CommonProcessors.CollectProcessor<VirtualFile>();
            var filter = everywhere ? ProjectScope.getAllScope(myProject) : ProjectScope.getProjectScope(myProject);
            FileBasedIndex.getInstance().getFilesWithKey(FlowNamesIndex.KEY,
                    Collections.singleton(name),
                    files,
                    filter);

            for (var file : files.getResults()) {
                if (file == null || !file.isValid()) {
                    continue;
                }

                var psiFile = PsiManager.getInstance(myProject).findFile(file);
                if (psiFile == null) {
                    continue;
                }

                var position = FileBasedIndex.getInstance().getFileData(FlowNamesIndex.KEY, file, myProject).get(name);
                if (position != null) {
                    String scopeName = null;
                    var primaryScope = scopeService.getPrimaryScope(file);
                    if (primaryScope != null) {
                        scopeName = primaryScope.getScopeName();
                    }

                    if (!consumer.process(new FlowDefinitionNavigationItem(myProject, name, file, position, scopeName))) {
                        return;
                    }
                }
            }
        }
    }

    @NotNull
    private static List<String> applyPattern(@NotNull Collection<String> keys,
                                             @NotNull String pattern,
                                             ProgressIndicator progressIndicator) {
        String lowerPattern = pattern.toLowerCase();
        List<String> result = new ArrayList<>();
        for (var key : keys) {
            progressIndicator.checkCanceled();
            if (key.toLowerCase().contains(lowerPattern)) {
                result.add(key);
            }
        }
        Collections.sort(result);
        return result;
    }

    public static class Factory implements SearchEverywhereContributorFactory<FlowDefinitionNavigationItem> {
        @NotNull
        @Override
        public SearchEverywhereContributor<FlowDefinitionNavigationItem> createContributor(@NotNull AnActionEvent initEvent) {
            return new FlowDefinitionSearchEverywhereContributor(initEvent.getProject());
        }
    }
}
