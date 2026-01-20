package brig.concord.navigation;

import brig.concord.ConcordIcons;
import com.intellij.ide.util.PsiNavigationSupport;
import com.intellij.navigation.ItemPresentation;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.Navigatable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Objects;

public class FlowDefinitionNavigationItem implements NavigationItem {

    private final @NotNull Navigatable myNavigatable;
    private final @NotNull Project myProject;
    private final @NotNull String myName;
    private final @NotNull VirtualFile myFile;
    private final int myPosition;
    private final @Nullable String myScopeName;

    FlowDefinitionNavigationItem(@NotNull Project project, @NotNull String name, @NotNull VirtualFile file, int position, @Nullable String scopeName) {
        myNavigatable = PsiNavigationSupport.getInstance().createNavigatable(project, file, position);
        myProject = project;
        myName = name;
        myFile = file;
        myPosition = position;
        myScopeName = scopeName;
    }

    @Override
    public void navigate(boolean requestFocus) {
        if (!canNavigate()) {
            return;
        }
        myNavigatable.navigate(requestFocus);
    }

    @Override
    public boolean canNavigate() {
        return myFile.isValid();
    }

    @Override
    public boolean canNavigateToSource() {
        return canNavigate();
    }

    @NotNull
    public Project getProject() {
        return myProject;
    }

    @NotNull
    @Override
    public String getName() {
        return myName;
    }

    public @NotNull VirtualFile getFile() {
        return myFile;
    }

    public String getLocationString() {
        var fileName = myFile.getName();

        if (myScopeName != null && !myScopeName.isBlank()) {
            return "[" + myScopeName + "]  " + fileName;
        }

        return fileName;
    }

    @NotNull
    @Override
    public ItemPresentation getPresentation() {
        return new ItemPresentation() {

            @Override
            public @NotNull String getPresentableText() {
                return myName;
            }

            @Override
            public String getLocationString() {
                return null;
            }

            @Override
            public @Nullable Icon getIcon(boolean unused) {
                return ConcordIcons.FILE;
            }
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        var item = (FlowDefinitionNavigationItem) o;
        return myPosition == item.myPosition && myName.equals(item.myName) && myFile.equals(item.myFile);
    }

    @Override
    public int hashCode() {
        return Objects.hash(myName, myFile, myPosition);
    }
}
