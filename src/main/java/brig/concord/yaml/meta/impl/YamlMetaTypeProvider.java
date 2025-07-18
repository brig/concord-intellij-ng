package brig.concord.yaml.meta.impl;

// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.ModificationTracker;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ObjectUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import brig.concord.yaml.YAMLTokenTypes;
import brig.concord.yaml.meta.model.Field;
import brig.concord.yaml.meta.model.ModelAccess;
import brig.concord.yaml.meta.model.YamlArrayType;
import brig.concord.yaml.meta.model.YamlMetaType;
import brig.concord.yaml.psi.*;

import java.util.function.Supplier;

public class YamlMetaTypeProvider {

    private static final Logger LOG = Logger.getInstance(YamlMetaTypeProvider.class);

    private final Key<CachedValue<MetaTypeProxy>> myKey;

    private final @NotNull ModelAccess myMetaModel;
    private final @NotNull ModificationTracker myModificationTracker;

    public YamlMetaTypeProvider(final @NotNull ModelAccess metaModel, final @NotNull ModificationTracker modificationTracker) {
        myMetaModel = metaModel;
        myKey = Key.create(metaModel.getClass().getName() + ":KEY");
        myModificationTracker = modificationTracker;
    }

    public @Nullable MetaTypeProxy getMetaTypeProxy(@NotNull PsiElement psi) {
        if (psi instanceof YAMLValue) {
            return getValueMetaType((YAMLValue)psi);
        }
        YAMLValue metaOwner = getMetaOwner(psi);
        return metaOwner == null ? null : getValueMetaType(metaOwner);
    }

    public @Nullable YAMLValue getMetaOwner(@NotNull PsiElement psi) {
        PsiFile file = psi.getContainingFile();
        if (!(file instanceof YAMLFile)) {
            return null;
        }
        return getTypedAncestorOrSelf(psi, YAMLValue.class);
    }

    public @Nullable MetaTypeProxy getKeyValueMetaType(@NotNull YAMLKeyValue keyValue) {
        if (keyValue.getValue() != null) {
            return getMetaTypeProxy(keyValue.getValue());
        }
        Field type = computeMetaType(keyValue);
        return FieldAndRelation.forNullable(type, Field.Relation.OBJECT_CONTENTS);
    }

    public @Nullable MetaTypeProxy getValueMetaType(@NotNull YAMLValue typedValue) {
        return CachedValuesManager.getCachedValue(typedValue, myKey, () -> {
            debug(() -> " >> computing type for : " + YamlDebugUtil.getDebugInfo(typedValue));
            MetaTypeProxy computed = computeMetaType(typedValue);
            debug(() -> " << finished for : " + YamlDebugUtil.getDebugInfo(typedValue) +
                    ", result: " + (computed == null ? "<null>" : computed));
            return new CachedValueProvider.Result<>(computed, typedValue.getContainingFile(), myModificationTracker);
        });
    }

    private @Nullable MetaTypeProxy computeMetaType(@NotNull YAMLValue value) {
        PsiElement typed = PsiTreeUtil.getParentOfType(value, YAMLKeyValue.class, YAMLSequenceItem.class, YAMLDocument.class);
        if (typed instanceof YAMLDocument) {
            Field root = myMetaModel.getRoot((YAMLDocument)typed);
            return FieldAndRelation.forNullable(root, Field.Relation.OBJECT_CONTENTS);
        }
        if (typed instanceof YAMLSequenceItem sequenceItem) {
            YAMLSequence sequence = ObjectUtils.tryCast(sequenceItem.getParent(), YAMLSequence.class);
            if (sequence == null) {
                debug(() -> "Unexpected: sequenceItem parent is not a sequence: " + sequenceItem.getParent());
                return null;
            }
            MetaTypeProxy sequenceMeta = getMetaTypeProxy(sequence);

            if (sequenceMeta != null) {
                YamlMetaType sequenceMetaType = sequenceMeta.getMetaType();
                Field resultField = value instanceof YAMLSequence && sequenceMetaType instanceof YamlArrayType ?
                        new Field("<array>", sequenceMetaType) :  // unwind nested array
                        sequenceMeta.getField();

                return FieldAndRelation.forNullable(specializeField(resultField, sequenceItem.getValue()), Field.Relation.SEQUENCE_ITEM);
            }

            return null;
        }
        if (typed instanceof YAMLKeyValue keyValue) {
            Field keyValueType = computeMetaType(keyValue);
            if (keyValueType == null) {
                return null;
            }
            Field.Relation relation = Field.Relation.OBJECT_CONTENTS;
            if (value instanceof YAMLScalar) {
                relation = computeScalarValueRelation(keyValue, (YAMLScalar)value);
            }
            else if (value instanceof YAMLSequence) {
                relation = Field.Relation.SEQUENCE_ITEM;
            }
            return FieldAndRelation.forNullable(keyValueType, relation);
        }
        return null;
    }

    private static Field.Relation computeScalarValueRelation(@NotNull YAMLKeyValue keyValue, @NotNull YAMLScalar value) {
        // in the most normal case it is always Field.Relation.SCALAR_VALUE;
        // however it should be adjusted for bad or incomplete yaml
        // there may be following cases:
        // a) sometimes auxiliary YamlMapping inserted by the parser to recover from incomplete key
        //    a-1) see `provider/keyIncomplete`,
        //    a-2) see `provider/keyIncompleteInner2` test
        // b) sometimes it is not inserted, and scalar is a direct sibling of other YamlKeyValue's
        //    b-1) see `provider/keyIncomplete2`
        // c) sometimes the incomplete object value is interpreted as a normal key-value-pair but with key containing EOL
        //    c-1) see `provider/keyIncomplete3`
        //    c-2) see `provider/keyIncompleteInner`
        // d) finally there may be incomplete state where the key already has both object and scalar content
        //    d-1) see `stringOrObject_stringMisplaced` test

        PsiElement keySibling = value.getParent() instanceof YAMLMapping ? value.getParent() : value;
        if (hasLineBreakBetweenKeyAndValue(keyValue, keySibling)) {
            return Field.Relation.OBJECT_CONTENTS;
        }
        return Field.Relation.SCALAR_VALUE;
    }

    private @Nullable Field computeMetaType(@NotNull YAMLKeyValue keyValue) {
        YAMLMapping parentMapping = keyValue.getParentMapping();
        if (parentMapping == null) {
            debug(() -> "Unexpected: keyValue parent is not a mapping: " + keyValue.getParent());
            return null;
        }
        MetaTypeProxy parentMeta = getMetaTypeProxy(parentMapping);
        Field childMeta = findChildMeta(parentMeta, keyValue);

        return childMeta != null ? specializeField(childMeta, keyValue.getValue()) : null;
    }

    private static @NotNull Field specializeField(@NotNull Field field, @Nullable YAMLValue value) {
        return value != null ? field.resolveToSpecializedField(value) : field;
    }

    @Contract("null, _ -> null")
    private static @Nullable Field findChildMeta(@Nullable MetaTypeProxy parentMeta, @NotNull YAMLKeyValue child) {
        if (parentMeta == null) {
            return null;
        }
        String tag = child.getKeyText().trim();
        return parentMeta.getMetaType().findFeatureByName(tag);
    }

    @SuppressWarnings("SameParameterValue")
    private static @Nullable <T extends PsiElement> T getTypedAncestorOrSelf(@NotNull PsiElement psi, @NotNull Class<? extends T> clazz) {
        return clazz.isInstance(psi) ? clazz.cast(psi) : PsiTreeUtil.getParentOfType(psi, clazz);
    }

    private static void debug(Supplier<String> textSupplier) {
        if(LOG.isDebugEnabled()) {
            String text = textSupplier.get();
            LOG.debug(text);
            //System.err.println(text);
        }
    }

    private static boolean hasLineBreakBetweenKeyAndValue(@NotNull YAMLKeyValue keyValue, @NotNull PsiElement keySibling) {
        if (keySibling.getParent() != keyValue) {
            return false; //assert?
        }
        PsiElement key = keyValue.getKey();
        for (PsiElement prev = keySibling.getPrevSibling(); prev != null && prev != key; prev = prev.getPrevSibling()) {
            if (prev.getNode().getElementType() == YAMLTokenTypes.EOL) {
                return true;
            }
        }
        return false;
    }

    public static class FieldAndRelation implements MetaTypeProxy {
        public static @Nullable FieldAndRelation forNullable(@Nullable Field field, @NotNull Field.Relation relation) {
            return field == null ? null : new FieldAndRelation(field, relation);
        }

        private final Field myField;
        private final Field.Relation myRelation;

        public FieldAndRelation(@NotNull Field field, @NotNull Field.Relation relation) {
            myField = field;
            myRelation = relation;
        }

        @Override
        public @NotNull Field getField() {
            return myField;
        }

        public @NotNull Field.Relation getRelation() {
            return myRelation;
        }

        @Override
        public String toString() {
            return "[" + getField().getName() + " : " + getRelation() + "]";
        }

        @Override
        public @NotNull YamlMetaType getMetaType() {
            return myField.getType(myRelation);
        }
    }

    public interface MetaTypeProxy {

        @NotNull
        YamlMetaType getMetaType();

        @NotNull
        Field getField();
    }
}
