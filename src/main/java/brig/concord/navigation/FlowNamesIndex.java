package brig.concord.navigation;

import brig.concord.ConcordFileBasedIndexUtil;
import brig.concord.meta.ConcordMetaTypeProvider;
import brig.concord.meta.model.StepElementMetaType;
import brig.concord.psi.ConcordFile;
import com.intellij.psi.PsiElement;
import com.intellij.util.indexing.*;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorIntegerDescriptor;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.meta.model.YamlMetaType;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLSequence;
import org.jetbrains.yaml.psi.YamlRecursivePsiElementVisitor;

import java.util.Collections;
import java.util.Map;

public final class FlowNamesIndex extends FileBasedIndexExtension<String, Integer> {
    @NonNls
    public static final ID<String, Integer> KEY = ID.create("concord.yaml.flow.names");

    @Override
    public int getVersion() {
        return 18;
    }

    @NotNull
    @Override
    public DataIndexer<String, Integer, FileContent> getIndexer() {
        return new DataIndexer<>() {
            @NotNull
            @Override
            public Map<String, Integer> map(@NotNull FileContent inputData) {
                if (!(inputData.getPsiFile() instanceof ConcordFile)) {
                    return Collections.emptyMap();
                }

                Object2IntMap<String> map = new Object2IntOpenHashMap<>();
                inputData.getPsiFile().accept(new YamlRecursivePsiElementVisitor() {
                    @Override
                    public void visitKeyValue(@NotNull YAMLKeyValue keyValue) {
                        PsiElement key = keyValue.getKey();
                        if (key != null) {
                            YamlMetaType type = ConcordMetaTypeProvider.getInstance(keyValue.getProject()).getResolvedKeyValueMetaTypeMeta(keyValue);
                            if (type instanceof StepElementMetaType) {
                                map.put(keyValue.getKeyText(), key.getTextOffset());
                            }
                        }

                        super.visitKeyValue(keyValue);
                    }

                    @Override
                    public void visitSequence(@NotNull YAMLSequence sequence) {
                        // Do not visit children
                    }
                });
                return map;
            }
        };
    }

    @NotNull
    @Override
    public KeyDescriptor<String> getKeyDescriptor() {
        return EnumeratorStringDescriptor.INSTANCE;
    }

    @NotNull
    @Override
    public DataExternalizer<Integer> getValueExternalizer() {
        return EnumeratorIntegerDescriptor.INSTANCE;
    }

    @NotNull
    @Override
    public ID<String, Integer> getName() {
        return KEY;
    }

    @NotNull
    @Override
    public FileBasedIndex.InputFilter getInputFilter() {
        return ConcordFileBasedIndexUtil.INPUT_FILTER;
    }

    @Override
    public boolean dependsOnFileContent() {
        return true;
    }
}
