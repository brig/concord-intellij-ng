package brig.concord;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.indexing.DefaultFileTypeSpecificInputFilter;
import com.intellij.util.indexing.FileBasedIndex;
import org.jetbrains.annotations.NotNull;

public final class ConcordFileBasedIndexUtil {

    public static final FileBasedIndex.InputFilter INPUT_FILTER =
            new DefaultFileTypeSpecificInputFilter(ConcordLanguage.INSTANCE.getAssociatedFileType()) {
                @Override
                public boolean acceptInput(@NotNull VirtualFile file) {
                    return !file.getPath().contains("/target/");
                }
            };

}
