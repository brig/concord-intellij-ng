package brig.concord.navigation;

import brig.concord.ConcordBundle;
import com.intellij.ide.actions.searcheverywhere.SearchEverywhereContributor;
import com.intellij.ide.actions.searcheverywhere.SearchEverywhereContributorFactory;
import com.intellij.ide.actions.searcheverywhere.SearchEverywhereManager;
import com.intellij.ide.util.NavigationItemListCellRenderer;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ex.ActionUtil;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.util.ProgressIndicatorUtils;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.ProjectScope;
import com.intellij.util.CommonProcessors;
import com.intellij.util.Processor;
import com.intellij.util.indexing.FileBasedIndex;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
        Application application = ApplicationManager.getApplication();
        if (application.isUnitTestMode()) {
            application.runReadAction(task);
        } else {
            ProgressIndicatorUtils.yieldToPendingWriteActions();
            ProgressIndicatorUtils.runInReadActionWithWriteActionPriority(task, progressIndicator);
        }
    }

    @Override
    public boolean processSelectedItem(@NotNull FlowDefinitionNavigationItem selected, int modifiers, @NotNull String searchText) {
        ((Navigatable) selected).navigate(true);
        return true;
    }

    @NotNull
    @Override
    public ListCellRenderer<? super Object> getElementsRenderer() {
        return new NavigationItemListCellRenderer();
    }

    @Override
    public Object getDataForItem(@NotNull FlowDefinitionNavigationItem element, @NotNull String dataId) {
        return null;
    }

    private void findKeys(@NotNull Processor<? super FlowDefinitionNavigationItem> consumer,
                          @NotNull String pattern,
                          ProgressIndicator progressIndicator) {
        if (ActionUtil.isDumbMode(myProject)) {
            return;
        }

        Collection<String> allKeys = FileBasedIndex.getInstance().getAllKeys(FlowNamesIndex.KEY, myProject);

        List<String> sorted = applyPattern(allKeys, pattern, progressIndicator);

        boolean everywhere = SearchEverywhereManager.getInstance(myProject).isEverywhere();
        for (String name : sorted) {
            progressIndicator.checkCanceled();
            CommonProcessors.CollectProcessor<VirtualFile> files = new CommonProcessors.CollectProcessor<>();
            GlobalSearchScope filter = everywhere ? ProjectScope.getAllScope(myProject) : ProjectScope.getProjectScope(myProject);
            FileBasedIndex.getInstance().getFilesWithKey(FlowNamesIndex.KEY,
                    Collections.singleton(name),
                    files,
                    filter);

            for (VirtualFile file : files.getResults()) {
                if (file == null || !file.isValid()) {
                    continue;
                }
                
                PsiFile psiFile = PsiManager.getInstance(myProject).findFile(file);
                if (psiFile == null) {
                    continue;
                }

                Integer position = FileBasedIndex.getInstance().getFileData(FlowNamesIndex.KEY, file, myProject).get(name);
                if (position != null) {
                    if (!consumer.process(new FlowDefinitionNavigationItem(myProject, name, file, position))) {
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
        List<String> result = new ArrayList<>();
        for (String key : keys) {
            progressIndicator.checkCanceled();
            int start = key.toLowerCase().indexOf(pattern.toLowerCase());
            if (start >= 0) {
                result.add(key);
            }
        }
        return result.stream().sorted().collect(Collectors.toList());
    }

    public static class Factory implements SearchEverywhereContributorFactory<FlowDefinitionNavigationItem> {
        @NotNull
        @Override
        public SearchEverywhereContributor<FlowDefinitionNavigationItem> createContributor(@NotNull AnActionEvent initEvent) {
            return new FlowDefinitionSearchEverywhereContributor(initEvent.getProject());
        }
    }
}
