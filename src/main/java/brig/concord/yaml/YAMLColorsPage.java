package brig.concord.yaml;

// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

import brig.concord.ConcordBundle;
import brig.concord.ConcordFileType;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

public class YAMLColorsPage implements ColorSettingsPage {

    private static final String DEMO_TEXT = """
    ---
    # Read about fixtures at http://ar.rubyonrails.org/classes/Fixtures.html
    static_sidebar:
      id: "foo"
      name: 'side_bar'
      staged_position: 1
      blog_id: 1
      config: |+
        --- !map:HashWithIndifferentAccess
          title: Static Sidebar
          body: The body of a static sidebar
      type: StaticSidebar
      description: >
        Sidebar configuration example
      extensions:
        - &params\s
            auto_run: true
            reload: true
        - *params""";

    private static final AttributesDescriptor[] ATTRS = new AttributesDescriptor[]{
            new AttributesDescriptor(ConcordBundle.message("color.settings.yaml.key"), YAMLHighlighter.SCALAR_KEY),
            new AttributesDescriptor(ConcordBundle.message("color.settings.yaml.string"), YAMLHighlighter.SCALAR_STRING),
            new AttributesDescriptor(ConcordBundle.message("color.settings.yaml.dstring"), YAMLHighlighter.SCALAR_DSTRING),
            new AttributesDescriptor(ConcordBundle.message("color.settings.yaml.scalar.list"), YAMLHighlighter.SCALAR_LIST),
            new AttributesDescriptor(ConcordBundle.message("color.settings.yaml.scalar.text"), YAMLHighlighter.SCALAR_TEXT),
            new AttributesDescriptor(ConcordBundle.message("color.settings.yaml.text"), YAMLHighlighter.TEXT),
            new AttributesDescriptor(ConcordBundle.message("color.settings.yaml.sign"), YAMLHighlighter.SIGN),
            new AttributesDescriptor(ConcordBundle.message("color.settings.yaml.anchor"), YAMLHighlighter.ANCHOR),
            new AttributesDescriptor(ConcordBundle.message("color.settings.yaml.comment"), YAMLHighlighter.COMMENT)
    };

    // Empty still
    private static final Map<String, TextAttributesKey> ADDITIONAL_HIGHLIGHT_DESCRIPTORS = new HashMap<>();

    @Override
    public @Nullable Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
        return ADDITIONAL_HIGHLIGHT_DESCRIPTORS;
    }

    @Override
    public @NotNull String getDisplayName() {
        return ConcordBundle.message("color.settings.yaml.name");
    }

    @Override
    public Icon getIcon() {
        return ConcordFileType.INSTANCE.getIcon();
    }

    @Override
    public AttributesDescriptor @NotNull [] getAttributeDescriptors() {
        return ATTRS;
    }

    @Override
    public ColorDescriptor @NotNull [] getColorDescriptors() {
        return ColorDescriptor.EMPTY_ARRAY;
    }

    @Override
    public @NotNull SyntaxHighlighter getHighlighter() {
        return new YAMLSyntaxHighlighter();
    }

    @Override
    public @NotNull String getDemoText() {
        return DEMO_TEXT;
    }

}

